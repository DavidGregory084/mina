package org.mina_lang.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
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

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
            "plugins {" +
            "  id('org.mina_lang.gradle')" +
            "}");

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build", "--info", "--stacktrace");
        runner.withDebug(true);
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        assertFalse(result.getOutput().isEmpty());
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
