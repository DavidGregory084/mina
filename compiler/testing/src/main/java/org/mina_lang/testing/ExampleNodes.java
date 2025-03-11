/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

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
     * A typed and kinded example node for a simple let binding to an integer literal:
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
     * A typed and kinded example node for a negation expression:
     * <pre><code>
     * -1
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        intNode(Meta.nameless(Type.INT), 1));

    /**
     * A typed and kinded example node for a negation expression that calls identity on its operand:
     * <pre><code>
     * -id(1)
     * </code></pre>
     */
    UnaryOpNode<Attributes> NEGATE_ID_ONE_NODE = unaryOpNode(
        Meta.nameless(Type.INT),
        UnaryOp.NEGATE,
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))));

    /**
     * A typed and kinded example node for an addition expression:
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
     * A typed and kinded example node for an addition expression that calls identity on its operands:
     * <pre><code>
     * id(1) + id(2)
     * </code></pre>
     */
    BinaryOpNode<Attributes> ID_ONE_PLUS_ID_TWO_NODE = binaryOpNode(
        Meta.nameless(Type.INT),
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 1))),
        BinaryOp.ADD,
        applyNode(Meta.nameless(Type.INT), refNode(LET_ID_META, "id"), Lists.immutable.of(intNode(Meta.nameless(Type.INT), 2))));
}
