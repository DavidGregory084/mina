package org.mina_lang.typechecker;

import org.eclipse.collections.api.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.Sort;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeConstructor;
import org.mina_lang.common.types.TypeKind;
import org.mina_lang.syntax.LiteralNode;
import org.mina_lang.syntax.ParamNode;
import org.mina_lang.syntax.ReferenceNode;
import org.mina_lang.syntax.TypeNode;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;

public class ExampleNodes {
    public static NamespaceName NAMESPACE_NAME = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");

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
        public static DataName NAME = new DataName(new QualifiedName(NAMESPACE_NAME, "Bool"));
        public static Meta<Attributes> META = Meta.of(new Attributes(NAME, TypeKind.INSTANCE));

        public static TypeNode<Name> NAMED_TYPE_NODE = typeRefNode(Meta.of(NAME), "Bool");

        public static TypeNode<Attributes> KINDED_TYPE_NODE = typeRefNode(
                Meta.of(new Attributes(NAME, TypeKind.INSTANCE)), "Bool");
    }

    public static class True {
        public static ConstructorName NAME = new ConstructorName(
                Bool.NAME,
                new QualifiedName(NAMESPACE_NAME, "True"));

        public static Type TYPE = Type.function(
                Lists.immutable.empty(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> META = Meta.of(new Attributes(NAME, TYPE));

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
                new QualifiedName(NAMESPACE_NAME, "False"));

        public static Type TYPE = Type.function(
                Lists.immutable.empty(),
                new TypeConstructor(Bool.NAME.name(), TypeKind.INSTANCE));

        public static Meta<Attributes> META = Meta.of(new Attributes(NAME, TYPE));

        public static TypeNode<Name> NAMED_TYPE_NODE = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.empty(),
                Bool.NAMED_TYPE_NODE);

        public static TypeNode<Attributes> KINDED_TYPE_NODE = funTypeNode(
                Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                Lists.immutable.empty(),
                Bool.KINDED_TYPE_NODE);
    }
}
