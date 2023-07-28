/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import ch.epfl.scala.bsp4j.BuildServer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class MinaBuildServer {
    private static final Logger logger = LoggerFactory.getLogger(MinaBuildServer.class);

    private static final Gson gson = new Gson();

    private static final BiPredicate<Path, BasicFileAttributes> isJsonFile = (path, attrs) -> {
        var fileName = path.getFileName().toString();
        return fileName.endsWith(".json") && attrs.isRegularFile();
    };

    public static Flux<BspConnectionDetails> discover(WorkspaceFolder folder) {
        Path workspacePath = Paths.get(URI.create(folder.getUri()));
        Path workspaceBspFolder = workspacePath.resolve(".bsp");

        // Try <workspace>/.bsp
        Flux<BspConnectionDetails> workspaceConnectionFlux = discover(workspaceBspFolder);

        return workspaceConnectionFlux.collectList()
            .flatMapMany(workspaceConnectionFiles -> {
                if (workspaceConnectionFiles.isEmpty()) {
                    // Try user data directory
                    Path dataLocalDirPath = Paths.get(BaseDirectories.get().dataLocalDir);
                    Path dataLocalDirBspFolder = dataLocalDirPath.resolve("bsp");

                    // On Windows there are both Local and Roaming user data directories
                    Path dataDirPath = Paths.get(BaseDirectories.get().dataDir);
                    Path dataDirBspFolder = dataDirPath.resolve("bsp");

                    Flux<BspConnectionDetails> dataDirConnectionFlux = Flux.concat(
                        discover(dataLocalDirBspFolder),
                        discover(dataDirBspFolder)
                    ).distinct();

                    return dataDirConnectionFlux.collectList()
                        .flatMapMany(dataDirConnectionFiles -> {
                            if (dataDirConnectionFiles.isEmpty()) {
                                // TODO: Try system-level directories to follow the BSP spec
                                return Flux.empty();
                            } else {
                                return Flux.fromIterable(dataDirConnectionFiles);
                            }

                        });
                } else {
                    return Flux.fromIterable(workspaceConnectionFiles);
                }
            });
    }

    public static Flux<BspConnectionDetails> discover(Path bspFolder) {
        if (Files.notExists(bspFolder)) {
           return Flux.empty();
        }

        var connectionFiles = Flux.using(
            () -> Files.find(bspFolder, 1, isJsonFile),
            Flux::fromStream,
            Stream::close
        );

        return connectionFiles.flatMap(connectionFile -> {
            try {
                var connectionDetails = gson.fromJson(Files.readString(connectionFile), BspConnectionDetails.class);
                if (connectionDetails != null) {
                   return Flux.just(connectionDetails);
                } else {
                    // Frustratingly Gson returns null if the JSON file is empty instead of throwing
                    logger.error("Error reading connection file {}", connectionFile);
                    return Flux.empty();
                }
            } catch (IOException | JsonSyntaxException e) {
                logger.error("Error reading connection file {}", connectionFile, e);
                return Flux.empty();
            }
        });
    }

    public static MinaBuildClient connect(MinaLanguageServer languageServer, WorkspaceFolder workspaceFolder, BspConnectionDetails details) throws IOException {
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

        buildClient.onStartListening(launcher.startListening());
        buildClient.onConnectWithServer(launcher.getRemoteProxy());

        return buildClient;
    }
}
