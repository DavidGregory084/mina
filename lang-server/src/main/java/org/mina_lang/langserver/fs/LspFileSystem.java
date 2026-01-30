/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LspFileSystem extends FileSystem {
    private LspFileSystemProvider provider;
    private URI root;
    private volatile boolean isOpen;

    public LspFileSystem(LspFileSystemProvider provider, URI root, Map<String, ?> env) {
        this.provider = provider;
        this.root = root;
        this.isOpen = true;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        if (isOpen) {
            isOpen = false;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(Paths.get(root));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return FileSystems.getDefault().getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return FileSystems.getDefault().supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return null;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return FileSystems.getDefault().getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return FileSystems.getDefault().newWatchService();
    }
}
