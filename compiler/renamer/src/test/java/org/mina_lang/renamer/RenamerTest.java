package org.mina_lang.renamer;

import org.junit.jupiter.api.Test;
import org.mina_lang.common.*;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.NamespaceNode;

import static org.mina_lang.syntax.SyntaxNodes.*;

import org.eclipse.collections.api.factory.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;

public class RenamerTest {

    void testSuccessfulRename(
            NameEnvironment environment,
            NamespaceNode<Void> originalNode,
            NamespaceNode<Name> expectedNode) {
        var diagnostics = new ErrorCollector();
        var renamer = new Renamer(diagnostics, environment);
        var renamedNode = renamer.rename(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(renamedNode, is(equalTo(expectedNode)));
    }

    <A extends Name> void testSuccessfulRename(
            NameEnvironment environment,
            MetaNode<Void> originalNode,
            MetaNode<A> expectedNode) {
        var diagnostics = new ErrorCollector();
        var renamer = new Renamer(diagnostics, environment);
        var renamedNode = renamer.rename(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(renamedNode, is(equalTo(expectedNode)));
    }

    @Test
    void renameNamespace() {
        var idNode = nsIdNode(new Range(0, 10, 0, 27), Lists.immutable.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");

        // namespace Mina/Test/Renamer {}
        var originalNode = namespaceNode(Meta.empty(new Range(0, 0, 0, 30)), idNode, Lists.immutable.empty(),
                Lists.immutable.empty());

        var expectedNode = namespaceNode(new Meta<Name>(new Range(0, 0, 0, 30), namespaceName), idNode,
                Lists.immutable.empty(), Lists.immutable.empty());

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameData() {
        var idNode = nsIdNode(new Range(0, 10, 0, 27), Lists.immutable.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var dataName = new DataName(new QualifiedName(namespaceName, "Void"));

        /*
         * namespace Mina/Test/Renamer {
         * data Void {}
         * }
         */
        var originalNode = namespaceNode(
                Meta.empty(new Range(0, 0, 2, 1)), idNode, Lists.immutable.empty(),
                Lists.immutable.of(
                        dataNode(new Range(1, 2, 1, 12), "Void", Lists.immutable.empty(), Lists.immutable.empty())));

        var expectedNode = namespaceNode(
                new Meta<Name>(new Range(0, 0, 2, 1), namespaceName), idNode, Lists.immutable.empty(),
                Lists.immutable.of(dataNode(new Meta<Name>(new Range(1, 2, 1, 12), dataName), "Void",
                        Lists.immutable.empty(), Lists.immutable.empty())));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralBoolean() {
        var originalNode = boolNode(Meta.empty(new Range(0, 0, 0, 4)), true);
        var expectedNode = boolNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), true);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralChar() {
        var originalNode = charNode(Meta.empty(new Range(0, 0, 0, 3)), 'a');
        var expectedNode = charNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), 'a');
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralString() {
        var originalNode = stringNode(Meta.empty(new Range(0, 0, 0, 5)), "abc");
        var expectedNode = stringNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), "abc");
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralInt() {
        var originalNode = intNode(Meta.empty(new Range(0, 0, 0, 1)), 1);
        var expectedNode = intNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), 1);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralLong() {
        var originalNode = longNode(Meta.empty(new Range(0, 0, 0, 2)), 1L);
        var expectedNode = longNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), 1L);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralFloat() {
        var originalNode = floatNode(Meta.empty(new Range(0, 0, 0, 4)), 0.1f);
        var expectedNode = floatNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), 0.1f);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralDouble() {
        var originalNode = doubleNode(Meta.empty(new Range(0, 0, 0, 3)), 0.1);
        var expectedNode = doubleNode(new Meta<>(originalNode.range(), Nameless.INSTANCE), 0.1);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }
}
