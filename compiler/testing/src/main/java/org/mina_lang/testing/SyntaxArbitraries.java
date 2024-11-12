/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import com.ibm.icu.text.UnicodeSet;
import com.opencastsoftware.yvette.Range;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import net.jqwik.api.arbitraries.StringArbitrary;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.syntax.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SyntaxArbitraries {
    private SyntaxArbitraries() {
    }

    private static Name getName(MetaNode<Attributes> node) {
        return node.meta().meta().name();
    }

    private static Type getType(MetaNode<Attributes> node) {
        return (Type) node.meta().meta().sort();
    }

    private static Set<Meta<Attributes>> getValues(GenEnvironment env) {
        return env.scopes().toList()
            .toReversed()
            .flatCollect(GenScope::values)
            .toSet();
    }

    private static Set<Meta<Attributes>> getValuesWhere(GenEnvironment env, Predicate<Meta<Attributes>> test) {
        return env.scopes().toList()
            .toReversed()
            .flatCollect(GenScope::values)
            .select(test)
            .toSet();
    }

    private static final Arbitrary<String> nameBeginArbitrary = Arbitraries.strings().alpha().ofLength(1);
    private static final Arbitrary<String> nameContinueArbitrary = Arbitraries.strings().alpha().numeric().ofMaxLength(19);

    private static StringArbitrary unicodeStrings(UnicodeSet unicodeSet) {
        var strings = Arbitraries.strings();
        for (var range : unicodeSet.ranges()) {
            if (range.codepoint > Character.MAX_VALUE) { break; }
            char rangeStart = (char) range.codepoint;
            char rangeEnd = range.codepointEnd > Character.MAX_VALUE
                ? Character.MAX_VALUE
                : (char) range.codepointEnd;
            strings = strings.withCharRange(rangeStart, rangeEnd);
        }
        return strings;
    }

    private static final Set<String> reservedWords = Set.of(
        "namespace",
        "import",
        "as",
        "let",
        "if",
        "then",
        "else",
        "case",
        "data",
        "match",
        "with"
    );

    public static final Arbitrary<String> nameArbitrary = Combinators
        .combine(nameBeginArbitrary, nameContinueArbitrary)
        .as((begin, cont) -> begin + cont)
        .filter(name -> !reservedWords.contains(name));

    private static final Arbitrary<Boolean> booleanArbitrary = Arbitraries.of(true, false);

    // Literals
    public static Arbitrary<BooleanNode<Attributes>> booleanNode = booleanArbitrary.map(bool -> {
        return SyntaxNodes.boolNode(Meta.nameless(Type.BOOLEAN), bool);
    });

    public static Arbitrary<CharNode<Attributes>> charNode = Arbitraries.chars().map(character -> {
        return SyntaxNodes.charNode(Meta.nameless(Type.CHAR), character);
    });

    public static Arbitrary<StringNode<Attributes>> stringNode = Arbitraries.strings().ascii().ofMaxLength(50).map(string -> {
        return SyntaxNodes.stringNode(Meta.nameless(Type.STRING), string);
    });

    public static Arbitrary<IntNode<Attributes>> intNode = Arbitraries.integers().greaterOrEqual(0).map(i -> {
        return SyntaxNodes.intNode(Meta.nameless(Type.INT), i);
    });

    public static Arbitrary<LongNode<Attributes>> longNode = Arbitraries.longs().greaterOrEqual(0L).map(l -> {
        return SyntaxNodes.longNode(Meta.nameless(Type.LONG), l);
    });

    public static Arbitrary<FloatNode<Attributes>> floatNode = Arbitraries.floats().greaterOrEqual(0F).map(f -> {
        return SyntaxNodes.floatNode(Meta.nameless(Type.FLOAT), f);
    });

    public static Arbitrary<DoubleNode<Attributes>> doubleNode = Arbitraries.doubles().greaterOrEqual(0D).map(d -> {
        return SyntaxNodes.doubleNode(Meta.nameless(Type.DOUBLE), d);
    });

    public static Arbitrary<BlockNode<Attributes>> unitNode = Arbitraries.just(SyntaxNodes.blockNode(
        Meta.nameless(Type.UNIT),
        Lists.immutable.empty(),
        Optional.empty()
    ));

    public static Arbitrary<ExprNode<Attributes>> literalNode = Arbitraries.oneOf(booleanNode, charNode, stringNode, intNode, longNode, floatNode, doubleNode, unitNode);

    public static Arbitrary<? extends ExprNode<Attributes>> literalWithType(Type typ) {
        if (Type.BOOLEAN.equals(typ)) {
            return booleanNode;
        } else if (Type.CHAR.equals(typ)) {
            return charNode;
        } else if (Type.STRING.equals(typ)) {
            return stringNode;
        } else if (Type.INT.equals(typ)) {
            return intNode;
        } else if (Type.LONG.equals(typ)) {
            return longNode;
        } else if (Type.FLOAT.equals(typ)) {
            return floatNode;
        } else if (Type.DOUBLE.equals(typ)) {
            return doubleNode;
        } else if (Type.UNIT.equals(typ)) {
            return unitNode;
        }

        return null;
    }

    // If expressions
    public static Arbitrary<IfNode<Attributes>> ifNode(GenEnvironment env) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNode(env)
        ).flatAs((cond, cons) -> {
            return exprNodeWithType(env, (Type) cons.meta().meta().sort()).map(alt -> {
                return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
            });
        });
    }

    public static Arbitrary<IfNode<Attributes>> ifNodeWithType(GenEnvironment env, Type typ) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNodeWithType(env, typ), exprNodeWithType(env, typ)
        ).as((cond, cons, alt) -> {
            return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
        });
    }

    // Lambda expressions
    private static ParamNode<Attributes> lambdaParamNode(GenEnvironment env, GenScope scope, String name, boolean ascribed, Type typ) {
        var nameMeta = new LocalName(name, env.localVarIndex().getAndIncrement());
        var meta = Meta.of(new Attributes(nameMeta, typ));
        scope.putValue(name, meta);

        if (typ instanceof BuiltInType builtInType) {
            var ascMeta = Meta.of(new Attributes(new BuiltInName(builtInType.name()), typ.kind()));
            var ascription = Optional.<TypeNode<Attributes>>ofNullable(
                ascribed ? SyntaxNodes.typeRefNode(ascMeta, builtInType.name()) : null);
            return SyntaxNodes.paramNode(meta, name, ascription);
        } else {
            return SyntaxNodes.paramNode(meta, name);
        }
    }

    public static Arbitrary<LambdaNode<Attributes>> lambdaNode(GenEnvironment env) {
        return Arbitraries.integers().between(0, 5).flatMap(numArgs -> {
            return Combinators.combine(
                nameArbitrary.list().ofSize(numArgs).uniqueElements(),
                booleanArbitrary.list().ofSize(numArgs),
                Arbitraries.of(Type.builtIns.stream().toList()).list().ofSize(numArgs)
            ).flatAs((argNames, argsAscribed, argTypes) -> {
                var lambdaScope = new GenScope();

                var argExprs = IntStream.range(0, numArgs).mapToObj(i -> {
                    var name = argNames.get(i);
                    var ascribed = argsAscribed.get(i);
                    var argTyp = argTypes.get(i);
                    return lambdaParamNode(env, lambdaScope, name, ascribed, argTyp);
                }).collect(Collectors2.toImmutableList());

                env.pushScope(lambdaScope);

                return exprNode(env).map(bodyExpr -> {
                    env.popScope(GenScope.class);
                    var upcastArgTypes = argTypes.stream().map(typ -> (Type) typ).collect(Collectors2.toImmutableList());
                    return SyntaxNodes.lambdaNode(
                        Meta.nameless(Type.function(upcastArgTypes, getType(bodyExpr))),
                        argExprs,
                        bodyExpr
                    );
                });
            });
        });
    }

    public static Stream<Arbitrary<LambdaNode<Attributes>>> lambdaNodeWithType(GenEnvironment env, Type typ) {
        if (!Type.isFunction(typ)) { return Stream.empty(); }

        var tyApp = (TypeApply) typ;
        var argTypes = tyApp.typeArguments().take(tyApp.typeArguments().size() - 1);
        var returnType = tyApp.typeArguments().getLast();
        var numArgs = argTypes.size();

        return Stream.of(Combinators.combine(
            nameArbitrary.list().ofSize(numArgs).uniqueElements(),
            booleanArbitrary.list().ofSize(numArgs)
        ).flatAs((argNames, argsAscribed) -> {
            var lambdaScope = new GenScope();

            var argExprs = IntStream.range(0, numArgs).mapToObj(i -> {
                var name = argNames.get(i);
                var ascribed = argsAscribed.get(i);
                var argTyp = argTypes.get(i);
                return lambdaParamNode(env, lambdaScope, name, ascribed, argTyp);
            }).collect(Collectors2.toImmutableList());

            env.pushScope(lambdaScope);

            return exprNodeWithType(env, returnType).map(bodyExpr -> {
                env.popScope(GenScope.class);
                return SyntaxNodes.lambdaNode(
                    Meta.nameless(Type.function(argTypes, getType(bodyExpr))),
                    argExprs,
                    bodyExpr
                );
            });
        }));
    }

    // Function application
    public static Stream<Arbitrary<ApplyNode<Attributes>>> applyNode(GenEnvironment env) {
        Set<Meta<Attributes>> values = getValuesWhere(env, meta -> {
            return Type.isFunction((Type) meta.meta().sort());
        });

        return values.stream().map(fnMeta -> {
            var fnTyp = (TypeApply) fnMeta.meta().sort();
            var fnName = (Named) fnMeta.meta().name();
            var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
            var retTyp = fnTyp.typeArguments().getLast();

            var appMeta = Meta.nameless(retTyp);

            return Combinators.combine(argTyps.toList().collect(argTyp -> {
                return exprNodeWithType(env, argTyp);
            })).as(argExprs -> {
                return SyntaxNodes.applyNode(
                    appMeta,
                    SyntaxNodes.refNode(fnMeta, fnName.localName()),
                    Lists.immutable.ofAll(argExprs)
                );
            });
        });
    }

    public static Stream<Arbitrary<ApplyNode<Attributes>>> applyNodeWithType(GenEnvironment env, Type typ) {
        Set<Meta<Attributes>> values = getValuesWhere(env, meta -> {
            var candidate = (Type) meta.meta().sort();
            if (Type.isFunction(candidate) && candidate instanceof TypeApply fnTyp) {
                return fnTyp.typeArguments().getLast().equals(typ);
            }
            return false;
        });

        return values.stream().map(fnMeta -> {
            var fnTyp = (TypeApply) fnMeta.meta().sort();
            var fnName = (Named) fnMeta.meta().name();
            var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
            var retTyp = fnTyp.typeArguments().getLast();

            var appMeta = Meta.nameless(retTyp);

            return Combinators.combine(argTyps.toList().collect(argTyp -> {
                return exprNodeWithType(env, argTyp);
            })).as(argExprs -> {
                return SyntaxNodes.applyNode(
                    appMeta,
                    SyntaxNodes.refNode(fnMeta, fnName.localName()),
                    Lists.immutable.ofAll(argExprs)
                );
            });
        });
    }

    // Variable references
    public static Arbitrary<ReferenceNode<Attributes>> refNode(GenEnvironment env) {
        return Arbitraries.of(getValues(env)).map(meta -> {
            Named name = (Named) meta.meta().name();
            return SyntaxNodes.refNode(meta, name.localName());
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>>[] refWithType(GenEnvironment env, Type typ) {
        return getValues(env).stream()
            .filter(meta -> meta.meta().sort().equals(typ))
            .map(meta -> {
                Named name = (Named) meta.meta().name();
                return Arbitraries.just(SyntaxNodes.refNode(meta, name.localName()));
            }).toArray(Arbitrary[]::new);
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNode(GenEnvironment env) {
        return Arbitraries.lazy(() -> {
            var generators = Lists.mutable.of(
                Tuple.of(4, literalNode),
                Tuple.of(1, ifNode(env)),
                Tuple.of(1, lambdaNode(env))
            );
            if (env.scopes().collectInt(scope -> scope.values().size()).sum() > 0) {
                generators.add(Tuple.of(1, refNode(env)));
            }
            if (!getValuesWhere(env, meta -> Type.isFunction((Type) meta.meta().sort())).isEmpty()) {
                generators.addAll(applyNode(env).map(gen -> Tuple.of(1, gen)).toList());
            }
            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNodeWithType(GenEnvironment env, Type typ) {
        return Arbitraries.lazy(() -> {
            List<Tuple.Tuple2<Integer, ? extends Arbitrary<? extends ExprNode<Attributes>>>> generators =
                Lists.mutable.empty();

            generators.addAll(Arrays.stream(refWithType(env, typ)).map(gen -> Tuple.of(1, gen)).toList());
            generators.addAll(applyNodeWithType(env, typ).map(gen -> Tuple.of(1, gen)).toList());
            generators.addAll(lambdaNodeWithType(env, typ).map(gen -> Tuple.of(1, gen)).toList());

            var literalGen = literalWithType(typ);
            if (literalGen != null) {
                generators.add(Tuple.of(4, literalGen));
                generators.add(Tuple.of(1, ifNodeWithType(env, typ)));
            }

            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }

    public static Arbitrary<DeclarationNode<Attributes>> declarationNode(GenEnvironment env, NamespaceName nsName) {
        return Combinators.combine(
            nameArbitrary.filter(name -> env.lookupValue(name).isEmpty()),
            exprNode(env)
        ).as((name, expr) -> {
            var letName = new LetName(new QualifiedName(nsName, name));
            var letMeta = Meta.of(new Attributes(letName, expr.meta().meta().sort()));
            return SyntaxNodes.letNode(letMeta, name, expr);
        });
    }

    public static Arbitrary<ImmutableList<DeclarationNode<Attributes>>> declarations(GenEnvironment env, NamespaceName nsName, int depth) {
        return Arbitraries.recursive(
            () -> Arbitraries.just(Lists.immutable.empty()),
            (existingDecls) -> existingDecls.flatMap(decls -> {
                return declarationNode(env, nsName).map(decl -> {
                    env.putValue(decl.name(), decl.meta());
                    return decls.newWith(decl);
                });
            }),
            depth
        );
    }

    public static Arbitrary<NamespaceNode<Attributes>> namespaceNode = Combinators.combine(
        nameArbitrary,
        Arbitraries.integers().between(0, 8)
    ).flatAs((name, declCount) -> {
        var pkg = Lists.immutable.of("Mina", "Test");
        var nsName = new NamespaceName(pkg, name);
        return declarations(new GenEnvironment(), nsName, declCount).map(decls -> {
            return SyntaxNodes.namespaceNode(
                Meta.of(new Attributes(nsName, Type.NAMESPACE)),
                SyntaxNodes.nsIdNode(Range.EMPTY, pkg, name),
                Lists.immutable.empty(), decls
            );
        });
    });
}
