package org.mina_lang.langserver;

import org.eclipse.collections.impl.block.function.checked.ThrowingFunction;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mina_lang.BuildInfo;
import org.newsclub.net.unix.AFUNIXServerSocket;
import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public class MinaLanguageServerTest {

    @TempDir
    static Path tempDir;

    static Path socketFile;

    @BeforeEach
    void setup() throws IOException {
        socketFile = tempDir.resolve("test.sock");
        Files.createFile(socketFile);
    }

    @AfterEach
    void teardown() throws IOException {
        Files.delete(socketFile);
    }

    static class TestClient implements LanguageClient {
        private ConcurrentHashMap<String, List<Diagnostic>> diagnosticsMap;

        public List<Diagnostic> getDiagnostics(String uri) {
            return diagnosticsMap.get(uri);
        }

        @Override
        public void telemetryEvent(Object object) {
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            diagnosticsMap.put(diagnostics.getUri(), diagnostics.getDiagnostics());
        }

        @Override
        public void showMessage(MessageParams messageParams) {
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            return null;
        }

        @Override
        public void logMessage(MessageParams message) {
        }
    }

    static void withClientAndServer(InputStream input, OutputStream output,
            BiConsumer<TestClient, LanguageServer> testFunction) {
        var client = new TestClient();

        var launcher = LSPLauncher.createClientLauncher(client, input, output);

        var listening = launcher.startListening();

        var server = launcher.getRemoteProxy();

        try {
            testFunction.accept(client, server);
        } finally {
            listening.cancel(true);
        }
    }

    static CompletableFuture<Integer> withUnixSocket(BiConsumer<TestClient, LanguageServer> testFunction)
            throws IOException {
        try (var serverSocket = AFUNIXServerSocket.bindOn(socketFile, false)) {
            var launcher = new CommandLine(new MinaServerLauncher());
            var exitCode = CompletableFuture.supplyAsync(() -> {
                return launcher.execute(new String[] { "--pipe", socketFile.toString() });
            });
            try (var socket = serverSocket.accept()) {
                withClientAndServer(socket.getInputStream(), socket.getOutputStream(), testFunction);
            }
            return exitCode;
        }
    }

    static CompletableFuture<Integer> withTcpSocket(BiConsumer<TestClient, LanguageServer> testFunction)
            throws IOException {
        try (var serverSocket = new ServerSocket(0)) {
            var launcher = new CommandLine(new MinaServerLauncher());
            var exitCode = CompletableFuture.supplyAsync(() -> {
                return launcher.execute(new String[] { "--socket", String.valueOf(serverSocket.getLocalPort()) });
            });
            try (var socket = serverSocket.accept()) {
                withClientAndServer(socket.getInputStream(), socket.getOutputStream(), testFunction);
            }
            return exitCode;
        }
    }

    static CompletableFuture<Integer> withInOutStreams(BiConsumer<TestClient, LanguageServer> testFunction)
            throws IOException {
        var sysIn = System.in;
        var sysOut = System.out;

        var clientIn = new PipedInputStream();
        var clientOut = new PipedOutputStream();
        var serverIn = new PipedInputStream();
        var serverOut = new PipedOutputStream();

        clientIn.connect(serverOut);
        clientOut.connect(serverIn);

        System.setIn(serverIn);
        System.setOut(new PrintStream(serverOut));

        var launcher = new CommandLine(new MinaServerLauncher());
        var exitCode = CompletableFuture.supplyAsync(() -> {
            return launcher.execute(new String[] { "--stdio" });
        });

        try {
            withClientAndServer(clientIn, clientOut, testFunction);
            return exitCode;
        } finally {
            clientIn.close();
            clientOut.close();
            System.setIn(sysIn);
            System.setOut(sysOut);
        }
    }

    @ParameterizedTest
    @MethodSource("clientServerProvider")
    void testInitialize(
            ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>> clientServerProvider)
            throws Exception {

        var exitCode = clientServerProvider.safeValueOf((client, server) -> {
            var params = new InitializeParams();

            var capabilities = new ClientCapabilities();

            params.setCapabilities(capabilities);
            params.setClientInfo(new ClientInfo("Test Client", BuildInfo.version));
            params.setLocale(Locale.getDefault().toLanguageTag());

            server.initialize(params)
                    .thenAccept(initializeResult -> server.initialized(new InitializedParams()))
                    .thenCompose(v -> server.shutdown())
                    .thenAccept(v -> server.exit())
                    .join();

        });

        assertThat(exitCode.join(), is(0));
    }

    @ParameterizedTest
    @MethodSource("clientServerProvider")
    void testExitBeforeInitialization(
            ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>> clientServerProvider)
            throws Exception {

        var exitCode = clientServerProvider.safeValueOf((client, server) -> {
            server.exit();
        });

        assertThat(exitCode.join(), is(1));
    }

    @ParameterizedTest
    @MethodSource("clientServerProvider")
    void testExitBeforeShutdown(
            ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>> clientServerProvider)
            throws Exception {

        var exitCode = clientServerProvider.safeValueOf((client, server) -> {
            var params = new InitializeParams();

            var capabilities = new ClientCapabilities();

            params.setCapabilities(capabilities);
            params.setClientInfo(new ClientInfo("Test Client", BuildInfo.version));
            params.setLocale(Locale.getDefault().toLanguageTag());

            server.initialize(params)
                    .thenAccept(initializeResult -> server.initialized(new InitializedParams()))
                    .thenRun(server::exit)
                    .join();
        });

        assertThat(exitCode.join(), is(1));
    }

    @ParameterizedTest
    @MethodSource("clientServerProvider")
    void testShutdownBeforeInitialization(
            ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>> clientServerProvider)
            throws Exception {

        var exitCode = clientServerProvider.safeValueOf((client, server) -> {
            var shutdownRequest = server.shutdown();

            var exception = (Throwable) shutdownRequest.exceptionally(ex -> ex).join();

            assertThat(exception, isA(ResponseErrorException.class));
            assertThat(exception.getMessage(), is("Server was not initialized"));

            server.exit();
        });

        assertThat(exitCode.join(), is(1));
    }

    @ParameterizedTest
    @MethodSource("clientServerProvider")
    void testShutdownAfterShutdown(
            ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>> clientServerProvider)
            throws Exception {

        var exitCode = clientServerProvider.safeValueOf((client, server) -> {
            var params = new InitializeParams();

            var capabilities = new ClientCapabilities();

            params.setCapabilities(capabilities);
            params.setClientInfo(new ClientInfo("Test Client", BuildInfo.version));
            params.setLocale(Locale.getDefault().toLanguageTag());

            var duplicateShutdownRequest = server.initialize(params)
                    .thenAccept(initializeResult -> server.initialized(new InitializedParams()))
                    .thenCompose(v -> server.shutdown())
                    .thenCompose(v -> server.shutdown());

            var exception = (Throwable) duplicateShutdownRequest.exceptionally(ex -> ex).join();

            assertThat(exception.getCause(), isA(ResponseErrorException.class));
            assertThat(exception.getCause().getMessage(), is("Server has been shut down"));

            server.exit();
        });

        assertThat(exitCode.join(), is(0));
    }

    static Stream<ThrowingFunction<BiConsumer<TestClient, LanguageServer>, CompletableFuture<Integer>>> clientServerProvider() {
        return Stream.of(
                /*MinaLanguageServerTest::withInOutStreams,*/
                MinaLanguageServerTest::withTcpSocket,
                MinaLanguageServerTest::withUnixSocket);
    }
}
