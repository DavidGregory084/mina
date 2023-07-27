/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MinaLanguageServer implements LanguageServer, LanguageClientAware {
    private static Logger logger = LoggerFactory.getLogger(MinaLanguageServer.class);

    private int exitCode = 0;
    private LanguageClient client;
    private MinaTextDocumentService documentService;
    private MinaWorkspaceService workspaceService;
    private NotebookDocumentService notebookDocumentService;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private AtomicReference<String> traceValue = new AtomicReference<>(TraceValue.Off);
    private AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();

    private AtomicReference<List<WorkspaceFolder>> workspaceFolders = new AtomicReference<>();
    private ConcurrentHashMap<WorkspaceFolder, BspConnectionDetails> bspConnectionDetails = new ConcurrentHashMap<>();
    private ConcurrentHashMap<WorkspaceFolder, MinaBuildClient> bspConnections = new ConcurrentHashMap<>();

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
    private Scheduler scheduler = Schedulers.fromExecutor(executor);

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

            return configureBsp().thenApply(v -> {
                return new InitializeResult(serverCapabilities, serverInfo);
            });
        }).thenCompose(result -> result);
    }

    CompletableFuture<Void> configureBsp() {
        return Flux.fromIterable(workspaceFolders.get())
            .flatMap(folder -> {
                return MinaBuildServer
                    .discover(folder)
                    .collectList()
                    .flatMap(connectionDetails -> {
                        return configureBspConnection(folder, connectionDetails);
                    });
            }).then().toFuture();
    }

    Mono<Void> configureBspConnection(WorkspaceFolder folder, List<BspConnectionDetails> connectionDetails) {
        if (connectionDetails.isEmpty()) {
            client.showMessage(new MessageParams(MessageType.Warning, "No build server connection details were found for workspace folder '" + folder.getName() + "'"));
            return Mono.empty();
        } else if (connectionDetails.size() > 1) {
            // Check which connection config the user wants to use
            var connectionOptions = createConnectionOptions(connectionDetails);
            var bspConnectionMessage = chooseBspConnectionMessage(folder, connectionOptions);
            return Mono.fromFuture(client.showMessageRequest(bspConnectionMessage)).flatMap(actionItem -> {
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
            .map(details -> new AbstractMap.SimpleEntry<>(new MessageActionItem("%s (%s)".formatted(details.getName(), details.getVersion())), details))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    ShowMessageRequestParams chooseBspConnectionMessage(WorkspaceFolder folder, Map<MessageActionItem, BspConnectionDetails> connectionOptions) {
        var connectionActionItems = connectionOptions.keySet().stream().toList();

        var messageParams = new ShowMessageRequestParams();
        messageParams.setActions(connectionActionItems);
        messageParams.setType(MessageType.Info);
        messageParams.setMessage("Multiple build server connection details were found. Which one do you wish to use for workspace folder '" + folder.getName() + "'?");

        return messageParams;
    }

    Mono<Void> connectToBuildServer(WorkspaceFolder folder, BspConnectionDetails chosenConnection) {
        bspConnectionDetails.put(folder, chosenConnection);
        try {
            var buildClient = MinaBuildServer.connect(this, folder, chosenConnection);
            bspConnections.put(folder, buildClient);
            return Mono.empty();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void initialized(InitializedParams params) {
        initialized.set(true);

    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return ifInitialized(cancelToken -> {
            performShutdown();
            shutdown.set(true);
            return null;
        });
    }

    void performShutdown() {
        bspConnections.values().forEach(MinaBuildClient::disconnect);
    }

    @Override
    public void exit() {
        try {
            if (!isShutdown()) {
                performShutdown();
                logger.error("Server exit request received before shutdown request");
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
