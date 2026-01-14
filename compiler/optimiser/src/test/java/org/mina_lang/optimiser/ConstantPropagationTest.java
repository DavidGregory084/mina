package org.mina_lang.optimiser;

import org.junit.jupiter.api.Test;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.ina.String;
import org.mina_lang.optimiser.constants.Constant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConstantPropagationTest {
    @Test
    void shouldDeriveConstantResultForLiteralBoolean() {
        var propagation = new ConstantPropagation();
        var literal = new Boolean(false);
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralChar() {
         var propagation = new ConstantPropagation();
         var literal = new Char('a');
         var result = propagation.analyseExpression(literal);
         assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralDouble() {
        var propagation = new ConstantPropagation();
        var literal = new Double(1.0);
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralFloat() {
        var propagation = new ConstantPropagation();
        var literal = new Float(1.0F);
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralInt() {
        var propagation = new ConstantPropagation();
        var literal = new Int(1);
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralLong() {
        var propagation = new ConstantPropagation();
        var literal = new Long(1L);
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Test
    void shouldDeriveConstantResultForLiteralString() {
        var propagation = new ConstantPropagation();
        var literal = new String("hello");
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }
}
