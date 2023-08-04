/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.*;
import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MinaBuildServers {
    private static final Logger logger = LoggerFactory.getLogger(MinaBuildServers.class);

    private MinaLanguageServer languageServer;

    private ConcurrentHashMap<WorkspaceFolder, BspConnectionDetails> bspConnectionDetails = new ConcurrentHashMap<>();
    private ConcurrentHashMap<WorkspaceFolder, MinaBuildClient> bspConnections = new ConcurrentHashMap<>();
    private ConcurrentHashMap<WorkspaceFolder, BuildServerCapabilities> buildServerCapabilities = new ConcurrentHashMap<>();
    private ConcurrentHashMap<WorkspaceFolder, List<BuildTarget>> bspBuildTargets = new ConcurrentHashMap<>();

    public MinaBuildServers(MinaLanguageServer languageServer) {
        this.languageServer = languageServer;
    }

    CompletableFuture<Void> initialiseBuildServers() {
        return Flux
            .fromIterable(languageServer.getWorkspaceFolders().get())
            .flatMap(folder -> {
                return BuildServerDiscovery
                    .discover(folder, BaseDirectories.get())
                    .collectList()
                    .flatMap(connectionDetails -> initialiseBuildServer(folder, connectionDetails));
            }).then().toFuture();
    }

    Mono<Void> initialiseBuildServer(WorkspaceFolder folder, List<BspConnectionDetails> connectionDetails) {
        return configureBspConnection(folder, connectionDetails).flatMap(res -> {
            buildServerCapabilities.put(folder, res.getCapabilities());
            var folderConnection = bspConnections.get(folder);
            folderConnection.buildServer().onBuildInitialized();
            return Mono.fromFuture(folderConnection.buildServer().workspaceBuildTargets()).doOnNext(buildTargets -> {
                logger.info("Build targets for workspace folder '{}': {}", folder.getName(), buildTargets.getTargets());
                bspBuildTargets.put(folder, buildTargets.getTargets());
            }).then();
        });
    }

    Mono<InitializeBuildResult> configureBspConnection(WorkspaceFolder folder, List<BspConnectionDetails> connectionDetails) {
        if (connectionDetails.isEmpty()) {
            languageServer.getClient().showMessage(new MessageParams(org.eclipse.lsp4j.MessageType.Warning, "No build server connection details were found for workspace folder '" + folder.getName() + "'"));
            return Mono.empty();
        } else if (connectionDetails.size() > 1) {
            // Check which connection config the user wants to use
            var connectionOptions = createConnectionOptions(connectionDetails);
            var bspConnectionMessage = chooseBspConnectionMessage(folder, connectionOptions);
            return Mono.fromFuture(languageServer.getClient().showMessageRequest(bspConnectionMessage)).flatMap(actionItem -> {
                var chosenConnection = connectionOptions.get(actionItem);
                return connectToBuildServer(folder, chosenConnection);
            });
        } else {
            // There's only one connection file that we can use
            var chosenConnection = connectionDetails.get(0);
            return connectToBuildServer(folder, chosenConnection);
        }
    }

    Map<MessageActionItem, BspConnectionDetails> createConnectionOptions(List<BspConnectionDetails> connectionDetails) {
        return connectionDetails.stream()
            .map(this::createConnectionOption)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    Map.Entry<MessageActionItem, BspConnectionDetails> createConnectionOption(BspConnectionDetails details) {
        var actionItem = new MessageActionItem("%s (%s)".formatted(details.getName(), details.getVersion()));
        return new AbstractMap.SimpleEntry<>(actionItem, details);
    }

    ShowMessageRequestParams chooseBspConnectionMessage(WorkspaceFolder folder, Map<MessageActionItem, BspConnectionDetails> connectionOptions) {
        var connectionActionItems = connectionOptions.keySet().stream().toList();

        var messageParams = new ShowMessageRequestParams();
        messageParams.setActions(connectionActionItems);
        messageParams.setType(MessageType.Info);
        messageParams.setMessage("Multiple build server connection details were found. Which one do you wish to use for workspace folder '" + folder.getName() + "'?");

        return messageParams;
    }

    Mono<InitializeBuildResult> connectToBuildServer(WorkspaceFolder folder, BspConnectionDetails chosenConnection) {
        bspConnectionDetails.put(folder, chosenConnection);
        try {
            var buildClient = connect(folder, chosenConnection);
            bspConnections.put(folder, buildClient);
            var buildServer = buildClient.buildServer();
            var initializeParams = initializeBuildParams(folder);
            return Mono.fromFuture(buildServer.buildInitialize(initializeParams));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    InitializeBuildParams initializeBuildParams(WorkspaceFolder folder) {
        var capabilities = new BuildClientCapabilities(List.of("mina"));
        return new InitializeBuildParams(
            "Mina Language Server",
            BuildInfo.version,
            BuildInfo.bspVersion,
            folder.getUri(),
            capabilities);
    }

    public CompletableFuture<Void> disconnect() {
        return Flux.fromIterable(bspConnections.values())
            .parallel()
            .flatMap(client -> Mono.fromFuture(client.disconnect()))
            .then().toFuture();
    }

    public MinaBuildClient connect(WorkspaceFolder workspaceFolder, BspConnectionDetails details) throws IOException {
        logger.info("Connecting to build server {} ({}) for workspace folder '{}'", details.getName(), details.getVersion(), workspaceFolder.getName());

        var workspacePath = Paths.get(URI.create(workspaceFolder.getUri()));

        var processBuilder = new ProcessBuilder()
            .command(details.getArgv())
            .directory(workspacePath.toFile());

        processBuilder.environment().putAll(System.getenv());

        var logFolder = System.getProperty("LOG_FOLDER");

        if (logFolder != null) {
            processBuilder.environment().put("LOG_FOLDER", logFolder);
        }

        var separator = System.lineSeparator();
        var argvString = String.join(separator, details.getArgv());
        logger.debug("Launching build server process with arguments: {}{}", separator, argvString);

        var buildServerProcess = processBuilder.start();

        languageServer.getExecutor().submit(() -> {
            // Forward build server's stderr to ours
            var errorStream = buildServerProcess.getErrorStream();
            var reader = new BufferedReader(new InputStreamReader(errorStream));
            reader.lines().forEach(System.err::println);
        });

        var buildClient = new MinaBuildClient(languageServer, buildServerProcess);

        var launcher = new Launcher.Builder<BuildServer>()
            .setLocalService(buildClient)
            .setRemoteInterface(BuildServer.class)
            .setInput(buildServerProcess.getInputStream())
            .setOutput(buildServerProcess.getOutputStream())
            .setExecutorService(languageServer.getExecutor())
            .traceMessages(new PrintWriter(System.err))
            .validateMessages(true)
            .create();

        buildClient.onConnectWithServer(launcher.getRemoteProxy());
        buildClient.onStartListening(launcher.startListening());

        return buildClient;
    }
}
