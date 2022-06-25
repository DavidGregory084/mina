package org.mina_lang.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.mina_lang.BuildInfo;
import org.mina_lang.langserver.MinaLanguageServer;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Mina Language Server", version = BuildInfo.version, mixinStandardHelpOptions = true)
public class Launcher implements Callable<Void> {

    @ArgGroup(exclusive = true, multiplicity = "1")
    private Transport transport;

    private static class Transport {
        @Option(names = { "--stdio" }, required = true, description = "Use standard input / output streams.")
        boolean useStdio;

        @Option(names = { "--socket" }, required = true, description = "Use TCP socket on the given port.")
        int socketPort;

        @Option(names = { "--pipe" }, required = true, description = "Use named pipe at the given location.")
        String pipeName;

        @Override
        public String toString() {
            return "Transport [pipe=" + pipeName + ", socket=" + socketPort + ", useStdio=" + useStdio + "]";
        }
    }

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new Launcher()).execute(args);
        System.exit(exitCode);
    }

    private Void listenOn(InputStream in, OutputStream out) throws InterruptedException, ExecutionException {
        var server = new MinaLanguageServer();
        var errWriter = new PrintWriter(System.err);
        var launcher = LSPLauncher.createServerLauncher(server, in, out, true, errWriter);
        server.connect(launcher.getRemoteProxy());
        return launcher.startListening().get();
    }

    @Override
    public Void call() throws IOException, InterruptedException, ExecutionException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        var logger = LoggerFactory.getLogger(Launcher.class);

        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            logger.error("Uncaught exception in thread " + t.getName(), ex);
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
