package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.mina_lang.BuildInfo;
import org.mina_lang.langserver.semantic.tokens.MinaSemanticTokens;

public class MinaLanguageServer implements LanguageServer, LanguageClientAware {

    private ExecutorService executor = Executors.newCachedThreadPool();

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private LanguageClient client;

    private TextDocumentService documentService = new MinaTextDocumentService(this);

    private WorkspaceService workspaceService = new MinaWorkspaceService(this);

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

    public <A> CompletableFuture<A> ifInitialized(Supplier<CompletableFuture<A>> action) {
        if (isInitialized()) {
            return action.get();
        } if (isShutdown()) {
            var result = new CompletableFuture<A>();
            var error = new ResponseError(ResponseErrorCode.serverNotInitialized, "Server was not initialized", null);
            result.completeExceptionally(new ResponseErrorException(error));
        } else {
            var result = new CompletableFuture<A>();
            var error = new ResponseError(ResponseErrorCode.serverNotInitialized, "Server was not initialized", null);
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

            var serverInfo = new ServerInfo("Mina Language Server", BuildInfo.version);
            return new InitializeResult(serverCapabilities, serverInfo);
        });
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return ifInitialized(() -> {
            return CompletableFutures.computeAsync(executor, cancelToken -> {
                cancelToken.checkCanceled();
                shutdown.set(true);
                return null;
            });
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

    @Override
    public void initialized(InitializedParams params) {
        initialized.set(true);
    }
}
