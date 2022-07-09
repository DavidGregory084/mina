package org.mina_lang.langserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MinaWorkspaceService implements WorkspaceService {
    private Logger logger = LoggerFactory.getLogger(MinaWorkspaceService.class);

    private MinaLanguageServer server;

    @Inject
    public MinaWorkspaceService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub

    }

}
