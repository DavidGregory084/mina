/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

public class StringByteChannel implements SeekableByteChannel {
    private volatile boolean isOpen;
    private final ByteBuffer contents;
    private volatile long position;

    public StringByteChannel(String contents) {
        this.contents = ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8));
        this.isOpen = true;
        this.position = 0;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        var startPos = contents.position();

        if (contents.remaining() <= dst.remaining()) {
            dst.put(contents);
        } else {
            while (dst.hasRemaining()) dst.put(contents.get());
        }

        return contents.position() - startPos;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        throw new NonWritableChannelException();
    }

    @Override
    public long position() throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        return this.position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        this.position = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        return contents.capacity();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (!this.isOpen()) throw new ClosedChannelException();
        throw new NonWritableChannelException();
    }

    @Override
    public boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public void close() throws IOException {
        if (this.isOpen) {
            this.isOpen = false;
        }
    }
}
