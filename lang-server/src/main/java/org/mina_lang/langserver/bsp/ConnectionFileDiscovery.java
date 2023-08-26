/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class ConnectionFileDiscovery implements BuildServerDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(BuildServerConnector.class);

    private static final Gson gson = new Gson();

    private static final BiPredicate<Path, BasicFileAttributes> isJsonFile = (path, attrs) -> {
        var fileName = path.getFileName().toString();
        return fileName.endsWith(".json") && attrs.isRegularFile();
    };

    BaseDirectories baseDirs;

    public ConnectionFileDiscovery(BaseDirectories baseDirs) {
        this.baseDirs = baseDirs;
    }

    public Flux<BspConnectionDetails> discover(WorkspaceFolder folder) {
        Path workspacePath = Paths.get(URI.create(folder.getUri()));
        Path workspaceBspFolder = workspacePath.resolve(".bsp");

        // Try <workspace>/.bsp
        Flux<BspConnectionDetails> workspaceConnectionFlux = discover(workspaceBspFolder);

        return workspaceConnectionFlux.collectList()
            .flatMapMany(workspaceConnectionFiles -> {
                if (!workspaceConnectionFiles.isEmpty()) {
                    return Flux.fromIterable(workspaceConnectionFiles);
                } else {
                    // Try <user data directory>/bsp
                    Path dataLocalDirPath = Paths.get(baseDirs.dataLocalDir);
                    Path dataLocalDirBspFolder = dataLocalDirPath.resolve("bsp");

                    // On Windows there are both Local and Roaming user data directories
                    Path dataDirPath = Paths.get(baseDirs.dataDir);
                    Path dataDirBspFolder = dataDirPath.resolve("bsp");

                    Flux<BspConnectionDetails> dataDirConnectionFlux = Flux.concat(
                        discover(dataLocalDirBspFolder),
                        discover(dataDirBspFolder)
                    ).distinct();

                    return dataDirConnectionFlux.collectList()
                        .flatMapMany(dataDirConnectionFiles -> {
                            if (!dataDirConnectionFiles.isEmpty()) {
                                return Flux.fromIterable(dataDirConnectionFiles);
                            } else {
                                // TODO: Try system-level directories to follow the BSP spec
                                return Flux.empty();
                            }
                        });
                }
            });
    }

    Flux<BspConnectionDetails> discover(Path bspFolder) {
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
                var connectionFileString = Files.readString(connectionFile);
                var connectionDetails = gson.fromJson(connectionFileString, BspConnectionDetails.class);
                if (connectionDetails != null) {
                    if (connectionDetails.getLanguages().contains("mina")) {
                        return Flux.just(connectionDetails);
                    } else {
                        return Flux.empty();
                    }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionFileDiscovery that = (ConnectionFileDiscovery) o;
        return Objects.equals(baseDirs, that.baseDirs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseDirs);
    }

    @Override
    public String toString() {
        return "ConnectionFileDiscovery[" +
                "baseDirs=" + baseDirs +
                ']';
    }
}
