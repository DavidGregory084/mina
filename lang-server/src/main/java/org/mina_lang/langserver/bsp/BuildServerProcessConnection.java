/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class BuildServerProcessConnection implements BuildServerConnection {
    private final InputStream input;
    private final OutputStream output;

    public BuildServerProcessConnection(Process process) {
        this.input = process.getInputStream();
        this.output = process.getOutputStream();
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildServerProcessConnection that = (BuildServerProcessConnection) o;
        return Objects.equals(input, that.input) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    @Override
    public String toString() {
        return "BuildServerProcessConnection[" +
                "input=" + input +
                ", output=" + output +
                ']';
    }
}
