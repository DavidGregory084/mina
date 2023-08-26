/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.*;
import ch.epfl.scala.bsp4j.MessageType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.Test;
import org.mina_lang.langserver.MinaLanguageServer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

import static com.spotify.hamcrest.future.CompletableFutureMatchers.stageWillCompleteWithValue;
import static com.spotify.hamcrest.future.CompletableFutureMatchers.stageWillCompleteWithValueThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MinaBuildClientTest {
    @Test
    void forwardsBuildMessagesToClient() {
        withBuildAndLanguageClient(((buildClient, langClient) -> {
            buildClient.onBuildShowMessage(new ShowMessageParams(MessageType.INFORMATION, "Hello!"));

            assertThat(langClient.getMessageReceived(), stageWillCompleteWithValueThat(allOf(
                    hasProperty("type", equalTo(org.eclipse.lsp4j.MessageType.Info)),
                    hasProperty("message", equalTo("Hello!"))
            )));
        }));
    }

    @Test
    void forwardsProgressNotificationsToClient() {
        withBuildAndLanguageClient((buildClient, langClient) -> {
            // Build server progress start
            var taskId = new TaskId(UUID.randomUUID().toString());
            var taskStartParams = new TaskStartParams(taskId);
            taskStartParams.setEventTime(1L);
            taskStartParams.setMessage("Starting something");

            // Language server progress start
            var progressBegin = new WorkDoneProgressBegin();
            progressBegin.setTitle("Gradle BSP");
            progressBegin.setMessage("Starting something");

            // Build server progress report
            var taskProgressParams = new TaskProgressParams(taskId);
            taskProgressParams.setEventTime(2L);
            taskProgressParams.setMessage("In the middle of something");

            // Language server progress report
            var progress = new WorkDoneProgressReport();
            progress.setMessage("In the middle of something");

            // Build server progress end
            var taskFinishParams = new TaskFinishParams(taskId, StatusCode.OK);
            taskFinishParams.setEventTime(3L);
            taskFinishParams.setMessage("Finished something");

            // Language server progress end
            var progressEnd = new WorkDoneProgressEnd();
            progressEnd.setMessage("Finished something");

            // Send task start
            buildClient.onBuildTaskStart(taskStartParams);

            // This should create a progress token on the client-side so that a UI element can be shown
            assertThat(langClient.getCreateProgressReceived(), stageWillCompleteWithValue());

            assertThat(langClient.getNotifyProgressReceived(), stageWillCompleteWithValueThat(allOf(
                    hasProperty("token", equalTo(Either.forLeft(taskId.getId()))),
                    hasProperty("value", equalTo(Either.forLeft(progressBegin)))
            )));

            // Send task progress
            buildClient.onBuildTaskProgress(taskProgressParams);

            // Send task end
            buildClient.onBuildTaskFinish(taskFinishParams);

            // Check that we received everything on the client side
            assertThat(langClient.getProgressNotifications(), contains(
                    allOf(
                            hasProperty("token", equalTo(Either.forLeft(taskId.getId()))),
                            hasProperty("value", equalTo(Either.forLeft(progressBegin)))
                    ),
                    allOf(
                            hasProperty("token", equalTo(Either.forLeft(taskId.getId()))),
                            hasProperty("value", equalTo(Either.forLeft(progress)))
                    ),
                    allOf(
                            hasProperty("token", equalTo(Either.forLeft(taskId.getId()))),
                            hasProperty("value", equalTo(Either.forLeft(progressEnd)))
                    )
            ));
        });
    }

    void withBuildAndLanguageClient(BiConsumer<BuildClient, TestLanguageClient> testFunction) {
        var langServer = new MinaLanguageServer();
        var langClient = new TestLanguageClient();

        langServer.connect(langClient);
        langServer.initialized(new InitializedParams());

        var buildClient = new MinaBuildClient(langServer);

        testFunction.accept(buildClient, langClient);
    }

    static class TestLanguageClient implements LanguageClient {
        private final CompletableFuture<MessageParams> messageReceived = new CompletableFuture<>();
        private final CompletableFuture<ShowMessageRequestParams> messageRequestReceived = new CompletableFuture<>();

        private final CompletableFuture<WorkDoneProgressCreateParams> createProgressReceived = new CompletableFuture<>();

        private final CompletableFuture<ProgressParams> notifyProgressReceived = new CompletableFuture<>();

        private final ConcurrentLinkedQueue<ProgressParams> progressNotifications = new ConcurrentLinkedQueue<>();

        CompletableFuture<MessageParams> getMessageReceived() {
            return messageReceived;
        }

        CompletableFuture<WorkDoneProgressCreateParams> getCreateProgressReceived() {
            return createProgressReceived;
        }

        CompletableFuture<ProgressParams> getNotifyProgressReceived() {
            return notifyProgressReceived;
        }

        public ConcurrentLinkedQueue<ProgressParams> getProgressNotifications() {
            return progressNotifications;
        }

        @Override
        public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
            createProgressReceived.complete(params);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyProgress(ProgressParams params) {
            notifyProgressReceived.complete(params);
            progressNotifications.add(params);
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
