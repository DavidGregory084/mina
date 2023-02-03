package org.mina_lang.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.function.Failable;
import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencastsoftware.yvette.handlers.ReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.GraphicalReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.RgbColours;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "minac", version = BuildInfo.version, mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class MinaCommandLine implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(MinaCommandLine.class);

    @Option(names = "-d")
    Path destination = Paths.get(".");

    @Parameters()
    Path[] paths;

    @Spec
    CommandSpec command;

    private Main compilerMain;
    private ReportHandler reportHandler;

    public MinaCommandLine(Main compilerMain) {
        this.compilerMain = compilerMain;
        this.reportHandler = GraphicalReportHandler.builder()
                .withColours(ColourSupport.isSupported())
                .withRgbColours(RgbColours.PREFERRED)
                .withUnicode(false)
                .buildFor(System.err);
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
        if (paths == null) {
            command.commandLine().usage(System.err);
            return ExitCode.USAGE;
        } else {
            return compileSourcePaths();
        }
    }

    public static void main(String... args) {
        var compilerMain = new Main(new MinaDiagnosticCollector());
        var minaCli = new MinaCommandLine(compilerMain);
        var exitCode = new CommandLine(minaCli).execute(args);
        System.exit(exitCode);
    }
}
