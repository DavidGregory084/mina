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

public class CodeGeneratorTest {
    private static final MetaNodePrinter<Attributes> printer = new MetaNodePrinter<>();

    private Path createTempDir() throws IOException {
        return Files.createTempDirectory("mina-codegen-test");
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
    public void generatesArbitraryNamespaces(@ForAll NamespaceNode<Attributes> namespace) throws IOException {
        var contextLoader = Thread.currentThread().getContextClassLoader();
        var codeGenerator = new CodeGenerator();
        var tempDir = createTempDir();
        System.err.println(namespace.accept(printer).render(80));
        try (var urlLoader = URLClassLoader.newInstance(new URL[] { tempDir.toUri().toURL() }, contextLoader)) {
            var namespaceClassName = namespace.getName().canonicalName().replace('/', '.') + ".$namespace";
            codeGenerator.generate(tempDir, namespace);
            try {
                Class.forName(namespaceClassName, true, urlLoader);
            } catch (ClassNotFoundException | LinkageError e) {
                Assertions.fail("Exception while loading compiled namespace " + namespace.getName().canonicalName(), e);
            }
        } finally {
            clearTempDir(tempDir);
        }
    }
}
