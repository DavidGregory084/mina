/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.Test;
import org.mina_lang.langserver.BuildInfo;
import org.mina_lang.langserver.MinaLanguageServer;
import reactor.core.publisher.Flux;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static com.spotify.hamcrest.future.CompletableFutureMatchers.stageWillCompleteWithValue;
import static com.spotify.hamcrest.future.CompletableFutureMatchers.stageWillCompleteWithValueThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BuildServerConnectorTest {
    private BuildServerDiscovery discovery(Flux<BspConnectionDetails> connections) {
        return workspaceFolder -> connections;
    }

    private BuildServerLauncher launcher(InputStream input, OutputStream output) {
        return (folder, connection) -> new BuildServerConnection() {
            @Override
            public InputStream input() {
                return input;
            }

            @Override
            public OutputStream output() {
                return output;
            }
        };
    }

    @Test
    void showsWarningWhenNoBuildServersFound() throws IOException {
        // Discover no build servers
        var discovery = discovery(Flux.empty());

        withBuildServerConnector(discovery, (connector, langClient, buildServer) -> {
            assertThat(connector.initialise(), stageWillCompleteWithValue());

            // Tell the user that we couldn't find any relevant build server connection details
            assertThat(langClient.getMessageReceived(), stageWillCompleteWithValueThat(allOf(
                hasProperty("type", equalTo(MessageType.Warning)),
                hasProperty("message", containsString("No build servers with Mina support were found"))
            )));

            // Don't connect to any server
            assertThat(buildServer.getBuildInitializeReceived().isDone(), is(false));

            return null;
        });
    }

    @Test
    void connectsWhenOnlyOneBuildServerFound() throws IOException {
        // Discover a single build server
        var discovery = discovery(Flux.just(
            new BspConnectionDetails("Gradle", List.of(), "8.2.1", "2.1.0", List.of("mina"))
        ));

        withBuildServerConnector(discovery, (connector, langClient, buildServer) -> {
            connector.initialise();

            // Launch and initialise the build server
            assertThat(buildServer.getBuildInitializeReceived(), stageWillCompleteWithValueThat(allOf(
                hasProperty("rootUri", equalTo("file:///workspace")),
                hasProperty("displayName", equalTo("Mina Language Server")),
                hasProperty("version", equalTo(BuildInfo.version)),
                hasProperty("bspVersion", equalTo(BuildInfo.bspVersion))
            )));

            // Don't send any messages to the user
            assertThat(langClient.getMessageReceived().isDone(), is(false));
            assertThat(langClient.getMessageRequestReceived().isDone(), is(false));

            assertThat(connector.disconnect(), stageWillCompleteWithValue());

            // Send a shutdown message to the server
            assertThat(buildServer.getBuildShutdownReceived(), stageWillCompleteWithValue());
            assertThat(buildServer.getBuildExitReceived(), stageWillCompleteWithValue());

            return null;
        });
    }

    @Test
    void showsMessageWhenManyBuildServersFound() throws IOException {
        // Discover several build servers
        var discovery = discovery(Flux.just(
            new BspConnectionDetails("Gradle", List.of(), "8.2.1", "2.1.0", List.of("mina")),
            new BspConnectionDetails("sbt", List.of(), "1.9.3", "2.1.0", List.of("mina"))));

        withBuildServerConnector(discovery, (connector, langClient, buildServer) -> {
            connector.initialise();

            // Tell the user that multiple build server connection details were found and prompt them to choose one
            assertThat(langClient.getMessageRequestReceived(), stageWillCompleteWithValueThat(
                hasProperty("actions", contains(
                    new MessageActionItem("Gradle (8.2.1)"),
                    new MessageActionItem("sbt (1.9.3)")
                ))
            ));

            // Launch and initialise the build server
            assertThat(buildServer.getBuildInitializeReceived(), stageWillCompleteWithValueThat(allOf(
                hasProperty("rootUri", equalTo("file:///workspace")),
                hasProperty("displayName", equalTo("Mina Language Server")),
                hasProperty("version", equalTo(BuildInfo.version)),
                hasProperty("bspVersion", equalTo(BuildInfo.bspVersion))
            )));

            // Don't send any warning messages
            assertThat(langClient.getMessageReceived().isDone(), is(false));

            assertThat(connector.disconnect(), stageWillCompleteWithValue());

            // Send a shutdown message to the server
            assertThat(buildServer.getBuildShutdownReceived(), stageWillCompleteWithValue());
            assertThat(buildServer.getBuildExitReceived(), stageWillCompleteWithValue());

            return null;
        });
    }

    void withBuildServerConnector(BuildServerDiscovery discovery, Function3<BuildServerConnector, TestLanguageClient, TestBuildServer, Void> testFunction) throws IOException {
        var buildClientIn = new PipedInputStream();
        var buildClientOut = new PipedOutputStream();
        var buildServerIn = new PipedInputStream();
        var buildServerOut = new PipedOutputStream();

        buildClientIn.connect(buildServerOut);
        buildClientOut.connect(buildServerIn);

        var langClient = new TestLanguageClient();
        var langServer = new MinaLanguageServer();
        langServer.connect(langClient);

        var folder = new WorkspaceFolder("file:///workspace", "workspace");
        var launcher = launcher(buildClientIn, buildClientOut);

        var connector = new BuildServerConnector(
            Executors.newSingleThreadExecutor(), langServer, folder, discovery, launcher);

        var buildServer = new TestBuildServer();

        var buildServerLauncher = Launcher.createLauncher(
            buildServer, BuildClient.class, buildServerIn, buildServerOut);

        var listener = buildServerLauncher.startListening();

        try {
            testFunction.value(connector, langClient, buildServer);
        } finally {
            listener.cancel(true);
        }
    }

    static class TestLanguageClient implements LanguageClient {
        private final CompletableFuture<MessageParams> messageReceived = new CompletableFuture<>();
        private final CompletableFuture<ShowMessageRequestParams> messageRequestReceived = new CompletableFuture<>();

        CompletableFuture<MessageParams> getMessageReceived() {
            return messageReceived;
        }

        CompletableFuture<ShowMessageRequestParams> getMessageRequestReceived() {
            return messageRequestReceived;
        }

        @Override
        public void telemetryEvent(Object object) {
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            messageReceived.complete(messageParams);
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            messageRequestReceived.complete(requestParams);
            // Choose whatever the first option is
            return CompletableFuture.completedFuture(requestParams.getActions().get(0));
        }

        @Override
        public void logMessage(MessageParams message) {
        }
    }

    static class TestBuildServer implements BuildServer {
        private final CompletableFuture<InitializeBuildParams> buildInitializeReceived = new CompletableFuture<>();

        private final CompletableFuture<Void> buildShutdownReceived = new CompletableFuture<>();
        private final CompletableFuture<Void> buildExitReceived = new CompletableFuture<>();

        public CompletableFuture<InitializeBuildParams> getBuildInitializeReceived() {
            return buildInitializeReceived;
        }

        public CompletableFuture<Void> getBuildShutdownReceived() {
            return buildShutdownReceived;
        }

        public CompletableFuture<Void> getBuildExitReceived() {
            return buildExitReceived;
        }

        @Override
        public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
            buildInitializeReceived.complete(params);

            return CompletableFuture.completedFuture(
                new InitializeBuildResult(
                    "Test Build Server",
                    "1.0.0", "2.1.0",
                    new BuildServerCapabilities()));
        }

        @Override
        public void onBuildInitialized() {

        }

        @Override
        public CompletableFuture<Object> buildShutdown() {
            buildShutdownReceived.complete(null);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onBuildExit() {
            buildExitReceived.complete(null);
        }

        @Override
        public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
            return CompletableFuture.completedFuture(new WorkspaceBuildTargetsResult(List.of()));
        }

        @Override
        public CompletableFuture<Object> workspaceReload() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
            return CompletableFuture.completedFuture(new SourcesResult(List.of()));
        }

        @Override
        public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(InverseSourcesParams params) {
            return CompletableFuture.completedFuture(new InverseSourcesResult(List.of()));
        }

        @Override
        public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(DependencySourcesParams params) {
            return CompletableFuture.completedFuture(new DependencySourcesResult(List.of()));
        }

        @Override
        public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
            return CompletableFuture.completedFuture(new ResourcesResult(List.of()));
        }

        @Override
        public CompletableFuture<OutputPathsResult> buildTargetOutputPaths(OutputPathsParams params) {
            return CompletableFuture.completedFuture(new OutputPathsResult(List.of()));
        }

        @Override
        public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
            return CompletableFuture.completedFuture(new CompileResult(StatusCode.OK));
        }

        @Override
        public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
            return CompletableFuture.completedFuture(new TestResult(StatusCode.OK));
        }

        @Override
        public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
            return CompletableFuture.completedFuture(new RunResult(StatusCode.OK));
        }

        @Override
        public CompletableFuture<DebugSessionAddress> debugSessionStart(DebugSessionParams params) {
            return null;
        }

        @Override
        public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
            return CompletableFuture.completedFuture(new CleanCacheResult(true));
        }

        @Override
        public CompletableFuture<DependencyModulesResult> buildTargetDependencyModules(DependencyModulesParams params) {
            return CompletableFuture.completedFuture(new DependencyModulesResult(List.of()));
        }
    }
}
