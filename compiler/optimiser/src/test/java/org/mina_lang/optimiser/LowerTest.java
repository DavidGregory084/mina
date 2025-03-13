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

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mina_lang.syntax.SyntaxNodes.*;
import static org.mina_lang.testing.ExampleNodes.*;

public class LowerTest {
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
                // let id: [A] { A -> A } = (a: A) -> a
                lower.lowerDeclaration(LET_INT_NODE),
                // let id: [A] { A -> A } = (a: A) -> a
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
            assertThat(
                lower.lowerDeclaration(LET_ID_NODE),
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
            assertThat(
                // -1
                lower.lowerExpr(NEGATE_ONE_NODE),
                // -1
                is(new UnOp(Type.INT, UnaryOp.NEGATE, new Int(1))));
        });
    }

    @Test
    void lowersApplyInc() {
        withLowering(lower -> {
            assertThat(
                // inc(1)
                lower.lowerExpr(APPLY_INC_NODE),
                // inc(1)
                is(new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1)))));
        });
    }

    @Test
    void lowersApplyMagicToInt() {
        withLowering(lower -> {
            assertThat(
                // magic(1)
                lower.lowerExpr(APPLY_MAGIC_INT_NODE),
                // magic(box(1))
                is(new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1))))));
        });
    }

    @Test
    void lowersApplyMagicToString() {
        withLowering(lower -> {
            assertThat(
                // magic("abc")
                lower.lowerExpr(APPLY_MAGIC_STRING_NODE),
                // magic("abc")
                is(new Apply(Type.STRING, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new String("abc")))));
        });
    }

    @Test
    void lowersApplyIdToInt() {
        withLowering(lower -> {
            assertThat(
                // id(1)
                lower.lowerExpr(APPLY_ID_INT_NODE),
                // {
                //   let $0 = id(box(1))
                //   unbox($0)
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(1)))))),
                    new Unbox(new Reference(new SyntheticName(0), Type.INT)))));
        });
    }

    @Test
    void lowersApplyIdToString() {
        withLowering(lower -> {
            assertThat(
                // id("abc")
                lower.lowerExpr(APPLY_ID_STRING_NODE),
                // id("abc")
                is(new Apply(Type.STRING, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new String("abc")))));
        });
    }

    @Test
    void lowersApplyConstPoly() {
        withLowering(lower -> {
            assertThat(
                // const(const(1, 'a'), "b")
                lower.lowerExpr(APPLY_CONST_POLY_NODE),
                // {
                //   let $1 = {
                //     let $0 = const(box(1), box('a'))
                //     unbox($0)
                //   }
                //   let $2 = const(box($1), "b")
                //   unbox($2)
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        // let $1 =
                        new LetAssign(
                            new SyntheticName(1),
                            Type.INT,
                            new Block(
                                Type.INT,
                                Lists.immutable.of(
                                    // let $0 =
                                    new LetAssign(
                                        new SyntheticName(0),
                                        Type.INT,
                                        // const(1, 'a')
                                        new Apply(
                                            Type.INT,
                                            new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                                            Lists.immutable.of(
                                                new Box(new Int(1)),
                                                new Box(new Char('a')))))

                                ),
                                // unbox($0)
                                new Unbox(new Reference(new SyntheticName(0), Type.INT)))),
                        // let $2 =
                        new LetAssign(
                            new SyntheticName(2),
                            Type.INT,
                            // const(box($1), "b")
                            new Apply(
                                Type.INT,
                                new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                                Lists.immutable.of(
                                    new Box(new Reference(new SyntheticName(1), Type.INT)),
                                    new String("b"))))),
                    // unbox($2)
                    new Unbox(new Reference(new SyntheticName(2), Type.INT)))));
        });
    }

    @Test
    void lowersSelectIncWithoutApply() {
        withLowering(lower -> {
            assertThat(
                // 1.inc
            lower.lowerExpr(SELECT_INC_INT_NODE),
                // () -> inc(1)
                is(new Lambda(
                    SELECT_INC_INT_TYPE,
                    // () ->
                    Lists.immutable.empty(),
                    new Apply(
                        Type.INT,
                        new Reference(LET_INC_NAME, LET_INC_TYPE),
                        Lists.immutable.of(new Int(1))))));
        });
    }

    @Test
    void lowersSelectIncWithApply() {
        withLowering(lower -> {
            assertThat(
                // 1.inc()
                lower.lowerExpr(APPLY_SELECT_INC_INT_NODE),
                // inc(1)
                is(new Apply(
                    Type.INT,
                    new Reference(LET_INC_NAME, LET_INC_TYPE),
                    Lists.immutable.of(new Int(1)))));
        });
    }

    @Test
    void lowersSelectMagicWithoutApply() {
        withLowering(lower -> {
            assertThat(
                // 1.magic
                lower.lowerExpr(SELECT_MAGIC_INT_NODE),
                // () -> magic(1)
                is(new Lambda(
                    SELECT_MAGIC_INT_TYPE,
                    // () ->
                    Lists.immutable.empty(),
                    new Apply(
                        Type.INT,
                        new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE),
                        Lists.immutable.of(new Box(new Int(1)))))));
        });
    }

    @Test
    void lowersSelectMagicWithApply() {
        withLowering(lower -> {
            assertThat(
                // 1.magic()
                lower.lowerExpr(APPLY_SELECT_MAGIC_INT_NODE),
                // magic(box(1))
                is(new Apply(
                    Type.INT,
                    new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE),
                    Lists.immutable.of(new Box(new Int(1))))));
        });
    }

    @Test
    void lowersSelectConstWithoutApply() {
        withLowering(lower -> {
            assertThat(
                // 1.const
                lower.lowerExpr(SELECT_CONST_INT_NODE),
                // ($0: B) -> {
                //   let $1 = const(box(1), $0)
                //   unbox($1)
                // }
                is(new Lambda(
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
                        new Unbox(new Reference(new SyntheticName(1), Type.INT))))));
        });
    }

    @Test
    void lowersSelectConstWithApply() {
        withLowering(lower -> {
            assertThat(
                // 1.const('b')
                lower.lowerExpr(APPLY_CHAR_SELECT_CONST_INT_NODE),
                // {
                //   let $0 = const(box(1), box('b'))
                //   unbox($0)
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        // let $0 =
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            // const(box(1), box('b'))
                            new Apply(
                                Type.INT,
                                new Reference(LET_CONST_NAME, LET_CONST_TYPE),
                                Lists.immutable.of(new Box(new Int(1)), new Box(new Char('b')))))),
                    // unbox($0)
                    new Unbox(new Reference(new SyntheticName(0), Type.INT)))));
        });
    }

    @Test
    void lowersUnOpWithIncOperand() {
        withLowering(lower -> {
            assertThat(
                // -inc(1)
                lower.lowerExpr(NEGATE_INC_ONE_NODE),
                // {
                //   let $0 = inc(1)
                //   -$0
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1))))
                    ),
                    new UnOp(Type.INT, UnaryOp.NEGATE, new Reference(new SyntheticName(0), Type.INT)))));
        });
    }

    @Test
    void lowersUnOpWithMagicOperand() {
        withLowering(lower -> {
            assertThat(
                // -magic(1)
                lower.lowerExpr(NEGATE_MAGIC_ONE_NODE),
                // {
                //   let $0 = magic(box(1))
                //   -$0
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1)))))
                    ),
                    new UnOp(Type.INT, UnaryOp.NEGATE, new Reference(new SyntheticName(0), Type.INT)))));
        });
    }

    @Test
    void lowersUnOpWithIdOperand() {
        withLowering(lower -> {
            assertThat(
                // -id(1)
                lower.lowerExpr(NEGATE_ID_ONE_NODE),
                // {
                //   let $1 = {
                //     let $0 = id(box(1))
                //     unbox($0)
                //   }
                //   -$1
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(new SyntheticName(1), Type.INT, new Block(
                            Type.INT,
                            Lists.immutable.of(
                                new LetAssign(
                                    new SyntheticName(0),
                                    Type.INT,
                                    new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(1)))))),
                            new Unbox(new Reference(new SyntheticName(0), Type.INT)))
                        )
                    ),
                    new UnOp(Type.INT, UnaryOp.NEGATE, new Reference(new SyntheticName(1), Type.INT)))));
        });
    }

    @Test
    void lowersBinOpWithValueOperands() {
        withLowering(lower -> {
            assertThat(
                // 1 + 2
                lower.lowerExpr(ONE_PLUS_TWO_NODE),
                // 1 + 2
                is(new BinOp(Type.INT, new Int(1), BinaryOp.ADD, new Int(2))));
        });
    }

    @Test
    void lowersBinOpWithIncOperands() {
        withLowering(lower -> {
            assertThat(
                // inc(1) + inc(2)
                lower.lowerExpr(INC_ONE_PLUS_INC_TWO_NODE),
                // {
                //   let $0 = inc(1)
                //   let $1 = inc(2)
                //   $0 + $1
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(1)))),
                        new LetAssign(
                            new SyntheticName(1),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_INC_NAME, LET_INC_TYPE), Lists.immutable.of(new Int(2))))),
                    new BinOp(Type.INT, new Reference(new SyntheticName(0), Type.INT), BinaryOp.ADD, new Reference(new SyntheticName(1), Type.INT)))));
        });
    }

    @Test
    void lowersBinOpWithMagicOperands() {
        withLowering(lower -> {
            assertThat(
                // magic(1) + magic(2)
                lower.lowerExpr(MAGIC_ONE_PLUS_MAGIC_TWO_NODE),
                // {
                //   let $0 = magic(box(1))
                //   let $1 = magic(box(2))
                //   $0 + $1
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(
                        new LetAssign(
                            new SyntheticName(0),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(1))))),
                        new LetAssign(
                            new SyntheticName(1),
                            Type.INT,
                            new Apply(Type.INT, new Reference(LET_MAGIC_NAME, LET_MAGIC_TYPE), Lists.immutable.of(new Box(new Int(2)))))),
                    new BinOp(Type.INT, new Reference(new SyntheticName(0), Type.INT), BinaryOp.ADD, new Reference(new SyntheticName(1), Type.INT)))));
        });
    }

    @Test
    void lowersBinOpWithIdOperands() {
        withLowering(lower -> {
            // let $1 = {
            //    let $0 = id(box(1))
            //    unbox($0)
            // }
            var letId1 = new LetAssign(new SyntheticName(1), Type.INT, new Block(
                Type.INT,
                Lists.immutable.of(
                    new LetAssign(
                        new SyntheticName(0),
                        Type.INT,
                        new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(1)))))),
                new Unbox(new Reference(new SyntheticName(0), Type.INT))
            ));

            // let $3 = {
            //    let $2 = id(box(2))
            //    unbox($2)
            // }
            var letId2 = new LetAssign(new SyntheticName(3), Type.INT, new Block(
                Type.INT,
                Lists.immutable.of(
                    new LetAssign(
                        new SyntheticName(2),
                        Type.INT,
                        new Apply(Type.INT, new Reference(LET_ID_NAME, LET_ID_TYPE), Lists.immutable.of(new Box(new Int(2)))))),
                new Unbox(new Reference(new SyntheticName(2), Type.INT))
            ));

            assertThat(
                // id(1) + id(2)
                lower.lowerExpr(ID_ONE_PLUS_ID_TWO_NODE),
                // {
                //   let $1 = {
                //     let $0 = id(box(1))
                //     unbox($0)
                //   }
                //   let $3 = {
                //     let $2 = id(box(2))
                //     unbox($2)
                //   }
                //   $1 + $3
                // }
                is(new Block(
                    Type.INT,
                    Lists.immutable.of(letId1, letId2),
                    new BinOp(Type.INT, new Reference(new SyntheticName(1), Type.INT), BinaryOp.ADD, new Reference(new SyntheticName(3), Type.INT)))));
        });
    }
}
