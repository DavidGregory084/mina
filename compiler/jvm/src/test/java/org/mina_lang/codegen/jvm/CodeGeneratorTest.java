/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.commons.lang3.function.Failable;
import org.junit.jupiter.api.Assertions;
import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.NamespaceNode;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class CodeGeneratorTest {
    private static Path tempDir;

    CodeGenerator codeGenerator = new CodeGenerator();

    @BeforeContainer
    static void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("mina-codegen-test");
    }

    @AfterTry
    void clearTempDir() throws IOException {
        try (var paths = Files.walk(tempDir)) {
            Failable
                .stream(paths.sorted(Comparator.reverseOrder()))
                .filter(path -> !path.equals(tempDir))
                .forEach(Files::delete);
        }
    }

    @Property
    public void generatesArbitraryNamespaces(@ForAll NamespaceNode<Attributes> namespace) throws IOException {
        var contextLoader = Thread.currentThread().getContextClassLoader();
        try (var urlLoader = URLClassLoader.newInstance(new URL[] { tempDir.toUri().toURL() }, contextLoader)) {
            var namespaceClassName = namespace.getName().canonicalName().replace('/', '.') + ".$namespace";
            codeGenerator.generate(tempDir, namespace);
            try {
                Class.forName(namespaceClassName, true, urlLoader);
            } catch (Exception e) {
                Assertions.fail("Exception while loading compiled namespace " + namespace.getName().canonicalName(), e);
            }
        }
    }
}
