/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.compiler;

import org.gradle.internal.UncheckedException;
import org.mina_lang.gradle.MinaCompilationException;
import org.mina_lang.gradle.MinaCompileParameters;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class MinaCliCompiler implements MinaCompiler {
    private static final MethodType getCallableMethodType = MethodType.methodType(Callable.class, String[].class);

    @Override
    public void compile(MinaCompileParameters compileParameters) {

        try {
            String destDirString = compileParameters
                .getDestinationDirectory().get()
                .getAsFile().getAbsolutePath();

            Stream<String> sourcePaths = compileParameters
                .getSourceFiles().getFiles()
                .stream()
                .map(File::getAbsolutePath);

            Class<?> compilerClass = Class.forName(
                compileParameters.getCompilerClassName().get());

            MethodHandle getCallableHandle = MethodHandles.publicLookup()
                .findStatic(compilerClass, "getCallable", getCallableMethodType);

            String[] args = Stream.concat(
                List.of("-d", destDirString).stream(),
                sourcePaths).toArray(String[]::new);

            Callable<Integer> compileCallable = (Callable<Integer>) getCallableHandle.invokeExact(args);

            int result = compileCallable.call();

            if (result != 0) {
                throw new MinaCompilationException(
                    "Compilation failed with exit code " + result + ". See log for more details");
            }
        } catch (Throwable e) {
            UncheckedException.throwAsUncheckedException(e);
        }
    }
}
