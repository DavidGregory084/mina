/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class BuildServerProcessLauncher implements BuildServerLauncher {
    private static Logger logger = LoggerFactory.getLogger(BuildServerProcessLauncher.class);

    private ExecutorService executor;

    public BuildServerProcessLauncher(ExecutorService executor) {
        this.executor = executor;
    }

    void forwardErrorStream(Process process) {
        this.executor.submit(() -> {
            // Forward build server's stderr to ours
            var errorStream = process.getErrorStream();
            var streamReader = new InputStreamReader(errorStream);
            var bufferedReader = new BufferedReader(streamReader);
            bufferedReader.lines().forEach(System.err::println);
        });
    }

    public BuildServerConnection launch(WorkspaceFolder workspaceFolder, BspConnectionDetails details) throws IOException {
        var workspacePath = Paths.get(URI.create(workspaceFolder.getUri()));

        var processBuilder = new ProcessBuilder()
                .command(details.getArgv())
                .directory(workspacePath.toFile());

        var logFolder = System.getProperty("LOG_FOLDER");
        if (logFolder != null) {
            processBuilder.environment().put("LOG_FOLDER", logFolder);
        }

        processBuilder.environment().putAll(System.getenv());

        var separator = System.lineSeparator();
        var argvString = String.join(separator, details.getArgv());

        logger.debug("Launching build server process with arguments:{}{}", separator, argvString);

        var process = processBuilder.start();

        forwardErrorStream(process);

        return new BuildServerProcessConnection(process);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildServerProcessLauncher that = (BuildServerProcessLauncher) o;
        return Objects.equals(executor, that.executor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executor);
    }

    @Override
    public String toString() {
        return "BuildServerProcessLauncher[" +
                "executor=" + executor +
                ']';
    }
}
