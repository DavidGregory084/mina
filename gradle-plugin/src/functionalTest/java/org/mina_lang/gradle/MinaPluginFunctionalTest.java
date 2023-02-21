package org.mina_lang.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinaPluginFunctionalTest {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    private File getSourceFile() {
        var sourceFile = new File(projectDir, "src/main/mina/Example.mina");
        sourceFile.getParentFile().mkdirs();
        return sourceFile;
    }

    @Test
    void reportsSuccessfulCompilation() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
                String.join(System.lineSeparator(),
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
                        "}"));

        writeString(getSourceFile(),
                String.join(System.lineSeparator(),
                    "namespace Mina/Examples/Gradle {",
                    "}"));

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build", "--info", "--stacktrace");
        runner.withDebug(true);
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        assertThat(
            result.tasks(TaskOutcome.SUCCESS),
            hasItem(hasProperty("path", equalTo(":compileMina"))));
    }

    @Test
    void reportsFailedCompilation() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
                String.join(System.lineSeparator(),
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
                        "}"));

        writeString(getSourceFile(),
                String.join(System.lineSeparator(),
                    "namespace Mina/Examples/Gradle {",
                    "  ???",
                    "}"));

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build", "--info", "--stacktrace");
        runner.withDebug(true);
        runner.withProjectDir(projectDir);
        BuildResult result = runner.buildAndFail();

        assertThat(
            result.tasks(TaskOutcome.FAILED),
            hasItem(hasProperty("path", equalTo(":compileMina"))));
    }

    @Test
    void reportsNoSource() throws IOException {
        writeString(getSettingsFile(), "");

        writeString(getBuildFile(),
                String.join(System.lineSeparator(),
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
                        "}"));

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build", "--info", "--stacktrace");
        runner.withDebug(true);
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        assertThat(
            result.tasks(TaskOutcome.NO_SOURCE),
            hasItem(hasProperty("path", equalTo(":compileMina"))));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
