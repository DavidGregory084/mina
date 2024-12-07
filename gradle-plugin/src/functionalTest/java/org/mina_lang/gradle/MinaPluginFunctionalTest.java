/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class MinaPluginFunctionalTest {
    @TempDir
    Path projectDir;

    private Path getBuildFile() {
        return projectDir.resolve("build.gradle");
    }

    private Path getSettingsFile() {
        return projectDir.resolve("settings.gradle");
    }

    private Path getJavaTestSourceFile(String name) throws IOException {
        var sourceFile = projectDir.resolve(String.format("src/test/java/Mina/Examples/%s.java", name));
        Files.createDirectories(sourceFile.getParent());
        return sourceFile;
    }

    private Path getMinaSourceFile(String name) throws IOException {
        var sourceFile = projectDir.resolve(String.format("src/main/mina/Mina/Examples/%s.mina", name));
        Files.createDirectories(sourceFile.getParent());
        return sourceFile;
    }

    BuildResult runBuild(GradleRunner runner, Function<GradleRunner, BuildResult> buildFn, String... arguments) {
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments(arguments);
        runner.withDebug(true);
        runner.withProjectDir(projectDir.toFile());
        return buildFn.apply(runner);
    }

    BuildResult runBuild(Function<GradleRunner, BuildResult> buildFn, String... arguments) {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments(arguments);
        runner.withDebug(true);
        runner.withProjectDir(projectDir.toFile());
        return buildFn.apply(runner);
    }

    @Test
    void reportsSuccessfulCompilation() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
            "plugins {",
            "  id('java')",
            "  id('org.mina-lang.gradle')",
            "}",
            "",
            "repositories {",
            "  mavenLocal()",
            "  mavenCentral()",
            "}",
            "",
            "mina {",
            "  minaVersion = \"" + BuildInfo.version + "\"",
            "}");

        writeString(getMinaSourceFile("Gradle"),
            "namespace Mina/Examples/Gradle {",
            "}");

        BuildResult result = runBuild(
            GradleRunner::build,
            "build", "--info", "--stacktrace");

        assertThat(
            result.task(":compileMina").getOutcome(),
            is(equalTo(TaskOutcome.SUCCESS)));
    }

    @Test
    void reportsFailedCompilation() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
            "plugins {",
            "  id('java')",
            "  id('org.mina-lang.gradle')",
            "}",
            "",
            "repositories {",
            "  mavenLocal()",
            "  mavenCentral()",
            "}",
            "",
            "mina {",
            "  minaVersion = \"" + BuildInfo.version + "\"",
            "}");

        writeString(getMinaSourceFile("Gradle"),
            "namespace Mina/Examples/Gradle {",
            "  ???",
            "}");

        BuildResult result = runBuild(
            GradleRunner::buildAndFail,
            "build", "--info", "--stacktrace");

        assertThat(
            result.task(":compileMina").getOutcome(),
            is(equalTo(TaskOutcome.FAILED)));
    }

    @Test
    void reportsNoSource() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
            "plugins {",
            "  id('java')",
            "  id('org.mina-lang.gradle')",
            "}",
            "",
            "repositories {",
            "  mavenLocal()",
            "  mavenCentral()",
            "}",
            "",
            "mina {",
            "  minaVersion = \"" + BuildInfo.version + "\"",
            "}");

        BuildResult result = runBuild(
            GradleRunner::build,
            "build", "--info", "--stacktrace");

        assertThat(
            result.task(":compileMina").getOutcome(),
            is(equalTo(TaskOutcome.NO_SOURCE)));
    }

    @Test
    void cleansStaleClassfiles() throws IOException {
        var runner = GradleRunner.create();

        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
            "plugins {",
            "  id('java')",
            "  id('org.mina-lang.gradle')",
            "}",
            "",
            "repositories {",
            "  mavenLocal()",
            "  mavenCentral()",
            "}",
            "",
            "mina {",
            "  minaVersion = \"" + BuildInfo.version + "\"",
            "}",
            "",
            "testing {",
            "  suites {",
            "    test {",
            "      useJUnitJupiter()",
            "    }",
            "  }",
            "}"
            );

        writeString(getMinaSourceFile("Gradle"),
            "namespace Mina/Examples/Gradle {",
            "  let foo = 1",
            "}");

        // We need at least one other source file as Gradle automatically cleans
        // output directories for source sets with no source files
        writeString(getMinaSourceFile("Foo"),
            "namespace Mina/Examples/Foo {",
            "  let bar = 1",
            "}");

        writeString(getJavaTestSourceFile("GradleTest"),
            "package Mina.Examples;",
            "",
            "import Mina.Examples.Gradle.$namespace;",
            "import org.junit.jupiter.api.Test;",
            "",
            "import static org.junit.jupiter.api.Assertions.*;",
            "",
            "class GradleTest {",
            "  @Test",
            "  void fooEqualsOne() {",
            "    assertEquals(1, $namespace.foo);",
            "  }",
            "}"
            );

        BuildResult beforeDeletionResult = runBuild(
            runner,
            GradleRunner::build,
            "build", "--info", "--stacktrace");

        assertThat(
            beforeDeletionResult.task(":test").getOutcome(),
            is(equalTo(TaskOutcome.SUCCESS)));

        deleteFile(getMinaSourceFile("Gradle"));

        BuildResult afterDeletionResult = runBuild(
            runner,
            GradleRunner::buildAndFail,
            "build", "--info", "--stacktrace");

        assertThat(
            afterDeletionResult.task(":compileTestJava").getOutcome(),
            is(equalTo(TaskOutcome.FAILED)));

        assertThat(
            afterDeletionResult.getOutput(),
            containsString("GradleTest.java:11: error: cannot find symbol"));
    }

    private void deleteFile(Path file) throws IOException {
        Files.delete(file);
    }

    private void writeString(Path file, String... lines) throws IOException {
        Files.write(file, Arrays.asList(lines));
    }
}
