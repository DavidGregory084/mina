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
import org.mina_lang.langserver.semantic.tokens.MinaSemanticTokens;
import org.mina_lang.parser.CompilationUnitParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Singleton
public class MinaLanguageServer implements LanguageServer, LanguageClientAware {
    private Logger logger = LoggerFactory.getLogger(MinaLanguageServer.class);

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

    @Inject
    public MinaLanguageServer(CompilationUnitParser parser) {
        this.documentService = new MinaTextDocumentService(this, parser);
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

    public <A> CompletableFuture<A> ifInitialized(Function<CancelChecker, A> action) {
        if (isInitialized()) {
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
        if (isInitialized()) {
            return CompletableFutures.computeAsync(executor, cancelToken -> {
                return action.apply(cancelToken);
            }).thenCompose(x -> x);
        } else {
            var error = isShutdown()
                    ? new ResponseError(ResponseErrorCode.InvalidRequest, "Server has been shut down", null)
                    : new ResponseError(ResponseErrorCode.ServerNotInitialized, "Server was not initialized", null);
            var result = new CompletableFuture<A>();
            result.completeExceptionally(new ResponseErrorException(error));
            return result;
        }
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
            textDocumentSyncOptions.setChange(TextDocumentSyncKind.Full);
            textDocumentSyncOptions.setSave(true);

            serverCapabilities.setTextDocumentSync(textDocumentSyncOptions);

            var tokenLegend = new SemanticTokensLegend(
                    MinaSemanticTokens.tokenTypes.toList(),
                    MinaSemanticTokens.tokenModifiers.toList());
            var tokenOptions = new SemanticTokensWithRegistrationOptions(tokenLegend, true, false);

            serverCapabilities.setSemanticTokensProvider(tokenOptions);

            cancelToken.checkCanceled();

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
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.exit(1);
            }
        } catch (InterruptedException e) {
            System.exit(1);
        } finally {
            if (isShutdown()) {
                System.exit(0);
            } else {
                System.exit(1);
            }
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
