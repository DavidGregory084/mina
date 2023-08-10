/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.*;
import ch.epfl.scala.bsp4j.MessageType;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.mina_lang.langserver.MinaLanguageServer;
import org.mina_lang.langserver.util.Conversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class MinaBuildClient implements BuildClient {
    private static final Logger logger = LoggerFactory.getLogger(MinaBuildClient.class);

    private MinaLanguageServer languageServer;
    private LanguageClient languageClient;

    private BuildServer buildServer;
    private Future<Void> listenerFuture;

    private ConcurrentHashMap<TaskId, CompletableFuture<Either<String, Integer>>> progressReports = new ConcurrentHashMap<>();

    public MinaBuildClient(MinaLanguageServer languageServer) {
        this.languageServer = languageServer;
        this.languageClient = languageServer.getClient();
    }

    public BuildServer buildServer() {
        return buildServer;
    }

    @Override
    public void onBuildShowMessage(ShowMessageParams params) {
        languageClient.showMessage(Conversions.toLspMessageParams(params));
    }

    @Override
    public void onBuildLogMessage(LogMessageParams params) {
        if (params.getType().equals(MessageType.ERROR)) {
            logger.error(params.getMessage());
        } else if (params.getType().equals(MessageType.WARNING)) {
            logger.warn(params.getMessage());
        } else if (params.getType().equals(MessageType.INFORMATION)) {
            logger.info(params.getMessage());
        } else if (params.getType().equals(MessageType.LOG)) {
            logger.debug(params.getMessage());
        }
    }

    @Override
    public void onBuildTaskStart(TaskStartParams params) {
        languageServer.ifShouldNotify(() -> {
            var progressToken = Either.<String, Integer>forLeft(params.getTaskId().getId());
            var progressCreateParams = new WorkDoneProgressCreateParams(progressToken);
            var progressFuture = languageClient.createProgress(progressCreateParams);
            progressReports.put(params.getTaskId(), progressFuture.thenApply(v -> progressToken));
            progressFuture.thenRun(() -> {
                var progressReport = new WorkDoneProgressBegin();
                progressReport.setTitle("Gradle BSP");
                progressReport.setMessage(params.getMessage());
                progressReport.setPercentage(0);
                var progressReportEither = Either.forLeft((WorkDoneProgressNotification) progressReport);
                var progressParams = new ProgressParams(progressToken, progressReportEither);
                languageClient.notifyProgress(progressParams);
            });
        });
    }

    void setProgressPercentage(TaskProgressParams params, WorkDoneProgressReport report) {
        if (params.getProgress() != null && params.getTotal() != null) {
            var progress = params.getProgress().doubleValue();
            var total = params.getTotal().doubleValue();
            var percentage = (progress / total) * 100;
            report.setPercentage((int) Math.round(percentage));
        }
    }

    @Override
    public void onBuildTaskProgress(TaskProgressParams params) {
        languageServer.ifShouldNotify(() -> {
            Optional
                .ofNullable(progressReports.get(params.getTaskId()))
                .ifPresent(future -> {
                    future.thenAccept(progressToken -> {
                        var progressReport = new WorkDoneProgressReport();
                        progressReport.setMessage(params.getMessage());
                        setProgressPercentage(params, progressReport);
                        var progressReportEither = Either.forLeft((WorkDoneProgressNotification) progressReport);
                        var progressParams = new ProgressParams(progressToken, progressReportEither);
                        languageClient.notifyProgress(progressParams);
                    });
                });
        });
    }

    @Override
    public void onBuildTaskFinish(TaskFinishParams params) {
        languageServer.ifShouldNotify(() -> {
            Optional
                .ofNullable(progressReports.get(params.getTaskId()))
                .ifPresent(future -> {
                    progressReports.remove(params.getTaskId());
                    future.thenAccept(progressToken -> {
                        var progressReport = new WorkDoneProgressEnd();
                        progressReport.setMessage(params.getMessage());
                        var progressReportEither = Either.forLeft((WorkDoneProgressNotification) progressReport);
                        var progressParams = new ProgressParams(progressToken, progressReportEither);
                        languageClient.notifyProgress(progressParams);
                    });
                });
        });
    }

    @Override
    public void onBuildPublishDiagnostics(PublishDiagnosticsParams params) {
        languageClient.publishDiagnostics(Conversions.toLspPublishDiagnostics(params));
    }

    @Override
    public void onBuildTargetDidChange(DidChangeBuildTarget params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectWithServer(BuildServer server) {
        this.buildServer = server;
    }

    public void onStartListening(Future<Void> listenerFuture) {
        this.listenerFuture = listenerFuture;
    }

    public CompletableFuture<Void> disconnect() {
        return buildServer.buildShutdown()
            .thenRun(buildServer::onBuildExit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinaBuildClient that = (MinaBuildClient) o;
        return Objects.equals(languageServer, that.languageServer) && Objects.equals(languageClient, that.languageClient) && Objects.equals(buildServer, that.buildServer) && Objects.equals(listenerFuture, that.listenerFuture) && Objects.equals(progressReports, that.progressReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageServer, languageClient, buildServer, listenerFuture, progressReports);
    }

    @Override
    public String toString() {
        return "MinaBuildClient[" +
                "languageServer=" + languageServer +
                ", languageClient=" + languageClient +
                ", buildServer=" + buildServer +
                ", listenerFuture=" + listenerFuture +
                ", progressReports=" + progressReports +
                ']';
    }
}
