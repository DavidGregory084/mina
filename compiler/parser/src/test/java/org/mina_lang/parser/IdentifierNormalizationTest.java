package org.mina_lang.parser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.mina_lang.parser.CompilationUnitParser.DeclarationVisitor;
import org.mina_lang.syntax.LetDeclarationNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class IdentifierNormalizationTest {
    Injector injector = Guice.createInjector();
    CompilationUnitParser parser = injector.getInstance(CompilationUnitParser.class);
    DeclarationVisitor declarationVisitor = injector.getInstance(DeclarationVisitor.class);

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

        var result1 = parser.parse(variant1, errorCollector, declarationVisitor,
                MinaParser::declaration);

        assertThat(result1, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result1).name(), equalTo(expected));

        var result2 = parser.parse(variant2, errorCollector, declarationVisitor,
                MinaParser::declaration);

        assertThat(result2, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result2).name(), equalTo(expected));

        var result3 = parser.parse(variant3, errorCollector, declarationVisitor,
                MinaParser::declaration);

        assertThat(result3, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result3).name(), equalTo(expected));

        var result4 = parser.parse(variant4, errorCollector, declarationVisitor,
                MinaParser::declaration);

        assertThat(result4, instanceOf(LetDeclarationNode.class));
        assertThat(((LetDeclarationNode<Void>) result4).name(), equalTo(expected));
    }
}
