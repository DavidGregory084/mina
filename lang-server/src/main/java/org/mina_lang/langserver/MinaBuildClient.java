package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

public class MinaBuildClient implements BuildClient {
    private MinaLanguageServer languageServer;
    private LanguageClient languageClient;
    private BuildServer buildServer;

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
        org.eclipse.lsp4j.PublishDiagnosticsParams lspDiagnosticsParams =
            new org.eclipse.lsp4j.PublishDiagnosticsParams(
                params.getTextDocument().getUri(),
                params.getDiagnostics().stream().map(Conversions::toLspDiagnostic).toList());

        languageClient.publishDiagnostics(lspDiagnosticsParams);
    }

    @Override
    public void onBuildTargetDidChange(DidChangeBuildTarget params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectWithServer(BuildServer server) {
        this.buildServer = server;
    }
}
