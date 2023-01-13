package org.mina_lang.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mina_lang.main.Main;

import picocli.CommandLine;

public class MinaCommandLineTest {
    @Test
    public void exitsWithErrorWhenGivenNoArguments() {
        var minaCli = new MinaCommandLine(new Main(new MinaDiagnosticCollector()));
        var exitCode = new CommandLine(minaCli).execute();
        assertEquals(1, exitCode);
    }
}
