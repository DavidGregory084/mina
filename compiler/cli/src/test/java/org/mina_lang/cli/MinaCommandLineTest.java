package org.mina_lang.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.mina_lang.main.Main;

import com.opencastsoftware.yvette.handlers.ToStringReportHandler;

import picocli.CommandLine;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.MissingParameterException;

public class MinaCommandLineTest {
    @Test
    public void exitsWithUsageErrorWhenSourcePathsMissing() {
        var minaCli = new MinaCommandLine(new Main(new MinaDiagnosticCollector()), new ToStringReportHandler());
        var exitCode = new CommandLine(minaCli).execute();
        assertEquals(ExitCode.USAGE, exitCode);
    }

    @Test
    public void throwsErrorWhenSourcePathsMissing() {
        var minaCli = new MinaCommandLine();
        var exception = assertThrows(
                MissingParameterException.class,
                () -> new CommandLine(minaCli).parseArgs());
        assertThat(exception.getMessage(), is(equalTo("Missing required parameter: '<paths>'")));
    }

    @Test
    public void setsSourcePathsFromArgs() {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("./examples", "./more");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.paths(), is(arrayContaining(Paths.get("./examples"), Paths.get("./more"))));
    }

    @Test
    public void setsDefaultDestinationPath() {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.destination(), is(equalTo(Paths.get("."))));
    }

    @Test
    public void setsDestinationPathFromArgsShortOption() {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("-d", "./out", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.destination(), is(equalTo(Paths.get("./out"))));
    }

    @Test
    public void setsDestinationPathFromArgsLongOption() {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("--destination", "./out", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.destination(), is(equalTo(Paths.get("./out"))));
    }
}
