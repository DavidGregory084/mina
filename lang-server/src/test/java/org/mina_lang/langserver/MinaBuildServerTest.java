/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MinaBuildServerTest {
    @Test
    void findsJsonFiles(@TempDir Path tmpDir) throws IOException {
        Files.write(tmpDir.resolve("gradle.json"), List.of(connectionFile("gradle", "8.2.1")));
        Files.write(tmpDir.resolve("sbt.json"), List.of(connectionFile("sbt", "1.9.3")));
        Files.write(tmpDir.resolve("invalid.json"), List.of(""));
        Files.write(tmpDir.resolve("foo.json.txt"), List.of(""));
        Files.write(tmpDir.resolve("bar.png"), List.of(""));

        List<BspConnectionDetails> discovered = MinaBuildServer.discover(tmpDir).collectList().block();

        assertThat(discovered, is(not(empty())));

        assertThat(discovered, containsInAnyOrder(
            connectionDetails("gradle", "8.2.1"),
            connectionDetails("sbt", "1.9.3")
        ));
    }

    String connectionFile(String name, String version) {
        return """
            {
             "name": "%s",
             "version": "%s",
             "bspVersion": "%s",
             "languages": ["java"],
             "argv": ["%s", "bsp"]
            }
            """.formatted(name, version, BuildInfo.bspVersion, name);
    }

    BspConnectionDetails connectionDetails(String name, String version) {
        return new BspConnectionDetails(name, List.of(name, "bsp"), version, BuildInfo.bspVersion, List.of("java"));
    }
}
