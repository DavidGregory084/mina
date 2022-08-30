package org.mina_lang.langserver;

import org.eclipse.lsp4j.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TextDocumentTest {

    @Test
    void testChangeIdentifier() {
        var uri = "file:///ApplyChange.mina";

        var document = new TextDocumentItem(
                uri, "mina", 1, """
                        namespace Mina/Test/ApplyChange {
                            let foo = baz
                        }
                        """);

        var params = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2), List.of(
                new TextDocumentContentChangeEvent(new Range(new Position(1, 8), new Position(1, 11)), "bar")));

        assertThat(
                TextDocument.applyChanges(document, params),
                is(new TextDocumentItem(uri, "mina", 2, """
                        namespace Mina/Test/ApplyChange {
                            let bar = baz
                        }
                        """)));
    }

    @Test
    void testAddNewLineAfterLineEnd() {
        var uri = "file:///ApplyChange.mina";

        var document = new TextDocumentItem(
                uri, "mina", 1, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                        }
                        """);

        var params = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2), List.of(
                new TextDocumentContentChangeEvent(new Range(new Position(2, 0), new Position(2, 0)),
                        "    let baz = quu\n")));

        assertThat(
                TextDocument.applyChanges(document, params),
                is(new TextDocumentItem(uri, "mina", 2, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                            let baz = quu
                        }
                        """)));
    }

    @Test
    void testAddNewLineBeforeLineEnd() {
        var uri = "file:///ApplyChange.mina";

        var document = new TextDocumentItem(
                uri, "mina", 1, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                        }
                        """);

        var params = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2), List.of(
                new TextDocumentContentChangeEvent(new Range(new Position(1, 17), new Position(1, 17)),
                        "\n    let baz = quu")));

        assertThat(
                TextDocument.applyChanges(document, params),
                is(new TextDocumentItem(uri, "mina", 2, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                            let baz = quu
                        }
                        """)));
    }

    @Test
    void testDeleteLine() {
        var uri = "file:///ApplyChange.mina";

        var document = new TextDocumentItem(
                uri, "mina", 1, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                            let baz = quu
                        }
                        """);

        var params = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2), List.of(
                new TextDocumentContentChangeEvent(new Range(new Position(1, 0), new Position(2, 0)), "")));

        assertThat(
                TextDocument.applyChanges(document, params),
                is(new TextDocumentItem(uri, "mina", 2, """
                        namespace Mina/Test/ApplyChange {
                            let baz = quu
                        }
                        """)));
    }

    @Test
    void testDeleteMultipleLines() {
        var uri = "file:///ApplyChange.mina";

        var document = new TextDocumentItem(
                uri, "mina", 1, """
                        namespace Mina/Test/ApplyChange {
                            let foo = bar
                            let baz = quu
                        }
                        """);

        var params = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2), List.of(
                new TextDocumentContentChangeEvent(new Range(new Position(1, 0), new Position(3, 0)), "")));

        assertThat(
                TextDocument.applyChanges(document, params),
                is(new TextDocumentItem(uri, "mina", 2, """
                        namespace Mina/Test/ApplyChange {
                        }
                        """)));
    }
}
