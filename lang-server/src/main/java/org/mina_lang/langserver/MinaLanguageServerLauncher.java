/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.mina_lang.BuildInfo;
import org.mina_lang.langserver.util.DaemonThreadFactory;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Command(name = "Mina Language Server", version = BuildInfo.version, mixinStandardHelpOptions = true)
public class MinaLanguageServerLauncher implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(MinaLanguageServerLauncher.class);

    @ArgGroup(exclusive = true, multiplicity = "1")
    private Transport transport;

    private static class Transport {
        @Option(names = {"--stdio"}, required = true, description = "Use standard input / output streams.")
        boolean useStdio;

        @Option(names = {"--socket"}, required = true, description = "Use TCP socket on the given port.")
        int socketPort;

        @Option(names = {"--pipe"}, required = true, description = "Use named pipe at the given location.")
        String pipeName;
    }

    public static void main(String[] args) throws IOException {
        var serverLauncher = new MinaLanguageServerLauncher();
        var commandLine = new CommandLine(serverLauncher);
        System.exit(commandLine.execute(args));
    }

    private int listenOn(InputStream in, OutputStream out) throws InterruptedException, ExecutionException {
        var server = new MinaLanguageServer();

        var threadFactory = DaemonThreadFactory.create(logger, "mina-langserver-listener-%d");
        var executor = Executors.newSingleThreadExecutor(threadFactory);

        var launcher = new LSPLauncher.Builder<LanguageClient>()
                .setLocalService(server)
                .setRemoteInterface(LanguageClient.class)
                .setInput(in)
                .setOutput(out)
                .setExecutorService(executor)
                .traceMessages(new PrintWriter(System.err))
                .create();

        server.connect(launcher.getRemoteProxy());

        launcher.startListening().get();

        var exitCode = server.getExitCode();

        logger.info("Server exiting with exit code {}", exitCode);

        return exitCode;
    }

    @Override
    public Integer call() throws IOException, InterruptedException, ExecutionException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        });

        if (transport.useStdio) {
            logger.debug("Using standard input/output streams");
            return listenOn(System.in, System.out);
        } else if (transport.pipeName != null) {
            logger.debug("Using pipe {}", transport.pipeName);
            var sockAddress = AFUNIXSocketAddress.of(new File(transport.pipeName));
            try (Socket socket = AFUNIXSocket.connectTo(sockAddress)) {
                return listenOn(socket.getInputStream(), socket.getOutputStream());
            }
        } else {
            logger.debug("Using local socket {}", transport.socketPort);
            try (Socket socket = new Socket("127.0.0.1", transport.socketPort)) {
                return listenOn(socket.getInputStream(), socket.getOutputStream());
            }
        }
    }
}
