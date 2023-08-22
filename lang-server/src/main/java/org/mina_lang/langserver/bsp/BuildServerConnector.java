/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.*;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.mina_lang.langserver.BuildInfo;
import org.mina_lang.langserver.MinaLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BuildServerConnector {
    private static final Logger logger = LoggerFactory.getLogger(BuildServerConnector.class);

    private MinaLanguageServer languageServer;
    private WorkspaceFolder workspaceFolder;
    private BuildServerDiscovery discovery;
    private BuildServerLauncher launcher;

    private AtomicReference<BspConnectionDetails> connectionDetails = new AtomicReference<>();
    private AtomicReference<MinaBuildClient> buildClient = new AtomicReference<>();
    private AtomicReference<BuildServerCapabilities> buildServerCapabilities = new AtomicReference<>();
    private ConcurrentLinkedQueue<BuildTarget> buildTargets = new ConcurrentLinkedQueue<>();

    public BuildServerConnector(MinaLanguageServer languageServer, WorkspaceFolder workspaceFolder, BuildServerDiscovery discovery, BuildServerLauncher launcher) {
        this.languageServer = languageServer;
        this.workspaceFolder = workspaceFolder;
        this.discovery = discovery;
        this.launcher = launcher;
    }

    public CompletableFuture<Void> initialise() {
        return discovery.discover(workspaceFolder)
            .collectList()
            .flatMap(this::initialise)
            .then().toFuture();
    }

    Mono<Void> initialise(List<BspConnectionDetails> connectionDetails) {
        return configureBspConnection(connectionDetails).flatMap(res -> {
            var client = buildClient.get();
            buildServerCapabilities.set(res.getCapabilities());
            client.buildServer().onBuildInitialized();
            return Mono.fromFuture(client.buildServer().workspaceBuildTargets()).doOnNext(buildTargets -> {
                logger.info("Build targets for workspace folder '{}': {}", workspaceFolder.getName(), buildTargets.getTargets());
                this.buildTargets.addAll(buildTargets.getTargets());
            }).flatMap(buildTargets -> {
                return getOpenDocumentBuildTargets();
            }).flatMap(targetIds -> {
                var compileParams = new CompileParams(targetIds);
                return Mono.fromFuture(client.buildServer().buildTargetCompile(compileParams));
            }).then();
        });
    }

    Mono<List<BuildTargetIdentifier>> getOpenDocumentBuildTargets() {
        return Flux
            .fromIterable(languageServer.getTextDocumentService().getAllDocuments())
            .flatMap(document -> {
                var inverseSourcesParams = new InverseSourcesParams(new TextDocumentIdentifier(document.getUri()));
                return Mono.fromFuture(buildClient.get().buildServer().buildTargetInverseSources(inverseSourcesParams));
            }).flatMap(inverseSources -> {
                return Flux.fromIterable(inverseSources.getTargets());
            }).collectList();
    }

    Mono<InitializeBuildResult> configureBspConnection(List<BspConnectionDetails> connectionDetails) {
        if (connectionDetails.isEmpty()) {
            languageServer.getClient().showMessage(noBspServersMessage());
            return Mono.empty();
        } else if (connectionDetails.size() > 1) {
            // Check which connection config the user wants to use
            var connectionOptions = createConnectionOptions(connectionDetails);
            var bspConnectionMessage = chooseBspConnectionMessage(connectionOptions);
            return Mono.fromFuture(languageServer.getClient().showMessageRequest(bspConnectionMessage)).flatMap(actionItem -> {
                var chosenConnection = connectionOptions.get(actionItem);
                return connectToBuildServer(chosenConnection);
            });
        } else {
            // There's only one connection file that we can use
            var chosenConnection = connectionDetails.get(0);
            return connectToBuildServer(chosenConnection);
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

    MessageParams noBspServersMessage() {
        return new MessageParams(MessageType.Warning, "No build servers with Mina support were found for workspace folder '" + workspaceFolder.getName() + "'");
    }

    ShowMessageRequestParams chooseBspConnectionMessage(Map<MessageActionItem, BspConnectionDetails> connectionOptions) {
        var connectionActionItems = connectionOptions.keySet().stream().toList();

        var messageParams = new ShowMessageRequestParams();
        messageParams.setActions(connectionActionItems);
        messageParams.setType(MessageType.Info);
        messageParams.setMessage("Multiple build server connection details were found. Which one do you wish to use for workspace folder '" + workspaceFolder.getName() + "'?");

        return messageParams;
    }

    Mono<InitializeBuildResult> connectToBuildServer(BspConnectionDetails chosenConnection) {
        connectionDetails.set(chosenConnection);
        try {
            var buildClient = connect(chosenConnection);
            this.buildClient.set(buildClient);
            var buildServer = buildClient.buildServer();
            var initializeParams = initializeBuildParams(workspaceFolder);
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
        return buildClient.get().disconnect();
    }

    MinaBuildClient connect(BspConnectionDetails details) throws IOException {
        logger.info("Connecting to build server {} ({}) for workspace folder '{}'", details.getName(), details.getVersion(), workspaceFolder.getName());
        var connection = launcher.launch(workspaceFolder, details);
        return connect(connection.input(), connection.output());
    }

    MinaBuildClient connect(InputStream in, OutputStream out) {
        var buildClient = new MinaBuildClient(languageServer);

        var launcher = new Launcher.Builder<BuildServer>()
            .setLocalService(buildClient)
            .setRemoteInterface(BuildServer.class)
            .setInput(in)
            .setOutput(out)
            .setExecutorService(languageServer.getExecutor())
            .traceMessages(new PrintWriter(System.err))
            .validateMessages(true)
            .create();

        buildClient.onConnectWithServer(launcher.getRemoteProxy());
        buildClient.onStartListening(launcher.startListening());

        return buildClient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildServerConnector that = (BuildServerConnector) o;
        return Objects.equals(languageServer, that.languageServer) && Objects.equals(workspaceFolder, that.workspaceFolder) && Objects.equals(discovery, that.discovery) && Objects.equals(launcher, that.launcher) && Objects.equals(connectionDetails, that.connectionDetails) && Objects.equals(buildClient, that.buildClient) && Objects.equals(buildServerCapabilities, that.buildServerCapabilities) && Objects.equals(buildTargets, that.buildTargets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageServer, workspaceFolder, discovery, launcher, connectionDetails, buildClient, buildServerCapabilities, buildTargets);
    }

    @Override
    public String toString() {
        return "BuildServerConnector[" +
            "languageServer=" + languageServer +
            ", workspaceFolder=" + workspaceFolder +
            ", discovery=" + discovery +
            ", launcher=" + launcher +
            ", connectionDetails=" + connectionDetails +
            ", buildClient=" + buildClient +
            ", buildServerCapabilities=" + buildServerCapabilities +
            ", buildTargets=" + buildTargets +
            ']';
    }
}
