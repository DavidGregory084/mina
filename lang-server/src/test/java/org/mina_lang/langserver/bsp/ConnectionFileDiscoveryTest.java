/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mina_lang.langserver.BuildInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConnectionFileDiscoveryTest {
    private final String DATA_LOCAL_DIR = "dataLocal";
    private final String DATA_DIR = "data";

    @Test
    void discoversConnectionFilesInWorkspace(@TempDir Path tmpDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        var workspaceFolder = new WorkspaceFolder(tmpDir.toUri().toString(), "tmp");

        var bspDir = tmpDir.resolve(".bsp");

        Files.createDirectories(bspDir);

        Files.write(bspDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));
        Files.write(bspDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3")));
        Files.write(bspDir.resolve("invalid.json"), List.of(""));
        Files.write(bspDir.resolve("foo.json.txt"), List.of(""));
        Files.write(bspDir.resolve("bar.png"), List.of(""));

        var baseDirs = baseDirectories(tmpDir);

        List<BspConnectionDetails> discovered = new ConnectionFileDiscovery(baseDirs)
            .discover(workspaceFolder)
            .collectList()
            .block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, containsInAnyOrder(
            connectionDetails("gradle", "8.2.1"),
            connectionDetails("sbt", "1.9.3")
        ));
    }

    @Test
    void onlyReturnsMinaSupportingConnections(@TempDir Path tmpDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        var workspaceFolder = new WorkspaceFolder(tmpDir.toUri().toString(), "tmp");

        var bspDir = tmpDir.resolve(".bsp");

        Files.createDirectories(bspDir);

        Files.write(bspDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));
        Files.write(bspDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3", List.of("java", "scala"))));
        Files.write(bspDir.resolve("invalid.json"), List.of(""));
        Files.write(bspDir.resolve("foo.json.txt"), List.of(""));
        Files.write(bspDir.resolve("bar.png"), List.of(""));

        var baseDirs = baseDirectories(tmpDir);

        List<BspConnectionDetails> discovered = new ConnectionFileDiscovery(baseDirs)
            .discover(workspaceFolder)
            .collectList()
            .block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, contains(connectionDetails("gradle", "8.2.1")));
    }

    @Test
    void discoversConnectionFilesInDataLocalDir(@TempDir Path tmpDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        var workspaceFolder = new WorkspaceFolder(tmpDir.toUri().toString(), "tmp");

        var bspDir = tmpDir.resolve(DATA_LOCAL_DIR).resolve("bsp");

        Files.createDirectories(bspDir);

        Files.write(bspDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));
        Files.write(bspDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3")));
        Files.write(bspDir.resolve("invalid.json"), List.of(""));
        Files.write(bspDir.resolve("foo.json.txt"), List.of(""));
        Files.write(bspDir.resolve("bar.png"), List.of(""));

        var baseDirs = baseDirectories(tmpDir);

        List<BspConnectionDetails> discovered = new ConnectionFileDiscovery(baseDirs)
            .discover(workspaceFolder)
            .collectList()
            .block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, containsInAnyOrder(
            connectionDetails("gradle", "8.2.1"),
            connectionDetails("sbt", "1.9.3")
        ));
    }

    @Test
    void discoversConnectionFilesInDataDir(@TempDir Path tmpDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        var workspaceFolder = new WorkspaceFolder(tmpDir.toUri().toString(), "tmp");

        var bspDir = tmpDir.resolve(DATA_DIR).resolve("bsp");

        Files.createDirectories(bspDir);

        Files.write(bspDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));
        Files.write(bspDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3")));
        Files.write(bspDir.resolve("invalid.json"), List.of(""));
        Files.write(bspDir.resolve("foo.json.txt"), List.of(""));
        Files.write(bspDir.resolve("bar.png"), List.of(""));

        var baseDirs = baseDirectories(tmpDir);

        List<BspConnectionDetails> discovered = new ConnectionFileDiscovery(baseDirs)
            .discover(workspaceFolder)
            .collectList()
            .block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, containsInAnyOrder(
            connectionDetails("gradle", "8.2.1"),
            connectionDetails("sbt", "1.9.3")
        ));
    }

    @Test
    void prefersWorkspaceConnectionFiles(@TempDir Path tmpDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        var workspaceFolder = new WorkspaceFolder(tmpDir.toUri().toString(), "tmp");

        var workspaceBspDir = tmpDir.resolve(".bsp");
        Files.createDirectories(workspaceBspDir);
        Files.write(workspaceBspDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));

        var dataBspDir = tmpDir.resolve(DATA_DIR).resolve("bsp");
        Files.createDirectories(dataBspDir);
        Files.write(dataBspDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3")));

        var baseDirs = baseDirectories(tmpDir);

        List<BspConnectionDetails> discovered = new ConnectionFileDiscovery(baseDirs)
            .discover(workspaceFolder)
            .collectList()
            .block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, contains(connectionDetails("gradle", "8.2.1")));
    }

    String connectionFile(String name, String version) {
        return connectionFile(name, version, List.of("java", "mina"));
    }

    String connectionFile(String name, String version, List<String> languages) {
        var languageStrings = languages.stream()
            .collect(Collectors.joining("\", \"", "\"", "\""));

        return """
            {
             "name": "%s",
             "version": "%s",
             "bspVersion": "%s",
             "languages": [%s],
             "argv": ["%s", "bsp"]
            }
            """.formatted(name, version, BuildInfo.bspVersion, languageStrings, name);
    }

    BspConnectionDetails connectionDetails(String name, String version) {
        return connectionDetails(name, version, List.of("java", "mina"));
    }

    BspConnectionDetails connectionDetails(String name, String version, List<String> languages) {
        return new BspConnectionDetails(name, List.of(name, "bsp"), version, BuildInfo.bspVersion, languages);
    }

    private BaseDirectories baseDirectories(Path tmpDir) throws IllegalAccessException, NoSuchFieldException {
        var baseDirs = BaseDirectories.get();
        var dataLocalField = BaseDirectories.class.getDeclaredField("dataLocalDir");
        dataLocalField.setAccessible(true);
        dataLocalField.set(baseDirs, tmpDir.resolve(DATA_LOCAL_DIR).toString());
        var dataField = BaseDirectories.class.getDeclaredField("dataDir");
        dataField.setAccessible(true);
        dataField.set(baseDirs, tmpDir.resolve(DATA_DIR).toString());
        return baseDirs;
    }
}
