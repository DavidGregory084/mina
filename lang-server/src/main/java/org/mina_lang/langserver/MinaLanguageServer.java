/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class MinaLanguageServer implements LanguageServer, LanguageClientAware {
    private static Logger logger = LoggerFactory.getLogger(MinaLanguageServer.class);

    private int exitCode = 0;
    private LanguageClient client;
    private MinaTextDocumentService documentService;
    private MinaWorkspaceService workspaceService;
    private NotebookDocumentService notebookDocumentService;
    private MinaBuildServers buildServers;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private AtomicReference<String> traceValue = new AtomicReference<>(TraceValue.Off);
    private AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();

    private AtomicReference<List<WorkspaceFolder>> workspaceFolders = new AtomicReference<>();

    private ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicLong count = new AtomicLong(0);
        private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        };

        @Override
        public Thread newThread(Runnable runnable) {
            var thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            thread.setName(String.format("mina-langserver-%d", count.getAndIncrement()));
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            return thread;
        }
    };

    private ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public MinaLanguageServer() {
        this.documentService = new MinaTextDocumentService(this);
        this.workspaceService = new MinaWorkspaceService(this);
        this.notebookDocumentService = new MinaNotebookDocumentService(this);
        this.buildServers = new MinaBuildServers(this);
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

    public AtomicReference<List<WorkspaceFolder>> getWorkspaceFolders() {
        return workspaceFolders;
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

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return CompletableFutures.computeAsync(executor, cancelToken -> {
            cancelToken.checkCanceled();

            clientCapabilities.set(params.getCapabilities());

            // Absolutely every intermediate value here can be null
            var hasWorkspaceCapabilities = params.getCapabilities().getWorkspace() != null;
            var hasWorkspaceFolderCapabilities = hasWorkspaceCapabilities && params.getCapabilities().getWorkspace().getWorkspaceFolders() != null;
            var supportsWorkspaceFolders = hasWorkspaceFolderCapabilities && params.getCapabilities().getWorkspace().getWorkspaceFolders();
            var hasWorkspaceFolders = params.getWorkspaceFolders() != null;

            if (supportsWorkspaceFolders && hasWorkspaceFolders) {
                workspaceFolders.set(params.getWorkspaceFolders());
            } else if (params.getRootUri() != null) {
                workspaceFolders.set(List.of(new WorkspaceFolder(params.getRootUri())));
            } else {
                workspaceFolders.set(List.of());
            }

            var serverCapabilities = new ServerCapabilities();

            var textDocumentSyncOptions = new TextDocumentSyncOptions();
            textDocumentSyncOptions.setOpenClose(true);
            textDocumentSyncOptions.setChange(TextDocumentSyncKind.Incremental);
            textDocumentSyncOptions.setSave(true);

            serverCapabilities.setTextDocumentSync(textDocumentSyncOptions);

            var hoverOptions = new HoverOptions();
            hoverOptions.setWorkDoneProgress(false);

            serverCapabilities.setHoverProvider(hoverOptions);

            var workspaceCapabilities = new WorkspaceServerCapabilities();

            var workspaceFoldersOptions = new WorkspaceFoldersOptions();
            workspaceFoldersOptions.setSupported(true);
            workspaceFoldersOptions.setChangeNotifications(false);

            workspaceCapabilities.setWorkspaceFolders(workspaceFoldersOptions);

            serverCapabilities.setWorkspace(workspaceCapabilities);

            Optional.ofNullable(params.getProcessId())
                .flatMap(ProcessHandle::of)
                .ifPresent(processHandle -> {
                    logger.info("Monitoring termination of parent process {}", processHandle.pid());
                    processHandle.onExit().thenRun(this::exit);
                });

            var serverInfo = new ServerInfo("Mina Language Server", BuildInfo.version);

            return buildServers.initialiseBuildServers().thenApply(v -> {
                return new InitializeResult(serverCapabilities, serverInfo);
            });
        }).thenCompose(result -> result);
    }

    @Override
    public void initialized(InitializedParams params) {
        initialized.set(true);
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
        return buildServers.disconnect();
    }

    @Override
    public void exit() {
        try {
            if (!isShutdown()) {
                logger.error("Server exit request received before shutdown request");
                performShutdown();
                exitCode = 1;
            }

            executor.shutdown();

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                exitCode = 1;
            }
        } catch (InterruptedException e) {
            exitCode = 1;
        }
    }

    @Override
    public void setTrace(SetTraceParams params) {
        ifShouldNotify(() -> {
            traceValue.set(params.getValue());
        });
    }

    @Override
    public TextDocumentService getTextDocumentService() {
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
