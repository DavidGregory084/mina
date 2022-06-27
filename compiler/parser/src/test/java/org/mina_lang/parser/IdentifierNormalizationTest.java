package org.mina_lang.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.jupiter.api.Test;
import org.mina_lang.parser.CompilationUnitParser.DeclarationVisitor;
import org.mina_lang.syntax.LetDeclarationNode;

public class IdentifierNormalizationTest {
    @Test
    public void testNormalizesEquivalentIdentifiers() {
        var errorCollector = new ErrorCollector();

        var expected = "vpnTrafficPort";

        // Baseline
        var variant1 = "let vpnTrafficPort = 1";
        // Ligatured 'ff'
        var variant2 = "let vpnTraﬀicPort = 2";
        // Ligatured 'fi'
        var variant3 = "let vpnTrafﬁcPort = 3";
        // Ligatured 'ffi'
        var variant4 = "let vpnTraﬃcPort = 4";

        var result1 = CompilationUnitParser.parse(variant1, errorCollector, DeclarationVisitor.INSTANCE,
                MinaParser::declaration);

        assertThat(result1, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result1).name(), equalTo(expected));

        var result2 = CompilationUnitParser.parse(variant2, errorCollector, DeclarationVisitor.INSTANCE,
                MinaParser::declaration);

        assertThat(result2, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result2).name(), equalTo(expected));

        var result3 = CompilationUnitParser.parse(variant3, errorCollector, DeclarationVisitor.INSTANCE,
                MinaParser::declaration);

        assertThat(result3, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result3).name(), equalTo(expected));

        var result4 = CompilationUnitParser.parse(variant4, errorCollector, DeclarationVisitor.INSTANCE,
                MinaParser::declaration);

        assertThat(result4, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result4).name(), equalTo(expected));
    }
}
