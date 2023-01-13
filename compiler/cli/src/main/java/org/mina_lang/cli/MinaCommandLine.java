package org.mina_lang.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.mina_lang.BuildInfo;
import org.mina_lang.main.Main;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "minac", mixinStandardHelpOptions = true, version = BuildInfo.version)
public class MinaCommandLine implements Callable<Integer> {

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
            compilerMain.compilePath(paths).join();

            compilerMain
                    .getDiagnosticsCollector()
                    .getDiagnostics()
                    .forEach(diagnostic -> {
                        System.err.println(diagnostic.severity() + " " +
                                (diagnostic.range().start().line() + 1) + ":" +
                                (diagnostic.range().start().character() + 1) +
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
