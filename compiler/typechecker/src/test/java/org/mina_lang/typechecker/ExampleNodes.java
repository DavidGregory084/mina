package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

public class ExampleNodes {
    public static NamespaceName TYPECHECKER_NAMESPACE = new NamespaceName(
            Lists.immutable.of("Mina", "Test"), "Typechecker");

    public static NamespaceName KINDCHECKER_NAMESPACE = new NamespaceName(
            Lists.immutable.of("Mina", "Test"), "Kindchecker");

    public static Meta<Name> namelessMeta() {
        return Meta.of(Nameless.INSTANCE);
    }

    public static Meta<Attributes> namelessMeta(Sort sort) {
        return Meta.of(new Attributes(Nameless.INSTANCE, sort));
    }

    public static class Boolean {
        public static BuiltInName NAME = new BuiltInName("Boolean");

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Boolean");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Boolean");

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
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Int");

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
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Long");

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

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Float");

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

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Double");

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

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Char");

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

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "String");

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
            return paramNode(Meta.of(new Attributes(name(name), type)), name);
        }

        public static ParamNode<Attributes> typedNode(
                java.lang.String name,
                Type type,
                TypeNode<Attributes> annotation) {
            return paramNode(Meta.of(new Attributes(name(name), type)), name, annotation);
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
            return refNode(Meta.of(new Attributes(name(name), type)), name);
        }
    }

    public static class Bool {
        public static DataName NAME = new DataName(new QualifiedName(TYPECHECKER_NAMESPACE, "Bool"));

        public static Kind KIND = TypeKind.INSTANCE;

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Bool");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, KIND)), "Bool");
    }

    public static class True {
        public static ConstructorName NAME = new ConstructorName(
                Bool.NAME,
                new QualifiedName(TYPECHECKER_NAMESPACE, "True"));

        public static Type TYPE = Type.function(
                Lists.immutable.empty(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> TYPED_META = Meta.of(new Attributes(NAME, TYPE));

        public static TypeNode<Name> NAMED_TYPE_NODE = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.empty(),
                Bool.NAMED_TYPE_NODE);

        public static TypeNode<Attributes> KINDED_TYPE_NODE = funTypeNode(
                Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                Lists.immutable.empty(),
                Bool.KINDED_TYPE_NODE);
    }

    public static class False {
        public static ConstructorName NAME = new ConstructorName(
                Bool.NAME,
                new QualifiedName(TYPECHECKER_NAMESPACE, "False"));

        public static Type TYPE = Type.function(
                Lists.immutable.empty(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> TYPED_META = Meta.of(new Attributes(NAME, TYPE));

        public static TypeNode<Name> NAMED_TYPE_NODE = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.empty(),
                Bool.NAMED_TYPE_NODE);

        public static TypeNode<Attributes> KINDED_TYPE_NODE = funTypeNode(
                Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                Lists.immutable.empty(),
                Bool.KINDED_TYPE_NODE);
    }

    public static class List {
        public static QualifiedName QUAL_NAME = new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "List");

        public static DataName NAME = new DataName(QUAL_NAME);

        public static Kind KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);

        public static Type TYPE = new TypeConstructor(QUAL_NAME, KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "List");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, KIND)), "List");

        public static Name TYPE_VAR_A_NAME = new TypeVarName("A");

        public static Kind TYPE_VAR_A_KIND = TypeKind.INSTANCE;

        public static TypeVar TYPE_VAR_A_TYPE = new ForAllVar("A", TYPE_VAR_A_KIND);

        public static Meta<Attributes> TYPE_VAR_A_META = Meta.of(
                new Attributes(TYPE_VAR_A_NAME, TypeKind.INSTANCE));

        public static DataNode<Name> NAMED_NODE = dataNode(
                Meta.<Name>of(NAME),
                "List",
                Lists.immutable.of(
                        forAllVarNode(Meta.of(TYPE_VAR_A_NAME), "A")),
                Lists.immutable.of(
                        Cons.NAMED_NODE,
                        Nil.NAMED_NODE));

        public static DataNode<Attributes> KINDED_NODE = dataNode(
                KINDED_META,
                "List",
                Lists.immutable.of(
                        forAllVarNode(TYPE_VAR_A_META, "A")),
                Lists.immutable.of(
                        Cons.KINDED_NODE,
                        Nil.KINDED_NODE));
    }

    public static class Cons {
        public static ConstructorName NAME = new ConstructorName(
                List.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Cons"));

        public static Kind KIND = List.KIND;

        public static Type TYPE = new TypeLambda(
                Lists.immutable.of(List.TYPE_VAR_A_TYPE),
                Type.function(
                        List.TYPE_VAR_A_TYPE,
                        new TypeApply(
                                List.TYPE,
                                Lists.immutable.of(List.TYPE_VAR_A_TYPE),
                                TypeKind.INSTANCE),
                        new TypeApply(
                                List.TYPE,
                                Lists.immutable.of(List.TYPE_VAR_A_TYPE),
                                TypeKind.INSTANCE)),
                KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static FieldName HEAD_NAME = new FieldName(NAME, "head");

        public static Meta<Attributes> KINDED_HEAD_META = Meta.of(new Attributes(HEAD_NAME, TypeKind.INSTANCE));

        public static FieldName TAIL_NAME = new FieldName(NAME, "tail");

        public static Meta<Attributes> KINDED_TAIL_META = Meta.of(new Attributes(TAIL_NAME, TypeKind.INSTANCE));

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Cons",
                Lists.immutable.of(
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
                                        Lists.immutable.of(typeRefNode(Meta.of(List.TYPE_VAR_A_NAME), "A"))))),
                Optional.empty());

        public static ConstructorNode<Attributes> KINDED_NODE = constructorNode(
                KINDED_META,
                "Cons",
                Lists.immutable.of(
                        constructorParamNode(
                                KINDED_HEAD_META,
                                "head",
                                typeRefNode(List.TYPE_VAR_A_META, "A")),
                        constructorParamNode(
                                KINDED_TAIL_META,
                                "tail",
                                typeApplyNode(
                                        namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(List.KINDED_META, "List"),
                                        Lists.immutable.of(typeRefNode(List.TYPE_VAR_A_META, "A"))))),
                Optional.empty());
    }

    public static class Nil {
        public static ConstructorName NAME = new ConstructorName(
                List.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Nil"));

        public static Kind KIND = List.KIND;

        public static Type TYPE = new TypeLambda(
                Lists.immutable.of(List.TYPE_VAR_A_TYPE),
                Type.function(
                        new TypeApply(
                                List.TYPE,
                                Lists.immutable.of(List.TYPE_VAR_A_TYPE), TypeKind.INSTANCE)),
                KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Nil",
                Lists.immutable.empty(),
                Optional.empty());

        public static ConstructorNode<Attributes> KINDED_NODE = constructorNode(
                KINDED_META,
                "Nil",
                Lists.immutable.empty(),
                Optional.empty());
    }

    public static class Either {
        public static DataName NAME = new DataName(new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Either"));

        public static Kind KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Either");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, KIND)), "Either");
    }

    public static class Functor {
        public static DataName NAME = new DataName(new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Functor"));

        public static Kind KIND = new HigherKind(
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE),
                TypeKind.INSTANCE);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Functor");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, KIND)), "Functor");
    }

    public static class Fix {
        public static QualifiedName QUAL_NAME = new QualifiedName(ExampleNodes.KINDCHECKER_NAMESPACE, "Fix");
        public static DataName NAME = new DataName(QUAL_NAME);

        public static Kind KIND = new HigherKind(
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE),
                TypeKind.INSTANCE);

        public static Type TYPE = new TypeConstructor(QUAL_NAME, KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Fix");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, KIND)), "Fix");

        public static Name TYPE_VAR_F_NAME = new TypeVarName("F");
        public static Kind TYPE_VAR_F_KIND = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        public static TypeVar TYPE_VAR_F_TYPE = new ForAllVar("F", TYPE_VAR_F_KIND);

        public static Meta<Attributes> TYPE_VAR_F_META = Meta.of(
                new Attributes(TYPE_VAR_F_NAME, TYPE_VAR_F_KIND));

        public static DataNode<Name> NAMED_NODE = dataNode(
                Meta.<Name>of(NAME),
                "Fix",
                Lists.immutable.of(
                        forAllVarNode(Meta.of(TYPE_VAR_F_NAME), "F")),
                Lists.immutable.of(Unfix.NAMED_NODE));

        public static DataNode<Attributes> KINDED_NODE = dataNode(
                ExampleNodes.Fix.KINDED_META,
                "Fix",
                Lists.immutable.of(
                        forAllVarNode(TYPE_VAR_F_META, "F")),
                Lists.immutable.of(Unfix.KINDED_NODE));
    }

    public static class Unfix {
        public static ConstructorName NAME = new ConstructorName(
                Fix.NAME,
                new QualifiedName(KINDCHECKER_NAMESPACE, "Unfix"));

        public static Kind KIND = Fix.KIND;

        public static Type TYPE = new TypeLambda(
                Lists.immutable.of(Fix.TYPE_VAR_F_TYPE),
                Type.function(
                        new TypeApply(
                                Fix.TYPE_VAR_F_TYPE,
                                Lists.immutable.of(
                                        new TypeApply(
                                                Fix.TYPE,
                                                Lists.immutable.of(Fix.TYPE_VAR_F_TYPE),
                                                TypeKind.INSTANCE)),
                                TypeKind.INSTANCE),
                        new TypeApply(
                                Fix.TYPE,
                                Lists.immutable.of(Fix.TYPE_VAR_F_TYPE),
                                TypeKind.INSTANCE)),
                Fix.KIND);

        public static Meta<Attributes> KINDED_META = Meta.of(new Attributes(NAME, KIND));

        public static FieldName UNFIX_NAME = new FieldName(NAME, "unfix");

        public static Meta<Attributes> KINDED_UNFIX_META = Meta.of(new Attributes(UNFIX_NAME, TypeKind.INSTANCE));

        public static ConstructorNode<Name> NAMED_NODE = constructorNode(
                Meta.<Name>of(NAME),
                "Unfix",
                Lists.immutable.of(
                        constructorParamNode(
                                Meta.<Name>of(UNFIX_NAME),
                                "unfix",
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        typeRefNode(Meta.of(Fix.TYPE_VAR_F_NAME), "F"),
                                        Lists.immutable.of(
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(),
                                                        Fix.NAMED_TYPE_NODE,
                                                        Lists.immutable.of(
                                                                typeRefNode(Meta.of(Fix.TYPE_VAR_F_NAME), "F"))))))),
                Optional.empty());

        public static ConstructorNode<Attributes> KINDED_NODE = constructorNode(
                Unfix.KINDED_META,
                "Unfix",
                Lists.immutable.of(
                        constructorParamNode(
                                ExampleNodes.Unfix.KINDED_UNFIX_META,
                                "unfix",
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(Fix.TYPE_VAR_F_META, "F"),
                                        Lists.immutable.of(
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                                        ExampleNodes.Fix.KINDED_TYPE_NODE,
                                                        Lists.immutable.of(typeRefNode(Fix.TYPE_VAR_F_META, "F"))))))),
                Optional.empty());
    }
}
