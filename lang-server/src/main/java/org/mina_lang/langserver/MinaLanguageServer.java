package org.mina_lang.langserver;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.*;
import org.mina_lang.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MinaLanguageServer implements LanguageServer, LanguageClientAware {
    private Logger logger = LoggerFactory.getLogger(MinaLanguageServer.class);

    private int exitCode = 0;
    private LanguageClient client;
    private MinaTextDocumentService documentService;
    private MinaWorkspaceService workspaceService;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("mina-langserver-%d")
            .setUncaughtExceptionHandler((t, ex) -> {
                logger.error("Uncaught exception in thread " + t.getName(), ex);
            })
            .build();

    private ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public MinaLanguageServer() {
        this.documentService = new MinaTextDocumentService(this);
        this.workspaceService = new MinaWorkspaceService(this);
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
            return CompletableFutures.computeAsync(executor, cancelToken -> {
                return action.apply(cancelToken);
            });
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

            var serverCapabilities = new ServerCapabilities();

            var textDocumentSyncOptions = new TextDocumentSyncOptions();
            textDocumentSyncOptions.setOpenClose(true);
            textDocumentSyncOptions.setChange(TextDocumentSyncKind.Incremental);
            textDocumentSyncOptions.setSave(true);

            serverCapabilities.setTextDocumentSync(textDocumentSyncOptions);

            cancelToken.checkCanceled();

            Optional.ofNullable(params.getProcessId())
                    .flatMap(ProcessHandle::of)
                    .ifPresent(processHandle -> {
                        logger.info("Monitoring termination of parent process {}", processHandle.pid());
                        processHandle.onExit().thenRun(this::exit);
                    });

            var serverInfo = new ServerInfo("Mina Language Server", BuildInfo.version);

            return new InitializeResult(serverCapabilities, serverInfo);
        });
    }

    @Override
    public void initialized(InitializedParams params) {
        initialized.set(true);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return ifInitialized(cancelToken -> {
            cancelToken.checkCanceled();
            shutdown.set(true);
            return null;
        });
    }

    @Override
    public void exit() {
        try {
            if (!isShutdown()) {
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
    public TextDocumentService getTextDocumentService() {
        return documentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}
