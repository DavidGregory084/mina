/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.*;
import org.mina_lang.langserver.documents.MinaTextDocumentService;
import org.mina_lang.langserver.notebooks.MinaNotebookDocumentService;
import org.mina_lang.langserver.util.DaemonThreadFactory;
import org.mina_lang.langserver.workspace.MinaWorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ExitCode;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class MinaLanguageServer implements LanguageServer, LanguageClientAware {
    private static Logger logger = LoggerFactory.getLogger(MinaLanguageServer.class);

    private int exitCode = ExitCode.OK;
    private LanguageClient client;
    private MinaTextDocumentService documentService;
    private MinaWorkspaceService workspaceService;
    private NotebookDocumentService notebookDocumentService;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private AtomicReference<String> traceValue = new AtomicReference<>(TraceValue.Off);
    private AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();

    private ThreadFactory threadFactory = DaemonThreadFactory.create(logger, "mina-langserver-%d");
    private ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public MinaLanguageServer() {
        this.documentService = new MinaTextDocumentService(this);
        this.workspaceService = new MinaWorkspaceService(this);
        this.notebookDocumentService = new MinaNotebookDocumentService(this);
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public LanguageClient getClient() {
        return client;
    }

    public int getExitCode() {
        return exitCode;
    }

    public <A> CompletableFuture<A> ifInitialized(Function<CancelChecker, A> action) {
        if (isInitialized() && !isShutdown()) {
            return CompletableFutures.computeAsync(executor, action);
        } else {
            var error = isShutdown()
                ? new ResponseError(ResponseErrorCode.InvalidRequest, "Server has been shut down", null)
                : new ResponseError(ResponseErrorCode.ServerNotInitialized, "Server was not initialized", null);
            var result = new CompletableFuture<A>();
            result.completeExceptionally(new ResponseErrorException(error));
            return result;
        }
    }

    public <A> CompletableFuture<A> ifInitializedAsync(Function<CancelChecker, CompletableFuture<A>> action) {
        return ifInitialized(action).thenCompose(x -> x);
    }

    public void ifShouldNotify(Runnable action) {
        if (isInitialized() && !isShutdown()) {
            action.run();
        }
    }

    boolean supportsWorkspaceFolders(ClientCapabilities capabilities) {
        // Absolutely every intermediate value here can be null
        var clientWorkspaceCapabilities = capabilities.getWorkspace();
        var hasWorkspaceCapabilities = clientWorkspaceCapabilities != null;
        var hasWorkspaceFolderCapabilities = hasWorkspaceCapabilities && clientWorkspaceCapabilities.getWorkspaceFolders() != null;
        var supportsWorkspaceFolders = hasWorkspaceFolderCapabilities && clientWorkspaceCapabilities.getWorkspaceFolders();
        return hasWorkspaceCapabilities && clientWorkspaceCapabilities.getWorkspaceFolders();
    }

    void setWorkspaceFolders(InitializeParams params) {
        var supportsWorkspaceFolders = supportsWorkspaceFolders(params.getCapabilities());
        var hasWorkspaceFolders = params.getWorkspaceFolders() != null;
        var hasWorkspaceRoot = params.getRootUri() != null;

        if (supportsWorkspaceFolders && hasWorkspaceFolders) {
            workspaceService.setWorkspaceFolders(params.getWorkspaceFolders());
        } else if (hasWorkspaceRoot) {
            var rootUri = params.getRootUri();
            var workspacePathSegments = URI.create(rootUri).getPath().split("/");
            var workspaceName = workspacePathSegments[workspacePathSegments.length - 1];
            var workspaceRoot = new WorkspaceFolder(rootUri, workspaceName);
            workspaceService.setWorkspaceFolders(List.of(workspaceRoot));
        } else {
            workspaceService.setWorkspaceFolders(List.of());
        }
    }

    ServerCapabilities getServerCapabilities(InitializeParams params) {
        clientCapabilities.set(params.getCapabilities());

        var serverCapabilities = new ServerCapabilities();

        var textDocumentSyncOptions = new TextDocumentSyncOptions();
        textDocumentSyncOptions.setChange(TextDocumentSyncKind.Incremental);
        textDocumentSyncOptions.setOpenClose(Boolean.TRUE);
        textDocumentSyncOptions.setSave(Boolean.TRUE);

        serverCapabilities.setTextDocumentSync(textDocumentSyncOptions);

        var workspaceCapabilities = new WorkspaceServerCapabilities();

        var workspaceFoldersOptions = new WorkspaceFoldersOptions();
        workspaceFoldersOptions.setSupported(Boolean.TRUE);
        workspaceFoldersOptions.setChangeNotifications(Boolean.TRUE);

        workspaceCapabilities.setWorkspaceFolders(workspaceFoldersOptions);

        serverCapabilities.setWorkspace(workspaceCapabilities);

        return serverCapabilities;
    }

    void monitorParentProcess(InitializeParams params) {
        Optional.ofNullable(params.getProcessId())
            .flatMap(ProcessHandle::of)
            .ifPresent(processHandle -> {
                logger.info("Monitoring termination of parent process {}", processHandle.pid());
                processHandle.onExit().thenRun(this::exit);
            });
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return CompletableFutures.computeAsync(executor, cancelToken -> {
            cancelToken.checkCanceled();

            setWorkspaceFolders(params);
            monitorParentProcess(params);

            var serverInfo = new ServerInfo("Mina Language Server", BuildInfo.version);
            var serverCapabilities = getServerCapabilities(params);

            return new InitializeResult(serverCapabilities, serverInfo);
        });
    }

    @Override
    public void initialized(InitializedParams params) {
        initialized.set(true);
        workspaceService.initialiseBuildServers();
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return ifInitializedAsync(cancelToken -> {
            return performShutdown().thenApply(v -> {
                shutdown.set(true);
                return null;
            });
        });
    }

    CompletableFuture<Void> performShutdown() {
        return workspaceService.disconnectBuildServers();
    }

    @Override
    public void exit() {
        try {
            if (!isShutdown()) {
                logger.error("Server exit request received before shutdown request");
                performShutdown().get();
                exitCode = ExitCode.SOFTWARE;
            }

            executor.shutdown();

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                exitCode = ExitCode.SOFTWARE;
            }
        } catch (InterruptedException | ExecutionException e) {
            exitCode = ExitCode.SOFTWARE;
        }
    }

    @Override
    public void setTrace(SetTraceParams params) {
        ifShouldNotify(() -> {
            traceValue.set(params.getValue());
        });
    }

    @Override
    public MinaTextDocumentService getTextDocumentService() {
        return documentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public NotebookDocumentService getNotebookDocumentService() {
        return notebookDocumentService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}
