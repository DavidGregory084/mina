/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import com.opencastsoftware.yvette.Range;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface ExampleNodes {
    /**
     * The kind of types with no type parameters, kind <code>*</code>.
     */
    Kind KIND_STAR = TypeKind.INSTANCE;

    /**
     * The kind of types with one type parameter, kind <code>* -> *</code>.
     */
    Kind KIND_STAR_TO_STAR = new HigherKind(KIND_STAR, KIND_STAR);

    /**
     * Namespace name <code>Mina/Test/Examples</code>.
     */
    NamespaceName NAMESPACE_NAME = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Examples");

    /**
     * A universally quantified type variable <code>A: *</code>.
     */
    ForAllVar TYPE_VAR_A = new ForAllVar("A", KIND_STAR);

    /**
     * Syntax tree metadata for the universally quantified type variable <code>A: *</code>.
     */
    Meta<Attributes> TYPE_VAR_A_META = Meta.of(new ForAllVarName("A"), KIND_STAR);

    /**
     * A kinded example node for the declaration of a universally quantified type variable <code>A: *</code>.
     */
    ForAllVarNode<Attributes> TYPE_VAR_A_NODE = forAllVarNode(TYPE_VAR_A_META, "A");

    /**
     * A kinded example node for a reference to a universally quantified type variable <code>A: *</code>.
     */
    TypeReferenceNode<Attributes> TYPE_VAR_A_REF_NODE = typeRefNode(TYPE_VAR_A_META, "A");

    /**
     * A universally quantified type variable <code>B: *</code>.
     */
    ForAllVar TYPE_VAR_B = new ForAllVar("B", KIND_STAR);

    /**
     * Syntax tree metadata for the universally quantified type variable <code>B: *</code>.
     */
    Meta<Attributes> TYPE_VAR_B_META = Meta.of(new ForAllVarName("B"), KIND_STAR);

    /**
     * A kinded example node for the declaration of a universally quantified type variable <code>B: *</code>.
     */
    ForAllVarNode<Attributes> TYPE_VAR_B_NODE = forAllVarNode(TYPE_VAR_B_META, "B");

    /**
     * A kinded example node for a reference to a universally quantified type variable <code>B: *</code>.
     */
    TypeReferenceNode<Attributes> TYPE_VAR_B_REF_NODE = typeRefNode(TYPE_VAR_B_META, "B");

    /**
     * Data type name <code>Mina/Test/Examples.List</code>
     */
    DataName LIST_DATA_NAME = new DataName(new QualifiedName(NAMESPACE_NAME, "List"));

    /**
     * Type constructor <code>List: * -> *</code>.
     */
    Type LIST_TYPE = new TypeConstructor(LIST_DATA_NAME.name(), KIND_STAR_TO_STAR);

    /**
     * Syntax tree metadata for the data type <code>Mina/Test/Examples.List</code>.
     */
    Meta<Attributes> LIST_DATA_META = Meta.of(LIST_DATA_NAME, KIND_STAR_TO_STAR);

    /**
     * The type <code>List[A]</code>.
     */
    TypeApply LIST_A_TYPE = new TypeApply(
        LIST_TYPE,
        Lists.immutable.of(TYPE_VAR_A),
        KIND_STAR
    );

    /**
     * Data constructor name <code>Mina/Test/Examples.Cons</code>.
     */
    ConstructorName CONS_CONSTRUCTOR_NAME = new ConstructorName(LIST_DATA_NAME, new QualifiedName(NAMESPACE_NAME, "Cons"));

    /**
     * The type of the data constructor <code>Mina/Test/Examples.Cons</code>.
     * <pre><code>[A] { (A, List[A]) -> List[A] }</code></pre>
     */
    QuantifiedType CONS_CONSTRUCTOR_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_A),
        Type.function(TYPE_VAR_A, LIST_A_TYPE, LIST_A_TYPE),
        KIND_STAR);

    /**
     * Syntax tree metadata for the data constructor <code>Mina/Test/Examples.Cons</code>.
     */
    Meta<Attributes> CONS_CONSTRUCTOR_META = Meta.of(CONS_CONSTRUCTOR_NAME, CONS_CONSTRUCTOR_TYPE);

    FieldName HEAD_FIELD_NAME = new FieldName(CONS_CONSTRUCTOR_NAME, "head");

    FieldName TAIL_FIELD_NAME = new FieldName(CONS_CONSTRUCTOR_NAME, "tail");

    /**
     * A typed and kinded example node for the <code>Cons</code> constructor of a <code>List</code> data type.
     * <pre><code>
     * case Cons(head: A, tail: List[A])
     * </code></pre>
     */
    ConstructorNode<Attributes> CONS_CONSTRUCTOR_NODE =
        constructorNode(
            CONS_CONSTRUCTOR_META,
            "Cons",
            Lists.immutable.of(
                constructorParamNode(
                    Meta.of(HEAD_FIELD_NAME, TYPE_VAR_A),
                    "head",
                    TYPE_VAR_A_REF_NODE),
                constructorParamNode(
                    Meta.of(TAIL_FIELD_NAME, LIST_A_TYPE),
                    "tail",
                    typeApplyNode(
                        Meta.nameless(KIND_STAR),
                        typeRefNode(LIST_DATA_META, "List"),
                        Lists.immutable.of(TYPE_VAR_A_REF_NODE)))),
            Optional.empty());

    /**
     * Data constructor name <code>Mina/Test/Examples.Nil</code>.
     */
    ConstructorName NIL_CONSTRUCTOR_NAME = new ConstructorName(LIST_DATA_NAME, new QualifiedName(NAMESPACE_NAME, "Nil"));

    /**
     * The type of the data constructor <code>Mina/Test/Examples.Nil</code>.
     * <pre><code>[A] { () -> List[A] }</code></pre>
     */
    QuantifiedType NIL_CONSTRUCTOR_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_A),
        Type.function(LIST_A_TYPE),
        KIND_STAR);

    /**
     * Syntax tree metadata for the data constructor <code>Mina/Test/Examples.Nil</code>.
     */
    Meta<Attributes> NIL_CONSTRUCTOR_META = Meta.of(NIL_CONSTRUCTOR_NAME, NIL_CONSTRUCTOR_TYPE);

    /**
     * A typed and kinded example node for the <code>Nil</code> constructor of a <code>List</code> data type.
     * <pre><code>
     * case Nil()
     * </code></pre>
     */
    ConstructorNode<Attributes> NIL_CONSTRUCTOR_NODE = constructorNode(
        NIL_CONSTRUCTOR_META,
        "Nil",
        Lists.immutable.empty(),
        Optional.empty());

    /**
     * A typed and kinded example node for a <code>List</code> data type.
     * <pre><code>
     * data List[A] {
     *     case Cons(head: A, tail: List[A])
     *     case Nil()
     * }
     *  </code></pre>
     */
    DataNode<Attributes> LIST_DATA_NODE = dataNode(
        LIST_DATA_META,
        "List",
        Lists.immutable.of(TYPE_VAR_A_NODE),
        Lists.immutable.of(
            CONS_CONSTRUCTOR_NODE,
            NIL_CONSTRUCTOR_NODE));

    /**
     * Top level let binding name <code>Mina/Test/Examples.one</code>.
     */
    LetName LET_INT_NAME = new LetName(new QualifiedName(NAMESPACE_NAME, "one"));

    /**
     * A typed example node for a simple let binding to an integer literal:
     * <pre><code>
     * let one = 1
     * </code></pre>
     */
    LetNode<Attributes> LET_INT_NODE = letNode(
        Meta.of(LET_INT_NAME, Type.INT),
        "one",
        intNode(Meta.nameless(Type.INT), 1));

    /**
     * Top level let binding name <code>Mina/Test/Examples.id</code>.
     */
    LetName LET_ID_NAME = new LetName(new QualifiedName(NAMESPACE_NAME, "id"));

    /**
     * The type of the identity function <code>Mina/Test/Examples.id</code>.
     * <pre><code>[A] { A -> A }</code></pre>
     */
    QuantifiedType LET_ID_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_A),
        Type.function(TYPE_VAR_A, TYPE_VAR_A),
        KIND_STAR);

    /**
     * Syntax tree metadata for an identity function <code>Mina/Test/Examples.id</code>.
     */
    Meta<Attributes> LET_ID_META = Meta.of(LET_ID_NAME, LET_ID_TYPE);

    /**
     * The local name for the parameter <code>a</code> in an identity function.
     */
    LocalName LET_ID_PARAM_NAME = new LocalName("a", 0);

    /**
     * A kinded example node for the type annotation for an identity function:
     * <pre><code>[A] { A -> A }</code></pre>
     */
    QuantifiedTypeNode<Attributes> LET_ID_TYPE_NODE = quantifiedTypeNode(
        Meta.nameless(KIND_STAR),
        Lists.immutable.of(TYPE_VAR_A_NODE),
        funTypeNode(Meta.nameless(KIND_STAR), Lists.immutable.of(TYPE_VAR_A_REF_NODE), TYPE_VAR_A_REF_NODE));

    /**
     * A typed and kinded example node for a let binding to an identity function:
     * <pre><code>
     * let id: [A] { A -> A } = a -> a
     * </code></pre>
     */
    LetNode<Attributes> LET_ID_NODE = letNode(
        Meta.of(LET_ID_NAME, LET_ID_TYPE),
        "id",
        LET_ID_TYPE_NODE,
        lambdaNode(
            Meta.nameless(LET_ID_TYPE),
            Lists.immutable.of(paramNode(Meta.of(LET_ID_PARAM_NAME, TYPE_VAR_A), "a", TYPE_VAR_A_REF_NODE)),
            refNode(Meta.of(LET_ID_PARAM_NAME, TYPE_VAR_A), "a")));

    /**
     * A typed and kinded example node for a let binding to an identity function, using the function syntax:
     * <pre><code>
     * let id[A](a: A): A = a
     * </code></pre>
     */
    LetFnNode<Attributes> LET_FN_ID_NODE = letFnNode(
        Meta.of(LET_ID_NAME, LET_ID_TYPE),
        "id",
        Lists.immutable.of(TYPE_VAR_A_NODE),
        Lists.immutable.of(paramNode(Meta.of(LET_ID_PARAM_NAME, TYPE_VAR_A), "a", TYPE_VAR_A_REF_NODE)),
        TYPE_VAR_A_REF_NODE,
        refNode(Meta.of(LET_ID_PARAM_NAME, TYPE_VAR_A), "a"));

    /**
     * Top level let binding name <code>Mina/Test/Examples.inc</code>.
     */
    LetName LET_INC_NAME = new LetName(new QualifiedName(NAMESPACE_NAME, "inc"));

    /**
     * The type of an increment function <code>Mina/Test/Examples.inc</code>.
     * <pre><code>Int -> Int</code></pre>
     */
    TypeApply LET_INC_TYPE = Type.function(Type.INT, Type.INT);

    /**
     * Syntax tree metadata for an increment function <code>Mina/Test/Examples.inc</code>.
     */
    Meta<Attributes> LET_INC_META = Meta.of(LET_INC_NAME, LET_INC_TYPE);

    /**
     * Top level let binding name <code>Mina/Test/Examples.magic</code>.
     */
    LetName LET_MAGIC_NAME = new LetName(new QualifiedName(NAMESPACE_NAME, "magic"));

    /**
     * The type of a magic number function <code>Mina/Test/Examples.magic</code>.
     * <pre><code>[A] { A -> Int }</code></pre>
     */
    QuantifiedType LET_MAGIC_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_A),
        Type.function(TYPE_VAR_A, Type.INT),
        KIND_STAR);

    /**
     * Syntax tree metadata for a magic number function <code>Mina/Test/Examples.magic</code>.
     */
    Meta<Attributes> LET_MAGIC_META = Meta.of(LET_MAGIC_NAME, LET_MAGIC_TYPE);

    /**
     * Top level let binding name <code>Mina/Test/Examples.const</code>.
     */
    LetName LET_CONST_NAME = new LetName(new QualifiedName(NAMESPACE_NAME, "const"));

    /**
     * The type of the constant function <code>Mina/Test/Examples.const</code>.
     * <pre><code>[A, B] { (A, B) -> A }</code></pre>
     */
    QuantifiedType LET_CONST_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_A, TYPE_VAR_B),
        Type.function(TYPE_VAR_A, TYPE_VAR_B, TYPE_VAR_A),
        KIND_STAR);

    /**
     * Syntax tree metadata for a constant function <code>Mina/Test/Examples.const</code>.
     */
    Meta<Attributes> LET_CONST_META = Meta.of(LET_CONST_NAME, LET_CONST_TYPE);

    /**
     * A typed example node for the application of an increment function to an integer:
     * <pre><code>
     * inc(1)
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_INC_NODE = applyNode(
        Meta.nameless(Type.INT),
        refNode(LET_INC_META, "inc"),
        Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1)));

    /**
     * A typed example node for the application of a magic number function to an integer:
     * <pre><code>
     * magic(1)
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_MAGIC_INT_NODE = applyNode(
        Meta.nameless(Type.INT),
        refNode(LET_MAGIC_META, "magic"),
        Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1)));

    /**
     * A typed example node for the application of a magic number function to a string:
     * <pre><code>
     * magic("abc")
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_MAGIC_STRING_NODE = applyNode(
        Meta.nameless(Type.STRING),
        refNode(LET_MAGIC_META, "magic"),
        Lists.immutable.of(stringNode(Meta.nameless(Type.STRING), "abc")));

    /**
     * A typed example node for the application of an identity function to an integer:
     * <pre><code>
     * id(1)
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_ID_INT_NODE = applyNode(
        Meta.nameless(Type.INT),
        refNode(LET_ID_META, "id"),
        Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1)));

    /**
     * A typed example node for the application of an identity function to a string:
     * <pre><code>
     * id("abc")
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_ID_STRING_NODE = applyNode(
        Meta.nameless(Type.STRING),
        refNode(LET_ID_META, "id"),
        Lists.immutable.of(stringNode(Meta.nameless(Type.STRING), "abc")));

    /**
     * A typed example node for two differently-instantiated applications of a const function:
     * <pre><code>
     * const(const(1, 'a'), "b")
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_CONST_POLY_NODE = applyNode(
        Meta.nameless(Type.INT),
        refNode(LET_CONST_META, "const"),
        Lists.immutable.of(
            applyNode(
                Meta.nameless(Type.INT),
                refNode(LET_CONST_META, "const"),
                Lists.immutable.of(
                    intNode(Meta.nameless(Type.INT), 1),
                    charNode(Meta.nameless(Type.CHAR), 'a'))),
            stringNode(Meta.nameless(Type.STRING), "b")));

    /**
     * The type of the increment function <code>Mina/Test/Examples.inc</code> after being partially applied with an integer.
     * <pre><code>() -> Int</code></pre>
     */
    TypeApply SELECT_INC_INT_TYPE = Type.function(Type.INT);

    /**
     * A typed example node for the selection of an increment function on an integer:
     * <pre><code>
     * 1.inc
     * </code></pre>
     */
    SelectNode<Attributes> SELECT_INC_INT_NODE = selectNode(
        Meta.nameless(SELECT_INC_INT_TYPE),
        intNode(Meta.nameless(Type.INT), 1),
        refNode(LET_INC_META, "inc"));

    /**
     * A typed example node for the selection and application of an increment function on an integer:
     * <pre><code>
     * 1.inc()
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_SELECT_INC_INT_NODE = applyNode(
        Meta.nameless(Type.INT),
        SELECT_INC_INT_NODE,
        Lists.immutable.empty());

    /**
     * The type of the magic number function <code>Mina/Test/Examples.magic</code> after being partially applied with an integer.
     * <pre><code>() -> Int</code></pre>
     */
    TypeApply SELECT_MAGIC_INT_TYPE = Type.function(Type.INT);

    /**
     * A typed example node for the selection of an increment function on an integer:
     * <pre><code>
     * 1.magic
     * </code></pre>
     */
    SelectNode<Attributes> SELECT_MAGIC_INT_NODE = selectNode(
        Meta.nameless(SELECT_MAGIC_INT_TYPE),
        intNode(Meta.nameless(Type.INT), 1),
        refNode(LET_MAGIC_META, "magic"));

    /**
     * A typed example node for the selection and application of an increment function on an integer:
     * <pre><code>
     * 1.magic()
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_SELECT_MAGIC_INT_NODE = applyNode(
        Meta.nameless(Type.INT),
        SELECT_MAGIC_INT_NODE,
        Lists.immutable.empty());

    /**
     * The type of the constant function <code>Mina/Test/Examples.const</code> after being partially applied with an integer.
     * <pre><code>[B] { B -> Int }</code></pre>
     */
    QuantifiedType SELECT_CONST_INT_TYPE = new QuantifiedType(
        Lists.immutable.of(TYPE_VAR_B),
        Type.function(TYPE_VAR_B, Type.INT),
        KIND_STAR);

    /**
     * A typed example node for the selection of a const function on an integer:
     * <pre><code>
     * 1.const
     * </code></pre>
     * This code could not actually typecheck in Mina source code without an enclosing type annotation, e.g.:
     * <pre><code>
     * let constOne: [B] { B -> Int } = 1.const
     * </code></pre>
     */
    SelectNode<Attributes> SELECT_CONST_INT_NODE = selectNode(
        Meta.nameless(SELECT_CONST_INT_TYPE),
        intNode(Meta.nameless(Type.INT), 1),
        refNode(LET_CONST_META, "const"));

    /**
     * A typed example node for the application of a char to the selection of a const function on an integer:
     * <pre><code>
     * 1.const('b')
     * </code></pre>
     */
    ApplyNode<Attributes> APPLY_CHAR_SELECT_CONST_INT_NODE = applyNode(
        Meta.nameless(Type.INT),
        SELECT_CONST_INT_NODE,
        Lists.immutable.of(charNode(Meta.nameless(Type.CHAR), 'b')));

    /**
     * A typed example node for a negation expression:
     * <pre><code>
     * -1
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        intNode(Meta.nameless(Type.INT), 1));

    /**
     * A typed example node for a negation expression that calls increment on its operand:
     * <pre><code>
     * -inc(1)
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_INC_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        applyNode(Meta.nameless(Type.INT), refNode(LET_INC_META, "inc"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))));

    /**
     * A typed example node for a negation expression that calls magic on its operand:
     * <pre><code>
     * -magic(1)
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_MAGIC_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        applyNode(Meta.nameless(Type.INT), refNode(LET_MAGIC_META, "magic"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))));

    /**
     * A typed example node for a negation expression that calls identity on its operand:
     * <pre><code>
     * -id(1)
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_ID_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))));

    /**
     * A typed example node for an addition expression:
     * <pre><code>
     * 1 + 2
     * </code></pre>
     */
    BinaryOpNode<Attributes> ONE_PLUS_TWO_NODE = binaryOpNode(
        Meta.nameless(Type.INT),
        intNode(Meta.nameless(Type.INT), 1),
        BinaryOp.ADD,
        intNode(Meta.nameless(Type.INT), 2));

    /**
     * A typed example node for an addition expression that calls increment on its operands:
     * <pre><code>
     * inc(1) + inc(2)
     * </code></pre>
     */
    BinaryOpNode<Attributes> INC_ONE_PLUS_INC_TWO_NODE = binaryOpNode(
        Meta.nameless(Type.INT),
        applyNode(Meta.nameless(Type.INT), refNode(LET_INC_META, "inc"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        BinaryOp.ADD,
        applyNode(Meta.nameless(Type.INT), refNode(LET_INC_META, "inc"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))));

    /**
     * A typed example node for an addition expression that calls magic on its operands:
     * <pre><code>
     * magic(1) + magic(2)
     * </code></pre>
     */
    BinaryOpNode<Attributes> MAGIC_ONE_PLUS_MAGIC_TWO_NODE = binaryOpNode(
        Meta.nameless(Type.INT),
        applyNode(Meta.nameless(Type.INT), refNode(LET_MAGIC_META, "magic"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        BinaryOp.ADD,
        applyNode(Meta.nameless(Type.INT), refNode(LET_MAGIC_META, "magic"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))));

    /**
     * A typed example node for an addition expression that calls identity on its operands:
     * <pre><code>
     * id(1) + id(2)
     * </code></pre>
     */
    BinaryOpNode<Attributes> ID_ONE_PLUS_ID_TWO_NODE = binaryOpNode(
        Meta.nameless(Type.INT),
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        BinaryOp.ADD,
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))));

    /**
     * A typed example node for an if expression with a constant boolean condition and branches.
     * <pre><code>if true then 1 else 2</code></pre>
     */
    IfNode<Attributes> IF_TRUE_THEN_ONE_ELSE_TWO_NODE = ifNode(
        Meta.nameless(Type.INT),
        boolNode(Meta.nameless(Type.BOOLEAN), true),
        intNode(Meta.nameless(Type.INT), 1),
        intNode(Meta.nameless(Type.INT), 2));

    /**
     * A typed example node for an if expression with a condition that calls identity and constant branches.
     * <pre><code>if id(true) then 1 else 2</code></pre>
     */
    IfNode<Attributes> IF_ID_TRUE_THEN_ONE_ELSE_TWO_NODE = ifNode(
        Meta.nameless(Type.INT),
        applyNode(
            Meta.nameless(Type.BOOLEAN),
            refNode(LET_ID_META, "id"),
            Lists.immutable.of(boolNode(Meta.nameless(Type.BOOLEAN), true))),
        intNode(Meta.nameless(Type.INT), 1),
        intNode(Meta.nameless(Type.INT), 2));

    /**
     * A typed example node for an if expression with a condition that calls identity and branches that call identity.
     * <pre><code>if id(true) then id(1) else id(2)</code></pre>
     */
    IfNode<Attributes> IF_ID_TRUE_THEN_ID_ONE_ELSE_ID_TWO_NODE = ifNode(
        Meta.nameless(Type.INT),
        applyNode(
            Meta.nameless(Type.BOOLEAN),
            refNode(LET_ID_META, "id"),
            Lists.immutable.of(boolNode(Meta.nameless(Type.BOOLEAN), true))),
        applyNode(
            Meta.nameless(Type.INT),
            refNode(LET_ID_META, "id"),
            Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        applyNode(
            Meta.nameless(Type.INT),
            refNode(LET_ID_META, "id"),
            Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))));

    /**
     * A typed example node for a block that declares a mixture of constant bindings and computations.
     * <pre><code>
     * {
     *     let a = 1
     *     let b = id(2)
     *     let c = magic(3)
     *     let d = true
     *     inc(c)
     * }
     * </code></pre>
     */
    BlockNode<Attributes> MIXED_BLOCK_NODE = blockNode(
        Meta.nameless(Type.INT),
        Lists.immutable.of(
            letNode(
                Meta.of(new LocalName("a", 0), Type.INT),
                "a",
                intNode(Meta.nameless(Type.INT), 1)),
            letNode(
                Meta.of(new LocalName("b", 1), Type.INT),
                "b",
                applyNode(
                    Meta.nameless(Type.INT),
                    refNode(LET_ID_META, "id"),
                    Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2)))),
            letNode(
                Meta.of(new LocalName("c", 2), Type.INT),
                "c",
                applyNode(
                    Meta.nameless(Type.INT),
                    refNode(LET_MAGIC_META, "magic"),
                    Lists.immutable.of(intNode(Meta.nameless(Type.INT), 3)))),
            letNode(
                Meta.of(new LocalName("d", 3), Type.BOOLEAN),
                "d",
                boolNode(Meta.nameless(Type.BOOLEAN), true))),
        applyNode(
            Meta.nameless(Type.INT),
            refNode(LET_INC_META, "inc"),
            Lists.immutable.of(refNode(Meta.of(new LocalName("c", 2), Type.INT), "c"))));

    /**
     * A typed example node for a block that declares a mixture of constant bindings and computations,
     * including a nested block of bindings.
     * <pre><code>
     * {
     *     let a = 1
     *     let c = {
     *         let b = id(2)
     *         magic(3)
     *     }
     *     let d = true
     *     inc(c)
     * }
     * </code></pre>
     */
    BlockNode<Attributes> MIXED_NESTED_BLOCK_NODE = blockNode(
        Meta.nameless(Type.INT),
        Lists.immutable.of(
            letNode(
                Meta.of(new LocalName("a", 0), Type.INT),
                "a",
                intNode(Meta.nameless(Type.INT), 1)),
            letNode(
                Meta.of(new LocalName("c", 2), Type.INT),
                "c",
                blockNode(
                    Meta.nameless(Type.INT),
                    Lists.immutable.of(
                        letNode(
                            Meta.of(new LocalName("b", 1), Type.INT),
                            "b",
                            applyNode(
                                Meta.nameless(Type.INT),
                                refNode(LET_ID_META, "id"),
                                Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))))
                    ),
                    applyNode(
                        Meta.nameless(Type.INT),
                        refNode(LET_MAGIC_META, "magic"),
                        Lists.immutable.of(intNode(Meta.nameless(Type.INT), 3))))),
            letNode(
                Meta.of(new LocalName("d", 3), Type.BOOLEAN),
                "d",
                boolNode(Meta.nameless(Type.BOOLEAN), true))),
        applyNode(
            Meta.nameless(Type.INT),
            refNode(LET_INC_META, "inc"),
            Lists.immutable.of(refNode(Meta.of(new LocalName("c", 2), Type.INT), "c"))));

    /**
     * A typed example node for a block that declares a mixture of constant bindings and computations
     * and does not have a tail expression.
     * <pre><code>
     * {
     *     let a = 1
     *     let b = id(2)
     *     let c = magic(3)
     *     let d = true
     * }
     * </code></pre>
     */
    BlockNode<Attributes> MIXED_BLOCK_NO_TAIL_NODE = blockNode(
        Meta.nameless(Type.INT),
        Lists.immutable.of(
            letNode(
                Meta.of(new LocalName("a", 0), Type.INT),
                "a",
                intNode(Meta.nameless(Type.INT), 1)),
            letNode(
                Meta.of(new LocalName("b", 1), Type.INT),
                "b",
                applyNode(
                    Meta.nameless(Type.INT),
                    refNode(LET_ID_META, "id"),
                    Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2)))),
            letNode(
                Meta.of(new LocalName("c", 2), Type.INT),
                "c",
                applyNode(
                    Meta.nameless(Type.INT),
                    refNode(LET_MAGIC_META, "magic"),
                    Lists.immutable.of(intNode(Meta.nameless(Type.INT), 3)))),
            letNode(
                Meta.of(new LocalName("d", 3), Type.BOOLEAN),
                "d",
                boolNode(Meta.nameless(Type.BOOLEAN), true))),
        Optional.empty());

    /**
     * A typed example node for a block that declares a mixture of constant bindings and computations
     * including a nested block, and does not have a tail expression.
     * <pre><code>
     * {
     *     let a = 1
     *     let c = {
     *       let b = id(2)
     *       magic(3)
     *     }
     *     let d = true
     * }
     * </code></pre>
     */
    BlockNode<Attributes> MIXED_NESTED_BLOCK_NO_TAIL_NODE = blockNode(
        Meta.nameless(Type.INT),
        Lists.immutable.of(
            letNode(
                Meta.of(new LocalName("a", 0), Type.INT),
                "a",
                intNode(Meta.nameless(Type.INT), 1)),
            letNode(
                Meta.of(new LocalName("c", 2), Type.INT),
                "c",
                blockNode(
                    Meta.nameless(Type.INT),
                    Lists.immutable.of(
                        letNode(
                            Meta.of(new LocalName("b", 1), Type.INT),
                            "b",
                            applyNode(
                                Meta.nameless(Type.INT),
                                refNode(LET_ID_META, "id"),
                                Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))))
                    ),
                    applyNode(
                        Meta.nameless(Type.INT),
                        refNode(LET_MAGIC_META, "magic"),
                        Lists.immutable.of(intNode(Meta.nameless(Type.INT), 3))))),
            letNode(
                Meta.of(new LocalName("d", 3), Type.BOOLEAN),
                "d",
                boolNode(Meta.nameless(Type.BOOLEAN), true))),
        Optional.empty());

    /**
     * A typed example node for a match node which scrutinises an integer with an identifier pattern.
     * <pre><code>
     * match 1 with {
     *    case x -> x
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_INT_ID_PATTERN = matchNode(
        Meta.nameless(Type.INT),
        intNode(Meta.nameless(Type.INT), 1),
        Lists.immutable.of(caseNode(
            Meta.nameless(Type.INT),
            idPatternNode(Meta.of(new LocalName("x", 0), Type.INT), "x"),
            refNode(Meta.of(new LocalName("x", 0), Type.INT), "x"))));

    /**
     * A typed example node for a match node which scrutinises an integer with a literal pattern.
     * <pre><code>
     * match 1 with {
     *    case 1 -> true
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_INT_LITERAL_PATTERN = matchNode(
        Meta.nameless(Type.BOOLEAN),
        intNode(Meta.nameless(Type.INT), 1),
        Lists.immutable.of(caseNode(
            Meta.nameless(Type.BOOLEAN),
            literalPatternNode(Meta.nameless(Type.INT), intNode(Meta.nameless(Type.INT), 1)),
            boolNode(Meta.nameless(Type.BOOLEAN), true))));

    /**
     * A typed example node for a match node which scrutinises an integer with a literal and alias pattern.
     * <pre><code>
     * match 1 with {
     *    case x @ 1 -> x
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_INT_ALIAS_PATTERN = matchNode(
        Meta.nameless(Type.INT),
        intNode(Meta.nameless(Type.INT), 1),
        Lists.immutable.of(caseNode(
            Meta.nameless(Type.INT),
            aliasPatternNode(
                Meta.of(new LocalName("x", 0), Type.INT),
                "x",
                literalPatternNode(Meta.nameless(Type.INT), intNode(Meta.nameless(Type.INT), 1))),
            refNode(Meta.of(new LocalName("x", 0), Type.INT), "x"))));

    /**
     * A typed example node for a match node which scrutinises a reference to a List with constructor and field identifier patterns.
     * <pre><code>
     * match list with {
     *    case Nil {} -> 0
     *    case Cons { head, tail } -> 1
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_LIST_CONSTRUCTOR_ID_PATTERN = matchNode(
        Meta.nameless(Type.INT),
        refNode(Meta.of(new LocalName("list", 0), LIST_A_TYPE), "list"),
        Lists.immutable.of(
            caseNode(
                Meta.nameless(Type.INT),
                constructorPatternNode(
                    Meta.of(NIL_CONSTRUCTOR_NAME, LIST_A_TYPE),
                    idNode(Range.EMPTY, "Nil"),
                    Lists.immutable.empty()),
                intNode(Meta.nameless(Type.INT), 0)),
            caseNode(
                Meta.nameless(Type.INT),
                constructorPatternNode(
                    Meta.of(CONS_CONSTRUCTOR_NAME, LIST_A_TYPE),
                    idNode(Range.EMPTY, "Cons"),
                    Lists.immutable.of(
                        fieldPatternNode(
                            Meta.of(HEAD_FIELD_NAME, TYPE_VAR_A),
                            "head",
                            idPatternNode(Meta.of(new LocalName("head", 1), TYPE_VAR_A), "head")),
                        fieldPatternNode(
                            Meta.of(TAIL_FIELD_NAME, LIST_A_TYPE),
                            "tail",
                            idPatternNode(Meta.of(new LocalName("tail", 2), LIST_A_TYPE), "tail")))),
                intNode(Meta.nameless(Type.INT), 1))));

    /**
     * A typed example node for a match node which calls identity on its scrutinee integer before matching it with an identifier pattern.
     * <pre><code>
     * match id(1) with {
     *    case x -> x
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_ID_INT_ID_PATTERN = matchNode(
        Meta.nameless(Type.INT),
        applyNode(
            Meta.nameless(Type.INT),
            refNode(LET_ID_META, "id"),
            Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        Lists.immutable.of(caseNode(
            Meta.nameless(Type.INT),
            idPatternNode(Meta.of(new LocalName("x", 0), Type.INT), "x"),
            refNode(Meta.of(new LocalName("x", 0), Type.INT), "x"))));

    /**
     * A typed example node for a match node which scrutinises an integer with a literal pattern before calling identity on its consequent boolean.
     * <pre><code>
     * match 1 with {
     *    case 1 -> id(true)
     * }
     * </code></pre>
     */
    MatchNode<Attributes> MATCH_NODE_INT_LITERAL_PATTERN_ID = matchNode(
        Meta.nameless(Type.BOOLEAN),
        intNode(Meta.nameless(Type.INT), 1),
        Lists.immutable.of(caseNode(
            Meta.nameless(Type.BOOLEAN),
            literalPatternNode(Meta.nameless(Type.INT), intNode(Meta.nameless(Type.INT), 1)),
            applyNode(
                Meta.nameless(Type.BOOLEAN),
                refNode(LET_ID_META, "id"),
                Lists.immutable.of(boolNode(Meta.nameless(Type.BOOLEAN), true))))));
}
