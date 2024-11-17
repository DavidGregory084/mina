/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.MissingParameterException;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinaCommandLineTest {
    @Test
    public void exitsWithUsageErrorWhenSourcePathsMissing() {
        var minaCli = new MinaCommandLine();
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

    @Test
    public void setsClasspathFromArgsShortOption() throws MalformedURLException {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("-cp", "./jars", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.classpath(), is(arrayContaining(
            Paths.get("./jars").toUri().toURL()
        )));
    }

    @Test
    public void setsClasspathFromArgsLongOption() throws MalformedURLException {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("--classpath", "./jars", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.classpath(), is(arrayContaining(
            Paths.get("./jars").toUri().toURL()
        )));
    }

    @Test
    public void setsMultipleClasspathEntriesFromArgsShortOption() throws MalformedURLException {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("-cp", "./jars" + File.pathSeparator + "libs/", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.classpath(), is(arrayContaining(
            Paths.get("./jars").toUri().toURL(),
            Paths.get("libs/").toUri().toURL()
        )));
    }

    @Test
    public void setsMultipleClasspathEntriesFromArgsLongOption() throws MalformedURLException {
        var minaCli = new MinaCommandLine();
        var parseResult = new CommandLine(minaCli).parseArgs("-cp", "./jars" + File.pathSeparator + "libs/", "./examples");
        assertThat(parseResult.errors(), is(empty()));
        assertThat(minaCli.classpath(), is(arrayContaining(
            Paths.get("./jars").toUri().toURL(),
            Paths.get("libs/").toUri().toURL()
        )));
    }

    @Test
    public void throwsExceptionWhenClasspathContainsInvalidChars() throws MalformedURLException {
        var minaCli = new MinaCommandLine();
        var exception = assertThrows(
            CommandLine.ParameterException.class,
            () -> new CommandLine(minaCli).parseArgs("--classpath", "./\u0000jars", "./examples"));
        assertThat(
            exception.getMessage(),
            is("Invalid value for option '--classpath': Unable to convert classpath segment to a path: ./\u0000jars"));
    }
}
