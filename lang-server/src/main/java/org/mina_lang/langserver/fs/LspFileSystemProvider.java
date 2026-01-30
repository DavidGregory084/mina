/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LspFileSystemProvider extends FileSystemProvider {
    private final ConcurrentHashMap<URI, LspFileSystem> filesystems = new ConcurrentHashMap<>();

    private final FileSystemProvider fileSchemeProvider;

    public FileSystemProvider getFileSchemeProvider() {
        return fileSchemeProvider;
    }

    public LspFileSystemProvider() {
        this.fileSchemeProvider = FileSystems.getDefault().provider();
    }

    @Override
    public String getScheme() {
        return "lsp";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        if (filesystems.containsKey(uri)) throw new FileSystemAlreadyExistsException();
        var filesystem = new LspFileSystem(this, uri, env);
        filesystems.put(uri, filesystem);
        return filesystem;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return filesystems.get(uri);
    }

    @Override
    public Path getPath(URI uri) {
        if (getScheme().equalsIgnoreCase(uri.getScheme())) {

        }

        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return fileSchemeProvider.newByteChannel(path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return fileSchemeProvider.newDirectoryStream(dir, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        fileSchemeProvider.createDirectory(dir, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        fileSchemeProvider.delete(path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        fileSchemeProvider.copy(source, target, options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        fileSchemeProvider.move(source, target, options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return fileSchemeProvider.isSameFile(path, path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return fileSchemeProvider.isHidden(path);
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return fileSchemeProvider.getFileStore(path);
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        fileSchemeProvider.checkAccess(path, modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return fileSchemeProvider.getFileAttributeView(path, type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return fileSchemeProvider.readAttributes(path, type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return fileSchemeProvider.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        fileSchemeProvider.setAttribute(path, attribute, value, options);
    }
}
