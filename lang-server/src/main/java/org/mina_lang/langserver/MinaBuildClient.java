/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.Future;

public class MinaBuildClient implements BuildClient {
    private MinaLanguageServer languageServer;
    private LanguageClient languageClient;
    private BuildServer buildServer;
    private Process buildServerProcess;
    private Future<Void> listenerFuture;

    public MinaBuildClient(MinaLanguageServer languageServer) {
        this.languageServer = languageServer;
        this.languageClient = languageServer.getClient();
    }

    @Override
    public void onBuildShowMessage(ShowMessageParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBuildLogMessage(LogMessageParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBuildTaskStart(TaskStartParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBuildTaskProgress(TaskProgressParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBuildTaskFinish(TaskFinishParams params) {
        // TODO Auto-generated method stub

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

    public void onStartProcess(Process buildServerProcess) {
        this.buildServerProcess = buildServerProcess;
    }

    public void onStartListening(Future<Void> listenerFuture) {
        this.listenerFuture = listenerFuture;
    }

    public void disconnect() {
        this.listenerFuture.cancel(true);
    }
}
