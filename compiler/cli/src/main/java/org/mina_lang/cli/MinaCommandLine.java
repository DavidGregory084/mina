package org.mina_lang.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "minac", mixinStandardHelpOptions = true, version = BuildInfo.version)
public class MinaCommandLine implements Callable<Integer> {
    @Option(names = "-d")
    Path destination = Paths.get(".");

    @Parameters()
    Path[] paths;

    @Spec
    CommandSpec command;

    private Main compilerMain;

    public MinaCommandLine(Main compilerMain) {
        this.compilerMain = compilerMain;
    }

    @Override
    public Integer call() throws Exception {
        if (paths == null) {
            command.commandLine().usage(System.err);
            return ExitCode.USAGE;
        } else {
            compilerMain.compilePath(destination, paths).join();

            compilerMain
                    .getMainCollector()
                    .getDiagnostics()
                    .forEach(diagnostic -> {
                        var location = diagnostic.location();
                        var rangeStart = location.range().start();
                        System.err.println(
                                System.lineSeparator() + diagnostic.severity() + " " +
                                        "[" + (location.uri().toString()) + ":" +
                                        (rangeStart.line() + 1) + ":" +
                                        (rangeStart.character() + 1) + "]" +
                                        " " + diagnostic.message());
                    });

            return ExitCode.OK;
        }
    }

    public static void main(String... args) {
        var compilerMain = new Main(new MinaDiagnosticCollector());
        var minaCli = new MinaCommandLine(compilerMain);
        var exitCode = new CommandLine(minaCli).execute(args);
        System.exit(exitCode);
    }
}
