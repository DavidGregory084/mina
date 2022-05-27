package org.mina_lang.langserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class MinaWorkspaceService implements WorkspaceService {

    private MinaLanguageServer server;

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
