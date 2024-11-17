/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.ShrinkingMode;
import org.apache.commons.lang3.function.Failable;
import org.junit.jupiter.api.Assertions;
import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.MetaNodePrinter;
import org.mina_lang.syntax.NamespaceNode;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class NamespaceReaderTest {
    private static final MetaNodePrinter<Attributes> printer = new MetaNodePrinter<>();

    private Path createTempDir() throws IOException {
        return Files.createTempDirectory("mina-codegen-reader-test");
    }

    private void clearTempDir(Path tempDir) throws IOException {
        try (var paths = Files.walk(tempDir)) {
            Failable
                .stream(paths.sorted(Comparator.reverseOrder()))
                .forEach(Files::delete);
        }
    }

    // Shrinking doesn't work well with such complex arbitraries
    @Property(shrinking = ShrinkingMode.OFF)
    public void roundTripsArbitraryNamespaces(@ForAll NamespaceNode<Attributes> namespace) throws IOException {
        var originalScope = namespace.getScope();
        var contextLoader = Thread.currentThread().getContextClassLoader();
        var codeGenerator = new CodeGenerator();
        var tempDir = createTempDir();
        try (var urlLoader = URLClassLoader.newInstance(new URL[] { tempDir.toUri().toURL() }, contextLoader)) {
            try {
                codeGenerator.generate(tempDir, namespace);
            } catch (Exception e) {
                System.err.println(namespace.accept(printer).render(80));
                Assertions.fail("Exception while generating code for namespace" + namespace.getName().canonicalName(), e);
            }
            var classpathScope = NamespaceReader.readScope(urlLoader, namespace.getName());
            assertThat(classpathScope, is(equalTo(originalScope)));
        } finally {
            clearTempDir(tempDir);
        }
    }
}
