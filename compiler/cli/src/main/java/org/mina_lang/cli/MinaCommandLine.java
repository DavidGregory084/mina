/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli;

import com.opencastsoftware.yvette.BasicDiagnostic;
import com.opencastsoftware.yvette.handlers.ReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.GraphicalReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.RgbColours;
import org.apache.commons.lang3.function.Failable;
import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "minac", description = "The command line interface for the Mina compiler.", version = BuildInfo.version, mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class MinaCommandLine implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(MinaCommandLine.class);

    @Option(names = { "-d", "--destination" }, paramLabel = "path", description = {
            "The destination path for compiled class files.",
            "Defaults to the current directory." })
    private Path destination = Paths.get(".");

    @Parameters(description = { "The source paths from which to compile *.mina files." }, arity = "1..*")
    private Path[] paths;

    private Main compilerMain;
    private ReportHandler reportHandler;

    private final IExecutionExceptionHandler exceptionHandler = (exc, cmd, result) -> {
        // TODO: Add an InternalCompilerError diagnostic
        var diagnostic = new BasicDiagnostic(exc.getMessage(), exc.getCause());
        reportHandler.display(diagnostic, System.err);
        System.err.println();
        exc.printStackTrace(System.err);
        return ExitCode.SOFTWARE;
    };

    MinaCommandLine() {
        // For unit testing only
    }

    public MinaCommandLine(Main compilerMain, ReportHandler reportHandler) {
        this.compilerMain = compilerMain;
        this.reportHandler = reportHandler;
    }

    public Path destination() {
        return destination;
    }

    public Path[] paths() {
        return paths;
    }

    public IExecutionExceptionHandler exceptionHandler() {
        return exceptionHandler;
    }

    public int compileSourcePaths() throws IOException {
        compilerMain.compileSourcePaths(destination, paths).join();

        var mainCollector = compilerMain.getMainCollector();

        Failable.stream(mainCollector.getDiagnostics()).forEach(diagnostic -> {
            reportHandler.display(diagnostic, System.err);
        });

        return mainCollector.hasErrors() ? ExitCode.SOFTWARE : ExitCode.OK;
    }

    @Override
    public Integer call() throws IOException {
        return compileSourcePaths();
    }

    public static void main(String... args) {
        var compilerMain = new Main(new MinaDiagnosticCollector());
        var reportHandler = GraphicalReportHandler.builder()
                .withColours(ColourSupport.isSupported())
                .withRgbColours(RgbColours.PREFERRED)
                .withUnicode(false)
                .buildFor(System.err);
        var minaCli = new MinaCommandLine(compilerMain, reportHandler);
        var commandLine = new CommandLine(minaCli);
        commandLine.setExecutionExceptionHandler(minaCli.exceptionHandler());
        System.exit(commandLine.execute(args));
    }

    public static Callable<Integer> getCallable(String... args) {
        var compilerMain = new Main(new MinaDiagnosticCollector());
        var minaCli = new MinaCommandLine(compilerMain, (diagnostic, output) -> {});
        new CommandLine(minaCli).parseArgs(args);
        return minaCli;
    }
}
