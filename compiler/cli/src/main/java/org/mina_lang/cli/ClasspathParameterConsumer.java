/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli;

import org.apache.commons.lang3.function.Failable;
import picocli.CommandLine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Stack;

class ClasspathParameterConsumer implements CommandLine.IParameterConsumer {
    private CommandLine.ParameterException parameterException(CommandLine commandLine, CommandLine.Model.ArgSpec argSpec, String parameterValue, String message, Exception cause) {
        String msg = String.format("Invalid value for option '%s': %s", ((CommandLine.Model.OptionSpec) argSpec).longestName(), message);
        return new CommandLine.ParameterException(commandLine, msg, cause, argSpec, parameterValue);
    }

    @Override
    public void consumeParameters(Stack<String> args, CommandLine.Model.ArgSpec argSpec, CommandLine.Model.CommandSpec commandSpec) {
        var commandLine = commandSpec.commandLine();

        var parameterValue = args.pop();
        var classpathSegments = parameterValue.split(File.pathSeparator);
        var segmentStream = Arrays.stream(classpathSegments);

        var classpathURLs = Failable.stream(segmentStream)
            .map(path -> {
                try {
                    return Paths.get(path).toUri().toURL();
                } catch (InvalidPathException e) {
                    throw parameterException(commandLine, argSpec, parameterValue, "Unable to convert classpath segment to a path: " + path, e);
                } catch (MalformedURLException e) {
                    throw parameterException(commandLine, argSpec, parameterValue, "Unable to convert classpath segment to a valid classpath URL: " + path, e);
                }
            }).stream().toArray(URL[]::new);

        argSpec.setValue(classpathURLs);
    }
}
