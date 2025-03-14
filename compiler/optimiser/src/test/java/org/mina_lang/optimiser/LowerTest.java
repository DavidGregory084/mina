/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import net.jqwik.api.Disabled;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.ShrinkingMode;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.SyntheticName;
import org.mina_lang.common.names.SyntheticNameSupply;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.Type;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.ina.String;
import org.mina_lang.syntax.NamespaceNode;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mina_lang.syntax.SyntaxNodes.*;
import static org.mina_lang.testing.ExampleNodes.*;

public class LowerTest {
    private final InaNodePrinter printer = new InaNodePrinter();

    java.lang.String print(InaNode node) {
        return node.accept(printer).render();
    }

    void withLowering(Consumer<Lower> test) {
        var nameSupply = new SyntheticNameSupply();
        var lower = new Lower(nameSupply);
        test.accept(lower);
    }

    @Disabled
    @Property(shrinking = ShrinkingMode.OFF)
    void lowersArbitraryNamespaces(@ForAll NamespaceNode<Attributes> namespace) {
        withLowering(lower -> {
            assertDoesNotThrow(() -> lower.lower(namespace));
        });
    }

    @Test
    void lowersData() {
        withLowering(lower -> {
            assertThat(
                // data List[A] {
                //   case Cons(head: A, tail: List[A])
                //   case Nil()
                // }
                lower.lowerDeclaration(LIST_DATA_NODE),
                is(new Data(
                    LIST_DATA_NAME,
                    Lists.immutable.of(TYPE_VAR_A),
                    Lists.immutable.of(
                        new Constructor(
                            CONS_CONSTRUCTOR_NAME,
                            Lists.immutable.of(
                                new Field(HEAD_FIELD_NAME, TYPE_VAR_A),
                                new Field(TAIL_FIELD_NAME, LIST_A_TYPE))),
                        new Constructor(NIL_CONSTRUCTOR_NAME, Lists.immutable.empty())
                    )))
            );
        });
    }

    @Test
    void lowersLet() {
        withLowering(lower -> {
            assertThat(
                // let one = 1
                lower.lowerDeclaration(LET_INT_NODE),
                // let one = 1
                is(new Let(LET_INT_NAME, Type.INT, new Int(1))));
        });
    }

    @Test
    void lowersLetFn() {
        withLowering(lower -> {
            assertThat(
                // let id[A](a: A): A = a
                lower.lowerDeclaration(LET_FN_ID_NODE),
                // let id: [A] { A -> A } = (a: A) -> a
                is(new Let(
                    LET_ID_NAME,
                    LET_ID_TYPE,
                    new Lambda(
                        LET_ID_TYPE,
                        Lists.immutable.of(new Param(LET_ID_PARAM_NAME, TYPE_VAR_A)),
                        new Reference(LET_ID_PARAM_NAME, TYPE_VAR_A)))));
        });
    }

    @Test
    void lowersLetAndLetFnTheSame() {
        withLowering(lower -> {
            // Both become:
            // let id: [A] { A -> A } = (a: A) -> a
            assertThat(
                // let id: [A] { A -> A } = (a: A) -> a
                lower.lowerDeclaration(LET_ID_NODE),
                // let id[A](a: A): A = a
                is(lower.lowerDeclaration(LET_FN_ID_NODE)));
        });
    }

    @Test
    void lowersBool() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(boolNode(Meta.nameless(Type.BOOLEAN), false)),
                is(new Boolean(false)));
        });
    }

    @Test
    void lowersChar() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(charNode(Meta.nameless(Type.CHAR), 'a')),
                is(new Char('a')));
        });
    }

    @Test
    void lowersInt() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(intNode(Meta.nameless(Type.INT), 1)),
                is(new Int(1)));
        });
    }

    @Test
    void lowersLong() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(longNode(Meta.nameless(Type.LONG), 1L)),
                is(new Long(1L)));
        });
    }

    @Test
    void lowersFloat() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(floatNode(Meta.nameless(Type.FLOAT), 1F)),
                is(new Float(1F)));
        });
    }

    @Test
    void lowersDouble() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(doubleNode(Meta.nameless(Type.DOUBLE), 1D)),
                is(new Double(1D)));
        });
    }

    @Test
    void lowersString() {
        withLowering(lower -> {
            assertThat(
                lower.lowerLiteral(stringNode(Meta.nameless(Type.STRING), "abc")),
                is(new String("abc")));
        });
    }

    @Test
    void lowersUnOpWithValueOperand() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // -1
            var actual = lower.lowerExpr(NEGATE_ONE_NODE, bindings);
            // -1
            var expected = new UnOp(Type.INT, UnaryOp.NEGATE, new Int(1));

            assertThat(bindings, is(empty()));
            assertThat(actual, is(expected));
        });
    }

    @Test
    void lowersApplyInc() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // inc(1)
            var actual = lower.lowerExpr(APPLY_INC_NODE, bindings);
            // inc(1)
            var expected = new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1)));

            assertThat(bindings, is(empty()));
            assertThat(actual, is(expected));
        });
    }

    @Test
    void lowersApplyMagicToInt() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // magic(1)
            var actual = lower.lowerExpr(APPLY_MAGIC_INT_NODE, bindings);
            // magic(box(1))
            var expected = new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1))));

            assertThat(bindings, is(empty()));
            assertThat(actual, is(expected));
        });
    }

    @Test
    void lowersApplyMagicToString() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // magic("abc")
            var actual = lower.lowerExpr(APPLY_MAGIC_STRING_NODE, bindings);
            // magic("abc")
            var expected = new Apply(
                Type.STRING,
                new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE),
                Lists.immutable.of(new String("abc")));

            assertThat(bindings, is(empty()));
            assertThat(actual, is(expected));
        });
    }

    @Test
    void lowersApplyIdToInt() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // id(1)
            var tail = lower.lowerExpr(APPLY_ID_INT_NODE, bindings);

            // let $0 = id(box(1))
            // unbox($0)
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0), Type.INT,
                    new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(1)))))
            );
            var expectedTail = new Unbox(new Reference(new SyntheticName(0), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersApplyIdToString() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // id("abc")
            var tail = lower.lowerExpr(APPLY_ID_STRING_NODE, bindings);

            // id("abc")
            var expectedTail = new Apply(
                Type.STRING,
                new Reference(LET_ID_NAME, LET_ID_TYPE),
                Lists.immutable.of(new String("abc")));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersApplyConstPoly() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // const(const(1, 'a'), "b")
            var tail = lower.lowerExpr(APPLY_CONST_POLY_NODE, bindings);

            // let $0 = const(box(1), box('a'))
            // let $1 = const(box(unbox($0)), "b")
            // unbox($1)
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0), Type.INT,
                    new Apply(
                        Type.INT, new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                        Lists.immutable.of(new Box(new Int(1)), new Box(new Char('a'))))),
                new LetAssign(
                    new SyntheticName(1), Type.INT,
                    new Apply(
                        Type.INT, new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                        Lists.immutable.of(new Box(new Unbox(new Reference(new SyntheticName(0), Type.INT))), new String("b"))))
            );
            var expectedTail = new Unbox(new Reference(new SyntheticName(1), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectIncWithoutApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.inc
            var tail = lower.lowerExpr(SELECT_INC_INT_NODE, bindings);

            // () -> inc(1)
            var expectedTail = new Lambda(
                SELECT_INC_INT_TYPE,
                Lists.immutable.empty(),
                new Apply(
                    Type.INT,
                    new Reference(LET_INC_NAME, LET_INC_TYPE),
                    Lists.immutable.of(new Int(1))));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectIncWithApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.inc()
            var tail = lower.lowerExpr(APPLY_SELECT_INC_INT_NODE, bindings);

            // inc(1)
            var expectedTail = new Apply(
                Type.INT,
                new Reference(LET_INC_NAME, LET_INC_TYPE),
                Lists.immutable.of(new Int(1)));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectMagicWithoutApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.magic
            var tail = lower.lowerExpr(SELECT_MAGIC_INT_NODE, bindings);

            // () -> magic(1)
            var expectedTail = new Lambda(
                SELECT_MAGIC_INT_TYPE,
                // () ->
                Lists.immutable.empty(),
                new Apply(
                    Type.INT,
                    new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE),
                    Lists.immutable.of(new Box(new Int(1)))));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectMagicWithApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.magic()
            var tail = lower.lowerExpr(APPLY_SELECT_MAGIC_INT_NODE, bindings);
            // magic(box(1))
            var expectedTail = new Apply(
                Type.INT,
                new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE),
                Lists.immutable.of(new Box(new Int(1))));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectConstWithoutApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.const
            var tail = lower.lowerExpr(SELECT_CONST_INT_NODE, bindings);

            // ($0: B) -> {
            //   let $1 = const(box(1), $0)
            //   unbox($1)
            // }
            var expectedTail = new Lambda(
                SELECT_CONST_INT_TYPE,
                // ($0: B) ->
                Lists.immutable.of(new Param(new SyntheticName(0), TYPE_VAR_B)),
                new Block(
                    Type.INT,
                    Lists.immutable.of(
                        // let $1 =
                        new LetAssign(
                            new SyntheticName(1),
                            Type.INT,
                            // const(box(1), $0)
                            new Apply(
                                Type.INT,
                                new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                                Lists.immutable.of(new Box(new Int(1)), new Reference(new SyntheticName(0), TYPE_VAR_B))))),
                    // unbox($1)
                    new Unbox(new Reference(new SyntheticName(1), Type.INT))));

            assertThat(bindings, is(empty()));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersSelectConstWithApply() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1.const('b')
            var tail = lower.lowerExpr(APPLY_CHAR_SELECT_CONST_INT_NODE, bindings);

            //   let $0 = const(box(1), box('b'))
            //   unbox($0)
            var expectedBindings = List.of(
                // let $0 =
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    // const(box(1), box('b'))
                    new Apply(
                        Type.INT,
                        new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                        Lists.immutable.of(new Box(new Int(1)), new Box(new Char('b')))))
            );
            var expectedTail = new Unbox(new Reference(new SyntheticName(0), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersUnOpWithIncOperand() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // -inc(1)
            var tail = lower.lowerExpr(NEGATE_INC_ONE_NODE, bindings);

            // let $0 = inc(1)
            // -$0
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1))))
            );
            var expectedTail = new UnOp(Type.INT, UnaryOp.NEGATE, new Reference(new SyntheticName(0), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersUnOpWithMagicOperand() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // -magic(1)
            var tail = lower.lowerExpr(NEGATE_MAGIC_ONE_NODE, bindings);

            // let $0 = magic(box(1))
            // -$0
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1)))))
            );
            var expectedTail = new UnOp(Type.INT, UnaryOp.NEGATE, new Reference(new SyntheticName(0), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersUnOpWithIdOperand() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // -id(1)
            var tail = lower.lowerExpr(NEGATE_ID_ONE_NODE, bindings);

            // let $0: Int = id(box(1))
            // -unbox($0)
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(
                        Type.INT,
                        new Reference(LET_ID_NAME, LET_ID_TYPE),
                        Lists.immutable.of(new Box(new Int(1)))))
            );
            var expectedTail = new UnOp(
                Type.INT,
                UnaryOp.NEGATE,
                new Unbox(new Reference(new SyntheticName(0), Type.INT)));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersBinOpWithValueOperands() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // 1 + 2
            var actual = lower.lowerExpr(ONE_PLUS_TWO_NODE, bindings);
            // 1 + 2
            var expected = new BinOp(Type.INT, new Int(1), BinaryOp.ADD, new Int(2));

            assertThat(bindings, is(empty()));
            assertThat(actual, is(expected));
        });
    }

    @Test
    void lowersBinOpWithIncOperands() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // inc(1) + inc(2)
            var tail = lower.lowerExpr(INC_ONE_PLUS_INC_TWO_NODE, bindings);

            // let $0 = inc(1)
            // let $1 = inc(2)
            // $0 + $1
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1)))),
                new LetAssign(
                    new SyntheticName(1),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(2))))
            );
            var expectedTail = new BinOp(Type.INT, new Reference(new SyntheticName(0), Type.INT), BinaryOp.ADD, new Reference(new SyntheticName(1), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersBinOpWithMagicOperands() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // magic(1) + magic(2)
            var tail = lower.lowerExpr(MAGIC_ONE_PLUS_MAGIC_TWO_NODE, bindings);

            // let $0 = magic(box(1))
            // let $1 = magic(box(2))
            // $0 + $1
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1))))),
                new LetAssign(
                    new SyntheticName(1),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(2)))))
            );
            var expectedTail = new BinOp(
                Type.INT,
                new Reference(new SyntheticName(0), Type.INT),
                BinaryOp.ADD,
                new Reference(new SyntheticName(1), Type.INT));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }

    @Test
    void lowersBinOpWithIdOperands() {
        withLowering(lower -> {
            List<LocalBinding> bindings = Lists.mutable.empty();

            // id(1) + id(2)
            var tail = lower.lowerExpr(ID_ONE_PLUS_ID_TWO_NODE, bindings);

            // let $0 = id(box(1))
            // let $1 = id(box(2))
            // unbox($1) + unbox($3)
            var expectedBindings = List.of(
                new LetAssign(
                    new SyntheticName(0),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(1))))),
                new LetAssign(
                    new SyntheticName(1),
                    Type.INT,
                    new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(2)))))
            );
            var expectedTail = new BinOp(
                Type.INT,
                new Unbox(new Reference(new SyntheticName(0), Type.INT)),
                BinaryOp.ADD,
                new Unbox(new Reference(new SyntheticName(1), Type.INT)));

            assertThat(bindings, is(expectedBindings));
            assertThat(tail, is(expectedTail));
        });
    }
}
