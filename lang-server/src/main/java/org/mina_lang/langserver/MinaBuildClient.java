/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.*;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class MinaBuildClient implements BuildClient {
    private MinaLanguageServer languageServer;
    private LanguageClient languageClient;

    private BuildServer buildServer;
    private Process buildServerProcess;
    private Future<Void> listenerFuture;

    private ConcurrentHashMap<TaskId, CompletableFuture<Void>> progressReports = new ConcurrentHashMap<>();

    public MinaBuildClient(MinaLanguageServer languageServer, Process buildServerProcess) {
        this.languageServer = languageServer;
        this.languageClient = languageServer.getClient();
        this.buildServerProcess = buildServerProcess;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onBuildTaskStart(TaskStartParams params) {
        languageServer.ifShouldNotify(() -> {
            var progressToken = Either.<String, Integer>forLeft(params.getTaskId().getId());
            var progressCreateParams = new WorkDoneProgressCreateParams(progressToken);
            var progressFuture = languageClient.createProgress(progressCreateParams);
            progressReports.put(params.getTaskId(), progressFuture);
            progressFuture.thenRun(() -> {
                var progressReport = new WorkDoneProgressBegin();
                progressReport.setTitle(params.getMessage());
                progressReport.setPercentage(0);
                var progressReportEither = Either.forLeft((WorkDoneProgressNotification) progressReport);
                var progressParams = new ProgressParams(progressToken, progressReportEither);
                languageClient.notifyProgress(progressParams);
            });
        });
    }

    @Override
    public void onBuildTaskProgress(TaskProgressParams params) {
        languageServer.ifShouldNotify(() -> {
            Optional
                .ofNullable(progressReports.get(params.getTaskId()))
                .ifPresent(future -> {
                    future.thenRun(() -> {
                        var progressToken = Either.<String, Integer>forLeft(params.getTaskId().getId());
                        var progressReport = new WorkDoneProgressReport();
                        progressReport.setMessage(params.getMessage());
                        progressReport.setPercentage(params.getProgress().intValue());
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
                    future.thenRun(() -> {
                        var progressToken = Either.<String, Integer>forLeft(params.getTaskId().getId());
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
}
