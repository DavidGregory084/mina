/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.Test;
import org.mina_lang.langserver.MinaLanguageServer;
import reactor.core.publisher.Flux;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

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

        withBuildServerConnector(discovery, (connector, langClient) -> {
            assertThat(connector.initialise(), stageWillCompleteWithValue());
            assertThat(langClient.getMessageReceived(), stageWillCompleteWithValueThat(allOf(
                hasProperty("type", equalTo(MessageType.Warning)),
                hasProperty("message", containsString("No build servers with Mina support were found"))
            )));
        });
    }

    @Test
    void showsMessageWhenManyBuildServersFound() throws IOException {
        // Discover several build servers
        var discovery = discovery(Flux.just(
            new BspConnectionDetails("Gradle", List.of(), "8.2.1", "2.1.0", List.of("mina")),
            new BspConnectionDetails("sbt", List.of(), "1.9.3", "2.1.0", List.of("mina"))));

        withBuildServerConnector(discovery, (connector, langClient) -> {
            connector.initialise();

            assertThat(langClient.getMessageRequestReceived(), stageWillCompleteWithValueThat(
                hasProperty("actions", contains(
                    new MessageActionItem("Gradle (8.2.1)"),
                    new MessageActionItem("sbt (1.9.3)")
                ))));
        });
    }

    void withBuildServerConnector(BuildServerDiscovery discovery, BiConsumer<BuildServerConnector, TestLanguageClient> testFunction) throws IOException {
        var buildClientIn = new PipedInputStream();
        var buildClientOut = new PipedOutputStream();
        var buildServerIn = new PipedInputStream();
        var buildServerOut = new PipedOutputStream();

        buildClientIn.connect(buildServerOut);
        buildClientOut.connect(buildServerIn);

        var langClient = new TestLanguageClient();
        var langServer = new MinaLanguageServer();
        langServer.connect(langClient);

        var folder = new WorkspaceFolder("file:///workspace1");
        var launcher = launcher(buildClientIn, buildClientOut);

        var connector = new BuildServerConnector(
            Executors.newSingleThreadExecutor(), langServer, folder, discovery, launcher);

        testFunction.accept(connector, langClient);
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
}
