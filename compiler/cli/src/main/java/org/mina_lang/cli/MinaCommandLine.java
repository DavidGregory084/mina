package org.mina_lang.cli;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.function.Failable;
import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencastsoftware.yvette.*;
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
    private GraphicalReportHandler reportHandler;

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
            reportHandler.display(new Diagnostic(diagnostic.message()) {
                @Override
                public String code() {
                    return null;
                }

                @Override
                public Severity severity() {
                    return switch (diagnostic.severity()) {
                        case Error -> Severity.Error;
                        case Warning -> Severity.Warning;
                        case Information -> Severity.Information;
                        case Hint -> Severity.Hint;
                    };
                }

                @Override
                public String help() {
                    return null;
                }

                @Override
                public URI url() {
                    return null;
                }

                @Override
                public SourceCode sourceCode() {
                    return new URISourceCode(diagnostic.location().uri());
                }

                @Override
                public Collection<LabelledRange> labels() {
                    var location = diagnostic.location();
                    var rangeStart = location.range().start();
                    var rangeEnd = location.range().end();

                    return Collections.singletonList(
                            new LabelledRange(
                                    null,
                                    new Position(rangeStart.line(), rangeStart.character()),
                                    new Position(rangeEnd.line(), rangeEnd.character())));
                }
            }, System.err);
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
