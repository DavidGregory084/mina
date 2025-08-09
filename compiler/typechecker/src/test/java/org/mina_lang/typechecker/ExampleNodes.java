/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class ExampleNodes {
    public static NamespaceName TYPECHECKER_NAMESPACE = new NamespaceName(
        java.util.List.of("Mina", "Test"), "Typechecker");

    public static NamespaceName KINDCHECKER_NAMESPACE = new NamespaceName(
        java.util.List.of("Mina", "Test"), "Kindchecker");

    public static Meta<Name> namelessMeta() {
        return Meta.of(Nameless.INSTANCE);
    }

    public static Meta<Attributes> namelessMeta(Sort sort) {
        return Meta.nameless(sort);
    }

    public static class Boolean {
        public static BuiltInName NAME = new BuiltInName("Boolean");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Boolean");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(NAME, TypeKind.INSTANCE), "Boolean");

        public static LiteralNode<Name> namedNode(boolean b) {
            return boolNode(namelessMeta(), b);
        }

        public static LiteralNode<Attributes> typedNode(boolean b) {
            return boolNode(namelessMeta(Type.BOOLEAN), b);
        }
    }

    public static class Int {
        public static BuiltInName NAME = new BuiltInName("Int");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Int");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(NAME, TypeKind.INSTANCE), "Int");

        public static LiteralNode<Name> namedNode(int i) {
            return intNode(namelessMeta(), i);
        }

        public static LiteralNode<Attributes> typedNode(int i) {
            return intNode(namelessMeta(Type.INT), i);
        }
    }

    public static class Long {
        public static BuiltInName NAME = new BuiltInName("Long");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Long");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(NAME, TypeKind.INSTANCE), "Long");

        public static LiteralNode<Name> namedNode(long i) {
            return longNode(namelessMeta(), i);
        }

        public static LiteralNode<Attributes> typedNode(long i) {
            return longNode(namelessMeta(Type.LONG), i);
        }
    }

    public static class Float {
        public static BuiltInName NAME = new BuiltInName("Float");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Float");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, TypeKind.INSTANCE), "Float");

        public static LiteralNode<Name> namedNode(float i) {
            return floatNode(namelessMeta(), i);
        }

        public static LiteralNode<Attributes> typedNode(float i) {
            return floatNode(namelessMeta(Type.FLOAT), i);
        }
    }

    public static class Double {
        public static BuiltInName NAME = new BuiltInName("Double");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Double");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, TypeKind.INSTANCE), "Double");

        public static LiteralNode<Name> namedNode(double i) {
            return doubleNode(namelessMeta(), i);
        }

        public static LiteralNode<Attributes> typedNode(double i) {
            return doubleNode(namelessMeta(Type.DOUBLE), i);
        }
    }

    public static class Char {
        public static BuiltInName NAME = new BuiltInName("Char");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Char");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, TypeKind.INSTANCE), "Char");

        public static LiteralNode<Name> namedNode(char c) {
            return charNode(namelessMeta(), c);
        }

        public static LiteralNode<Attributes> typedNode(char c) {
            return charNode(namelessMeta(Type.CHAR), c);
        }
    }

    public static class String {
        public static BuiltInName NAME = new BuiltInName("String");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "String");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, TypeKind.INSTANCE), "String");

        public static LiteralNode<Name> namedNode(java.lang.String s) {
            return stringNode(namelessMeta(), s);
        }

        public static LiteralNode<Attributes> typedNode(java.lang.String s) {
            return stringNode(namelessMeta(Type.STRING), s);
        }
    }

    public static class Param {
        public static LocalName name(java.lang.String name) {
            return new LocalName(name, 0);
        }

        public static ParamNode<Name> namedNode(java.lang.String name) {
            return paramNode(Meta.of(name(name)), name);
        }

        public static ParamNode<Name> namedNode(
                java.lang.String name,
                TypeNode<Name> annotation) {
            return paramNode(Meta.of(name(name)), name, annotation);
        }

        public static ParamNode<Attributes> typedNode(java.lang.String name, Type type) {
            return paramNode(Meta.of(name(name), type), name);
        }

        public static ParamNode<Attributes> typedNode(
                java.lang.String name,
                Type type,
                TypeNode<Attributes> annotation) {
            return paramNode(Meta.of(name(name), type), name, annotation);
        }
    }

    public static class LocalVar {
        public static LocalName name(java.lang.String name) {
            return new LocalName(name, 0);
        }

        public static ReferenceNode<Name> namedNode(java.lang.String name) {
            return refNode(Meta.of(name(name)), name);
        }

        public static ReferenceNode<Attributes> typedNode(java.lang.String name, Type type) {
            return refNode(Meta.of(name(name), type), name);
        }
    }

    public static class Bool {
        public static DataName NAME = new DataName(new QualifiedName(TYPECHECKER_NAMESPACE, "Bool"));

        public static Kind KIND = TypeKind.INSTANCE;

        public static Meta<Attributes> KINDED_META = Meta.of(NAME, KIND);

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Bool");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, KIND), "Bool");
    }

    public static class True {
        public static ConstructorName NAME = new ConstructorName(
                Bool.NAME,
                new QualifiedName(TYPECHECKER_NAMESPACE, "True"));

        public static Type TYPE = Type.function(
                java.util.List.of(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> TYPED_META = Meta.of(NAME, TYPE);

        public static TypeNode<Name> NAMED_TYPE_NODE = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                java.util.List.of(),
                Bool.NAMED_TYPE_NODE);

        public static TypeNode<Attributes> KINDED_TYPE_NODE = funTypeNode(
                Meta.nameless(TypeKind.INSTANCE),
                java.util.List.of(),
                Bool.KINDED_TYPE_NODE);
    }

    public static class False {
        public static ConstructorName NAME = new ConstructorName(
                Bool.NAME,
                new QualifiedName(TYPECHECKER_NAMESPACE, "False"));

        public static Type TYPE = Type.function(
                java.util.List.of(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> TYPED_META = Meta.of(NAME, TYPE);

        public static TypeNode<Name> NAMED_TYPE_NODE = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                java.util.List.of(),
                Bool.NAMED_TYPE_NODE);

        public static TypeNode<Attributes> KINDED_TYPE_NODE = funTypeNode(
                Meta.nameless(TypeKind.INSTANCE),
                java.util.List.of(),
                Bool.KINDED_TYPE_NODE);
    }

    public static class List {
        public static QualifiedName QUAL_NAME = new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "List");

        public static DataName NAME = new DataName(QUAL_NAME);

        public static Kind KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);

        public static Type TYPE = new TypeConstructor(QUAL_NAME, KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(NAME, KIND);

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "List");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, KIND), "List");

        public static Name TYPE_VAR_A_NAME = new ForAllVarName("A");

        public static Kind TYPE_VAR_A_KIND = TypeKind.INSTANCE;

        public static TypeVar TYPE_VAR_A_TYPE = new ForAllVar("A", TYPE_VAR_A_KIND);

        public static Meta<Attributes> TYPE_VAR_A_META = Meta.of(
                new Attributes(TYPE_VAR_A_NAME, TypeKind.INSTANCE));

        public static DataNode<Name> NAMED_NODE = dataNode(
                Meta.of(NAME),
                "List",
                java.util.List.of(forAllVarNode(Meta.of(TYPE_VAR_A_NAME), "A")),
                java.util.List.of(Cons.NAMED_NODE, Nil.NAMED_NODE));

        public static DataNode<Attributes> KINDED_NODE = dataNode(
                KINDED_META,
                "List",
                java.util.List.of(forAllVarNode(TYPE_VAR_A_META, "A")),
                java.util.List.of(Cons.TYPED_NODE, Nil.TYPED_NODE));
    }

    public static class Cons {
        public static ConstructorName NAME = new ConstructorName(
                List.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Cons"));

        public static Kind KIND = List.KIND;

        public static Type HEAD_TYPE = List.TYPE_VAR_A_TYPE;

        public static Type TAIL_TYPE = new TypeApply(
                List.TYPE,
                java.util.List.of(List.TYPE_VAR_A_TYPE),
                TypeKind.INSTANCE);

        public static Type TYPE = new QuantifiedType(
                java.util.List.of(List.TYPE_VAR_A_TYPE),
                Type.function(
                        HEAD_TYPE,
                        TAIL_TYPE,
                        new TypeApply(
                                List.TYPE,
                                java.util.List.of(List.TYPE_VAR_A_TYPE),
                                TypeKind.INSTANCE)),
                KIND);

        public static Meta<Attributes> TYPED_META = Meta.of(NAME, TYPE);

        public static FieldName HEAD_NAME = new FieldName(NAME, "head");

        public static Meta<Attributes> TYPED_HEAD_META = Meta.of(HEAD_NAME, HEAD_TYPE);

        public static FieldName TAIL_NAME = new FieldName(NAME, "tail");

        public static Meta<Attributes> TYPED_TAIL_META = Meta.of(TAIL_NAME, TAIL_TYPE);

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Cons",
                java.util.List.of(
                        constructorParamNode(
                                Meta.<Name>of(HEAD_NAME),
                                "head",
                                typeRefNode(Meta.of(List.TYPE_VAR_A_NAME), "A")),
                        constructorParamNode(
                                Meta.<Name>of(TAIL_NAME),
                                "tail",
                                typeApplyNode(
                                        namelessMeta(),
                                        typeRefNode(Meta.of(List.NAME), "List"),
                                        java.util.List.of(typeRefNode(Meta.of(List.TYPE_VAR_A_NAME), "A"))))),
                Optional.empty());

        public static ConstructorNode<Attributes> TYPED_NODE = constructorNode(
                TYPED_META,
                "Cons",
                java.util.List.of(
                        constructorParamNode(
                                TYPED_HEAD_META,
                                "head",
                                typeRefNode(List.TYPE_VAR_A_META, "A")),
                        constructorParamNode(
                                TYPED_TAIL_META,
                                "tail",
                                typeApplyNode(
                                        namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(List.KINDED_META, "List"),
                                        java.util.List.of(typeRefNode(List.TYPE_VAR_A_META, "A"))))),
                Optional.empty());
    }

    public static class Nil {
        public static ConstructorName NAME = new ConstructorName(
                List.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Nil"));

        public static Kind KIND = List.KIND;

        public static Type TYPE = new QuantifiedType(
                java.util.List.of(List.TYPE_VAR_A_TYPE),
                Type.function(
                        new TypeApply(
                                List.TYPE,
                                java.util.List.of(List.TYPE_VAR_A_TYPE), TypeKind.INSTANCE)),
                KIND);

        public static Meta<Attributes> TYPED_META = Meta.of(NAME, TYPE);

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Nil",
                java.util.List.of(),
                Optional.empty());

        public static ConstructorNode<Attributes> TYPED_NODE = constructorNode(
                TYPED_META,
                "Nil",
                java.util.List.of(),
                Optional.empty());
    }

    public static class Either {
        public static DataName NAME = new DataName(new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Either"));

        public static Kind KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);

        public static Meta<Attributes> KINDED_META = Meta.of(NAME, KIND);

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Either");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, KIND), "Either");
    }

    public static class Functor {
        public static DataName NAME = new DataName(new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Functor"));

        public static Kind KIND = new HigherKind(
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE),
                TypeKind.INSTANCE);

        public static Meta<Attributes> KINDED_META = Meta.of(NAME, KIND);

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Functor");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, KIND), "Functor");
    }

    public static class Fix {
        public static QualifiedName QUAL_NAME = new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Fix");
        public static DataName NAME = new DataName(QUAL_NAME);

        public static Kind KIND = new HigherKind(
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE),
                TypeKind.INSTANCE);

        public static Type TYPE = new TypeConstructor(QUAL_NAME, KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(NAME, KIND);

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Fix");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(Meta.of(NAME, KIND), "Fix");

        public static Name TYPE_VAR_F_NAME = new ForAllVarName("F");
        public static Kind TYPE_VAR_F_KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        public static TypeVar TYPE_VAR_F_TYPE = new ForAllVar("F", TYPE_VAR_F_KIND);

        public static Meta<Attributes> TYPE_VAR_F_META = Meta.of(
                new Attributes(TYPE_VAR_F_NAME, TYPE_VAR_F_KIND));

        public static DataNode<Name> NAMED_NODE = dataNode(
                Meta.<Name>of(NAME),
                "Fix",
                java.util.List.of(forAllVarNode(Meta.of(TYPE_VAR_F_NAME), "F")),
                java.util.List.of(Unfix.NAMED_NODE));

        public static DataNode<Attributes> KINDED_NODE = dataNode(
                ExampleNodes.Fix.KINDED_META,
                "Fix",
                java.util.List.of(forAllVarNode(TYPE_VAR_F_META, "F")),
                java.util.List.of(Unfix.KINDED_NODE));
    }

    public static class Unfix {
        public static ConstructorName NAME = new ConstructorName(
                Fix.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Unfix"));

        public static Kind KIND = Fix.KIND;

        public static Type UNFIX_TYPE = new TypeApply(
                Fix.TYPE_VAR_F_TYPE,
                java.util.List.of(
                        new TypeApply(
                                Fix.TYPE,
                                java.util.List.of(Fix.TYPE_VAR_F_TYPE),
                                TypeKind.INSTANCE)),
                TypeKind.INSTANCE);

        public static Type TYPE = new QuantifiedType(
                java.util.List.of(Fix.TYPE_VAR_F_TYPE),
                Type.function(
                        UNFIX_TYPE,
                        new TypeApply(
                                Fix.TYPE,
                                java.util.List.of(Fix.TYPE_VAR_F_TYPE),
                                TypeKind.INSTANCE)),
                Fix.KIND);

        public static Meta<Attributes> TYPED_META = Meta.of(NAME, TYPE);

        public static FieldName UNFIX_NAME = new FieldName(NAME, "unfix");

        public static Meta<Attributes> TYPED_UNFIX_META = Meta.of(UNFIX_NAME, UNFIX_TYPE);

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Unfix",
                java.util.List.of(
                        constructorParamNode(
                                Meta.<Name>of(UNFIX_NAME),
                                "unfix",
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        typeRefNode(Meta.of(Fix.TYPE_VAR_F_NAME), "F"),
                                        java.util.List.of(
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(),
                                                        Fix.NAMED_TYPE_NODE,
                                                        java.util.List.of(
                                                                typeRefNode(Meta.of(Fix.TYPE_VAR_F_NAME), "F"))))))),
                Optional.empty());

        public static ConstructorNode<Attributes> KINDED_NODE = constructorNode(
                Unfix.TYPED_META,
                "Unfix",
                java.util.List.of(
                        constructorParamNode(
                                ExampleNodes.Unfix.TYPED_UNFIX_META,
                                "unfix",
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(Fix.TYPE_VAR_F_META, "F"),
                                        java.util.List.of(
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                                        ExampleNodes.Fix.KINDED_TYPE_NODE,
                                                        java.util.List.of(typeRefNode(Fix.TYPE_VAR_F_META, "F"))))))),
                Optional.empty());
    }
}
