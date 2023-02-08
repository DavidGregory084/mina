package org.mina_lang.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.io.PrintWriter;

import org.apache.commons.lang3.function.Failable;
import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencastsoftware.yvette.BasicDiagnostic;
import com.opencastsoftware.yvette.handlers.ReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.GraphicalReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.RgbColours;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "minac", description = "The command line interface for the Mina compiler.", version = BuildInfo.version, mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class MinaCommandLine implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(MinaCommandLine.class);

    @Option(names = { "-d", "--destination" }, paramLabel = "path", description = {
            "The destination path for compiled class files.",
            "Defaults to the current directory." })
    private Path destination = Paths.get(".");

    @Parameters(description = { "The source paths from which to compile *.mina files." }, arity = "1..*")
    private Path[] paths;

    @Spec
    private CommandSpec command;

    private Main compilerMain;
    private ReportHandler reportHandler;

    private IExecutionExceptionHandler exceptionHandler = (exc, cmd, result) -> {
        var output = command.commandLine().getOut();
        // TODO: Add an InternalCompilerError diagnostic
        var diagnostic = new BasicDiagnostic(exc.getMessage(), exc.getCause());
        reportHandler.display(diagnostic, output);
        output.println();
        exc.printStackTrace(output);
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
            reportHandler.display(diagnostic, command.commandLine().getOut());
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
        commandLine.setOut(new PrintWriter(System.err));
        commandLine.setExecutionExceptionHandler(minaCli.exceptionHandler());
        System.exit(commandLine.execute(args));
    }
}
