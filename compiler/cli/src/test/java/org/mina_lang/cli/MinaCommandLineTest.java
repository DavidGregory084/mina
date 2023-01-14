package org.mina_lang.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mina_lang.main.Main;

import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

public class MinaCommandLineTest {
    @Test
    public void exitsWithUsageErrorWhenGivenNoArguments() {
        var minaCli = new MinaCommandLine(new Main(new MinaDiagnosticCollector()));
        var exitCode = new CommandLine(minaCli).execute();
        assertEquals(ExitCode.USAGE, exitCode);
    }
}
