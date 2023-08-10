/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.workspace;

import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.mina_lang.langserver.MinaLanguageServer;
import org.mina_lang.langserver.bsp.BuildServerConnector;
import org.mina_lang.langserver.bsp.BuildServerProcessLauncher;
import org.mina_lang.langserver.bsp.ConnectionFileDiscovery;
import org.mina_lang.langserver.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.*;

public class MinaWorkspaceService implements WorkspaceService {
    private static Logger logger = LoggerFactory.getLogger(MinaWorkspaceService.class);

    private MinaLanguageServer server;
    private BuildServerProcessLauncher launcher;
    private ConnectionFileDiscovery discovery;
    private ConcurrentLinkedQueue<WorkspaceFolder> workspaceFolders = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<WorkspaceFolder, BuildServerConnector> buildServers = new ConcurrentHashMap<>();

    public MinaWorkspaceService(MinaLanguageServer server) {
        this.server = server;
        var threadFactory = DaemonThreadFactory.create(logger, "mina-bsp-err-forwarder-%d");
        this.launcher = new BuildServerProcessLauncher(Executors.newCachedThreadPool(threadFactory));
        this.discovery = new ConnectionFileDiscovery(BaseDirectories.get());
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
        var changeEvent = params.getEvent();

        for (var folder : changeEvent.getRemoved()) {
            workspaceFolders.remove(folder);
            buildServers.computeIfPresent(folder, (f, buildServer) -> {
                buildServer.disconnect();
                return null;
            });
        }

        for (var folder : changeEvent.getAdded()) {
            var buildServer = new BuildServerConnector(server, folder, discovery, launcher);
            buildServers.put(folder, buildServer);
            workspaceFolders.add(folder);
            buildServer.initialise();
        }
    }

    public void setWorkspaceFolders(List<WorkspaceFolder> folders) {
        workspaceFolders.clear();
        workspaceFolders.addAll(folders);
    }

    public CompletableFuture<Void> initialiseBuildServers() {
        return Flux.fromIterable(workspaceFolders).flatMap(folder -> {
            var buildServer = new BuildServerConnector(server, folder, discovery, launcher);
            buildServers.put(folder, buildServer);
            return Mono.fromFuture(buildServer.initialise());
        }).then().toFuture();
    }

    public CompletableFuture<Void> disconnectBuildServers() {
        return Flux.fromIterable(buildServers.values())
                .flatMap(buildServer -> Mono.fromFuture(buildServer.disconnect()))
                .then().toFuture();
    }
}
