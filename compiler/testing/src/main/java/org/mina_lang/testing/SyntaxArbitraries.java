/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import com.opencastsoftware.yvette.Range;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SyntaxArbitraries {
    private SyntaxArbitraries() {
    }

    private static Name getName(MetaNode<Attributes> node) {
        return node.meta().meta().name();
    }

    private static Name getName(Meta<Attributes> meta) {
        return meta.meta().name();
    }

    private static Type getType(MetaNode<Attributes> node) {
        return (Type) node.meta().meta().sort();
    }

    private static Type getType(Meta<Attributes> meta) {
        return (Type) meta.meta().sort();
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

    private static Set<Meta<Attributes>> getTypes(GenEnvironment env) {
        return env.scopes().toList()
            .toReversed()
            .flatCollect(GenScope::types)
            .toSet();
    }

    private static Set<Meta<Attributes>> getTypesWhere(GenEnvironment env, Predicate<Meta<Attributes>> test) {
        return env.scopes().toList()
            .toReversed()
            .flatCollect(GenScope::types)
            .select(test)
            .toSet();
    }

    private static Map<String, Meta<Attributes>> getFields(GenEnvironment env, ConstructorName constrName) {
        return env.scopes().toList()
            .toReversed().stream()
            .flatMap(scope -> {
                var constrFields = scope.fields().get(constrName);
                return constrFields != null
                    ? constrFields.entrySet().stream()
                    : Stream.empty();
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, Meta<Attributes>> getFieldsWhere(GenEnvironment env, ConstructorName constrName, Predicate<Meta<Attributes>> test) {
        return env.scopes().toList()
            .toReversed().stream()
            .flatMap(scope -> {
                var constrFields = scope.fields().get(constrName);
                return constrFields != null
                    ? constrFields.entrySet().stream()
                    : Stream.empty();
            })
            .filter(entry -> test.accept(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <T> void addWeightedGenerator(List<Tuple.Tuple2<Integer, Arbitrary<? extends T>>> generators, Stream<Arbitrary<? extends T>> arbitraryStream, int weight) {
        var arbitraries = arbitraryStream.toList();
        if (!arbitraries.isEmpty()) {
            generators.add(Tuple.of(weight, Arbitraries.oneOf(arbitraries)));
        }
    }

    private static boolean isFunction(Meta<Attributes> meta) {
        return Type.isFunction(getType(meta));
    }

    private static boolean isFunctionWithReturnType(Meta<Attributes> meta, Type typ) {
        return isFunction(meta) && ((TypeApply) getType(meta)).typeArguments().getLast().equals(typ);
    }

    private static boolean isAtLeastUnaryFunction(Meta<Attributes> meta) {
        return isFunction(meta) && ((TypeApply) getType(meta)).typeArguments().size() > 1;
    }

    private static boolean isFunctionOfBoundType(Meta<Attributes> meta, Type typ) {
        if (!isAtLeastUnaryFunction(meta)) { return false; }
        var fnTyp = (TypeApply) getType(meta);
        var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
        var retTyp = fnTyp.typeArguments().getLast();
        var boundTyp = Type.function(argTyps.drop(1), retTyp);
        return boundTyp.equals(typ);
    }

    private static final Arbitrary<String> nameBeginArbitrary = Arbitraries.strings().alpha().ofLength(1);
    private static final Arbitrary<String> nameContinueArbitrary = Arbitraries.strings().alpha().numeric().ofMaxLength(19);

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

    public static Arbitrary<? extends ExprNode<Attributes>> literalNode = Arbitraries.oneOf(booleanNode, charNode, stringNode, intNode, longNode, floatNode, doubleNode, unitNode);

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
    public static Arbitrary<? extends ExprNode<Attributes>> ifNode(GenEnvironment env) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNode(env)
        ).flatAs((cond, cons) -> {
            return exprNodeWithType(env, getType(cons)).map(alt -> {
                return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
            });
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>> ifNodeWithType(GenEnvironment env, Type typ) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNodeWithType(env, typ), exprNodeWithType(env, typ)
        ).as((cond, cons, alt) -> {
            return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
        });
    }

    // Match expressions
    private static Arbitrary<? extends PatternNode<Attributes>> idPatternNode(GenEnvironment env, GenScope scope, Type scrutineeType) {
        return nameArbitrary.map(name -> {
            var patName = new LocalName(name, env.localVarIndex().getAndIncrement());
            var patMeta = Meta.of(patName, scrutineeType);
            scope.putValue(name, patMeta);
            return SyntaxNodes.idPatternNode(patMeta, name);
        });
    }

    private static Arbitrary<? extends PatternNode<Attributes>> aliasPatternNode(GenEnvironment env, GenScope scope, Type scrutineeType) {
        return nameArbitrary.flatMap(name -> {
            var patName = new LocalName(name, env.localVarIndex().getAndIncrement());
            var patMeta = Meta.of(patName, scrutineeType);
            scope.putValue(name, patMeta);
            return patternNode(env, scope, scrutineeType).map(pattern -> {
                return SyntaxNodes.aliasPatternNode(patMeta, name, pattern);
            });
        });
    }

    private static Stream<Arbitrary<? extends PatternNode<Attributes>>> literalPatternNode(GenEnvironment env, Type scrutineeType) {
        var literal = literalWithType(scrutineeType);

        if (literal == null || scrutineeType.equals(Type.UNIT))  { return Stream.empty(); }

        return Stream.of(literal).map(literalArb -> {
            return literalArb.map(literalNode -> {
                return SyntaxNodes.literalPatternNode(
                    Meta.nameless(scrutineeType),
                    (LiteralNode<Attributes>) literalNode
                );
            });
        });
    }

    private static Arbitrary<FieldPatternNode<Attributes>> fieldPatternNode(GenEnvironment env, GenScope scope, String fieldName, Meta<Attributes> fieldMeta) {
        var fieldType = getType(fieldMeta);
        return patternNode(env, scope, fieldType).optional().map(pat -> {
            if (pat.isEmpty()) {
                scope.putValue(fieldName, fieldMeta);
            }
            return SyntaxNodes.fieldPatternNode(fieldMeta, fieldName, pat);
        });
    }

    private static Arbitrary<ImmutableList<FieldPatternNode<Attributes>>> fieldPatternNodes(GenEnvironment env, GenScope scope, ConstructorName constrName) {
        var constrFields = getFields(env, constrName);
        return Arbitraries.subsetOf(constrFields.entrySet()).flatMap(fieldSet -> {
            var fieldPatterns = fieldSet.stream()
                .map(entry -> fieldPatternNode(env, scope, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
           return Combinators.combine(fieldPatterns).as(Lists.immutable::ofAll);
        });
    }

    private static Stream<Arbitrary<? extends PatternNode<Attributes>>> constructorPatternNode(GenEnvironment env, GenScope scope, Type scrutineeType) {
        if (scrutineeType instanceof TypeConstructor tyCon) {
            var dataMeta = env.lookupType(tyCon.name().name());

            if (dataMeta.isEmpty()) { return Stream.empty(); }

            var dataName = getName(dataMeta.get());

            var dataConstructors = getValuesWhere(env, meta -> {
                var metaName = getName(meta);
                return metaName instanceof ConstructorName constrName &&
                    constrName.enclosing().equals(dataName);
            });

            return Stream.of(
                Arbitraries.of(dataConstructors)
                    .flatMap(constrMeta -> {
                        var constrName = (ConstructorName) getName(constrMeta);
                        var constrId = SyntaxNodes.idNode(Range.EMPTY, constrName.name().name());
                        var constrType = (TypeApply) getType(constrMeta);
                        var constrPatMeta = Meta.of(constrName, constrType.typeArguments().getLast());
                        return fieldPatternNodes(env, scope, constrName).map(fieldPatterns -> {
                            return SyntaxNodes.constructorPatternNode(constrPatMeta, constrId, fieldPatterns);
                        });
                    })
            );
        } else {
            return Stream.empty();
        }
    }

    private static Arbitrary<PatternNode<Attributes>> patternNode(GenEnvironment env, GenScope scope, Type scrutineeType) {
        List<Tuple.Tuple2<Integer, Arbitrary<? extends PatternNode<Attributes>>>> generators =
            Lists.mutable.of(
                Tuple.of(1, idPatternNode(env, scope, scrutineeType)),
                Tuple.of(1, aliasPatternNode(env, scope, scrutineeType))
            );

        addWeightedGenerator(generators, literalPatternNode(env, scrutineeType), 2);
        addWeightedGenerator(generators, constructorPatternNode(env, scope, scrutineeType), 2);

        return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
    }

    private static Arbitrary<CaseNode<Attributes>> caseNodeWithType(GenEnvironment env, GenScope caseScope, Type scrutineeType, Type resultType) {
        return patternNode(env, caseScope, scrutineeType).flatMap(pattern -> {
            return exprNodeWithType(env, resultType).map(expr -> {
                return SyntaxNodes.caseNode(Meta.nameless(resultType), pattern, expr);
            });
        });
    }

    private static Arbitrary<CaseNode<Attributes>> fallbackCaseNodeWithType(GenEnvironment env, Type scrutineeType, Type resultType) {
        var caseScope = new GenScope();
        var envWithCaseScope = env.withScope(caseScope);
        return idPatternNode(envWithCaseScope, caseScope, scrutineeType).flatMap(pattern -> {
            return exprNodeWithType(envWithCaseScope, resultType).map(expr -> {
                return SyntaxNodes.caseNode(Meta.nameless(resultType), pattern, expr);
            });
        });
    }

    public static Arbitrary<ImmutableList<CaseNode<Attributes>>> caseNodesWithType(GenEnvironment env, Type scrutineeType, Type resultType, int depth) {
        return Arbitraries.recursive(
            () -> Arbitraries.just(Lists.immutable.empty()),
            (existingDecls) -> existingDecls.flatMap(decls -> {
                var caseScope = new GenScope();
                var envWithCaseScope = env.withScope(caseScope);
                return caseNodeWithType(envWithCaseScope, caseScope, scrutineeType, resultType).map(caseNode -> {
                    return decls.newWith(caseNode);
                });
            }),
            depth
        );
    }

    private static Arbitrary<? extends ExprNode<Attributes>> matchNode(GenEnvironment env) {
        return Arbitraries.of(Type.builtIns.toSet()).flatMap(typ -> matchNodeWithType(env, typ));
    }

    private static Arbitrary<MatchNode<Attributes>> matchNodeWithType(GenEnvironment env, Type resultType) {
        return Combinators.combine(
            Arbitraries.integers().between(0, 4),
            exprNode(env)
        ).flatAs((caseCount, scrutinee) -> {
            var scrutineeType = getType(scrutinee);
            return caseNodesWithType(env, scrutineeType, resultType, caseCount).flatMap(caseNodes -> {
                return fallbackCaseNodeWithType(env, scrutineeType, resultType).map(fallbackCase -> {
                    return SyntaxNodes.matchNode(
                        Meta.nameless(resultType),
                        scrutinee,
                        Lists.immutable.ofAll(caseNodes).newWith(fallbackCase)
                    );
                });
            });
        });
    }

    // Lambda expressions
    private static ParamNode<Attributes> lambdaParamNode(GenEnvironment env, GenScope scope, String name, boolean ascribed, Type typ) {
        var nameMeta = new LocalName(name, env.localVarIndex().getAndIncrement());
        var meta = Meta.of(nameMeta, typ);
        scope.putValue(name, meta);

        if (typ instanceof BuiltInType builtInType) {
            var ascMeta = Meta.of(new BuiltInName(builtInType.name()), typ.kind());
            var ascription = Optional.<TypeNode<Attributes>>ofNullable(
                ascribed ? SyntaxNodes.typeRefNode(ascMeta, builtInType.name()) : null);
            return SyntaxNodes.paramNode(meta, name, ascription);
        } else {
            return SyntaxNodes.paramNode(meta, name);
        }
    }

    public static Arbitrary<? extends ExprNode<Attributes>> lambdaNode(GenEnvironment env) {
        return Arbitraries.integers().between(0, 5).flatMap(numArgs -> {
            return Combinators.combine(
                nameArbitrary.list().ofSize(numArgs).uniqueElements(),
                booleanArbitrary.list().ofSize(numArgs),
                Arbitraries.of(Type.builtIns.toSet()).list().ofSize(numArgs)
            ).flatAs((argNames, argsAscribed, argTypes) -> {
                var lambdaScope = new GenScope();

                var argExprs = IntStream.range(0, numArgs).mapToObj(i -> {
                    var name = argNames.get(i);
                    var ascribed = argsAscribed.get(i);
                    var argTyp = argTypes.get(i);
                    return lambdaParamNode(env, lambdaScope, name, ascribed, argTyp);
                }).collect(Collectors2.toImmutableList());

                var envWithLambdaScope = env.withScope(lambdaScope);

                return exprNode(envWithLambdaScope).map(bodyExpr -> {
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

    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> lambdaNodeWithType(GenEnvironment env, Type typ) {
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

            var envWithLambdaScope = env.withScope(lambdaScope);

            return exprNodeWithType(envWithLambdaScope, returnType).map(bodyExpr -> {
                return SyntaxNodes.lambdaNode(
                    Meta.nameless(Type.function(argTypes, getType(bodyExpr))),
                    argExprs,
                    bodyExpr
                );
            });
        }));
    }

    // Blocks
    public static Arbitrary<LetNode<Attributes>> blockLetNode(GenEnvironment env, GenScope scope) {
        return Combinators.combine(
            nameArbitrary.filter(name -> scope.lookupValue(name).isEmpty()),
            exprNode(env)
        ).as((name, expr) -> {
            var letName = new LocalName(name, env.localVarIndex().getAndIncrement());
            var letMeta = Meta.of(letName, getType(expr));
            return SyntaxNodes.letNode(letMeta, name, expr);
        });
    }

    public static Arbitrary<ImmutableList<LetNode<Attributes>>> blockLetDeclarations(GenEnvironment env, GenScope scope, int depth) {
        return Arbitraries.recursive(
            () -> Arbitraries.just(Lists.immutable.empty()),
            (existingDecls) -> existingDecls.flatMap(decls -> {
                return blockLetNode(env, scope).map(decl -> {
                    scope.putValue(decl.name(), decl.meta());
                    return decls.newWith(decl);
                });
            }),
            depth
        );
    }

    public static Arbitrary<? extends ExprNode<Attributes>> blockNode(GenEnvironment env) {
        return Arbitraries.integers().between(0, 3).flatMap(letCount -> {
            var blockScope = new GenScope();
            var envWithBlockScope = env.withScope(blockScope);
            return blockLetDeclarations(envWithBlockScope, blockScope, letCount).flatMap(letDecls -> {
                return exprNode(envWithBlockScope).optional().map(maybeResult -> {
                    return maybeResult.map(result -> {
                        var blockMeta = Meta.nameless(getType(result));
                        return SyntaxNodes.blockNode(blockMeta, letDecls, result);
                    }).orElseGet(() -> {
                        var blockMeta = Meta.nameless(Type.UNIT);
                        return SyntaxNodes.blockNode(blockMeta, letDecls, Optional.empty());
                    });
                });
            });
        });
    }

    public static Arbitrary<BlockNode<Attributes>> blockNodeWithType(GenEnvironment env, Type typ) {
        return Arbitraries.integers().between(0, 3).flatMap(letCount -> {
            var blockScope = new GenScope();
            var envWithBlockScope = env.withScope(blockScope);
            return blockLetDeclarations(envWithBlockScope, blockScope, letCount).flatMap(letDecls -> {
                return exprNodeWithType(envWithBlockScope, typ).map(result -> {
                    var blockMeta = Meta.nameless(getType(result));
                    return SyntaxNodes.blockNode(blockMeta, letDecls, result);
                });
            });
        });
    }

    // Function binding (UFCS)
    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> selectNode(GenEnvironment env) {
        var values = getValuesWhere(env, SyntaxArbitraries::isAtLeastUnaryFunction);

        return values.stream().map(fnMeta -> {
            var fnName = (Named) getName(fnMeta);
            var fnTyp = (TypeApply) getType(fnMeta);
            var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
            var retTyp = fnTyp.typeArguments().getLast();

            var selectMeta = Meta.nameless(Type.function(argTyps.drop(1), retTyp));

            return exprNodeWithType(env, argTyps.getFirst()).map(receiver -> {
                return SyntaxNodes.selectNode(
                    selectMeta,
                    receiver,
                    SyntaxNodes.refNode(fnMeta, fnName.localName())
                );
            });
        });
    }

    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> selectNodeWithType(GenEnvironment env, Type typ) {
        if (!Type.isFunction(typ)) { return Stream.empty(); }

        var values = getValuesWhere(env, meta -> isFunctionOfBoundType(meta, typ));

        return values.stream().map(fnMeta -> {
            var fnName = (Named) getName(fnMeta);
            var fnTyp = (TypeApply) getType(fnMeta);
            var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
            var retTyp = fnTyp.typeArguments().getLast();

            var selectMeta = Meta.nameless(Type.function(argTyps.drop(1), retTyp));

            return exprNodeWithType(env, argTyps.getFirst()).map(receiver -> {
                return SyntaxNodes.selectNode(
                    selectMeta,
                    receiver,
                    SyntaxNodes.refNode(fnMeta, fnName.localName())
                );
            });
        });
    }

    public static Stream<Arbitrary<SelectNode<Attributes>>> selectNodeWithReturnType(GenEnvironment env, Type typ) {
        if (!Type.isFunction(typ)) { return Stream.empty(); }

        var values = getValuesWhere(env, meta -> isAtLeastUnaryFunction(meta) && isFunctionWithReturnType(meta, typ));

        return values.stream().map(fnMeta -> {
            var fnName = (Named) getName(fnMeta);
            var fnTyp = (TypeApply) getType(fnMeta);
            var argTyps = fnTyp.typeArguments().take(fnTyp.typeArguments().size() - 1);
            var retTyp = fnTyp.typeArguments().getLast();

            var selectMeta = Meta.nameless(Type.function(argTyps.drop(1), retTyp));

            return exprNodeWithType(env, argTyps.getFirst()).map(receiver -> {
                return SyntaxNodes.selectNode(
                    selectMeta,
                    receiver,
                    SyntaxNodes.refNode(fnMeta, fnName.localName())
                );
            });
        });
    }

    // Function application
    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> applyNode(GenEnvironment env) {
        Set<Meta<Attributes>> values = getValuesWhere(env, SyntaxArbitraries::isFunction);

        return values.stream().map(fnMeta -> {
            var fnName = (Named) getName(fnMeta);
            var fnTyp = (TypeApply) getType(fnMeta);
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

    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> selectApplyNode(GenEnvironment env) {
        return selectNode(env).map(selectArb -> {
            return selectArb.flatMap(selectNode -> {
                var selectTyp = (TypeApply) getType(selectNode);
                var argTyps = selectTyp.typeArguments().take(selectTyp.typeArguments().size() - 1);
                var retTyp = selectTyp.typeArguments().getLast();

                var appMeta = Meta.nameless(retTyp);

                return Combinators.combine(argTyps.toList().collect(argTyp -> {
                    return exprNodeWithType(env, argTyp);
                })).as(argExprs -> {
                    return SyntaxNodes.applyNode(
                        appMeta,
                        selectNode,
                        Lists.immutable.ofAll(argExprs)
                    );
                });
            });
        });
    }

    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> applyNodeWithType(GenEnvironment env, Type typ) {
        Set<Meta<Attributes>> values = getValuesWhere(env, meta -> isFunctionWithReturnType(meta, typ));

        return values.stream().map(fnMeta -> {
            var fnName = (Named) getName(fnMeta);
            var fnTyp = (TypeApply) getType(fnMeta);
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

    public static Stream<Arbitrary<? extends ExprNode<Attributes>>> selectApplyNodeWithType(GenEnvironment env, Type typ) {
        return selectNodeWithReturnType(env, typ).map(selectArb -> {
            return selectArb.flatMap(selectNode -> {
                var selectTyp = (TypeApply) getType(selectNode);
                var argTyps = selectTyp.typeArguments().take(selectTyp.typeArguments().size() - 1);
                var retTyp = selectTyp.typeArguments().getLast();

                var appMeta = Meta.nameless(retTyp);

                return Combinators.combine(argTyps.toList().collect(argTyp -> {
                    return exprNodeWithType(env, argTyp);
                })).as(argExprs -> {
                    return SyntaxNodes.applyNode(
                        appMeta,
                        selectNode,
                        Lists.immutable.ofAll(argExprs)
                    );
                });
            });
        });
    }

    // Variable references
    public static Arbitrary<ReferenceNode<Attributes>> refNode(GenEnvironment env) {
        return Arbitraries.of(getValues(env)).map(meta -> {
            Named name = (Named) getName(meta);
            return SyntaxNodes.refNode(meta, name.localName());
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>>[] refWithType(GenEnvironment env, Type typ) {
        return getValues(env).stream()
            .filter(meta -> getType(meta).equals(typ))
            .map(meta -> {
                Named name = (Named) getName(meta);
                return Arbitraries.just(SyntaxNodes.refNode(meta, name.localName()));
            }).toArray(Arbitrary[]::new);
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNode(GenEnvironment env) {
        return Arbitraries.lazy(() -> {
            List<Tuple.Tuple2<Integer, Arbitrary<? extends ExprNode<Attributes>>>> generators =
                Lists.mutable.of(
                    Tuple.of(8, literalNode),
                    Tuple.of(1, ifNode(env)),
                    Tuple.of(1, lambdaNode(env)),
                    Tuple.of(1, blockNode(env)),
                    Tuple.of(1, matchNode(env))
                );

            if (!getValues(env).isEmpty()) {
                generators.add(Tuple.of(4, refNode(env)));
            }
            if (!getValuesWhere(env, SyntaxArbitraries::isFunction).isEmpty()) {
                addWeightedGenerator(generators, applyNode(env), 1);
            }
            if (!getValuesWhere(env, SyntaxArbitraries::isAtLeastUnaryFunction).isEmpty()) {
                addWeightedGenerator(generators, selectNode(env), 1);
                addWeightedGenerator(generators, selectApplyNode(env), 1);
            }

            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNodeWithType(GenEnvironment env, Type typ) {
        return Arbitraries.lazy(() -> {
            List<Tuple.Tuple2<Integer, Arbitrary<? extends ExprNode<Attributes>>>> generators =
                Lists.mutable.empty();

            addWeightedGenerator(generators, Arrays.stream(refWithType(env, typ)), 1);
            addWeightedGenerator(generators, applyNodeWithType(env, typ), 1);
            addWeightedGenerator(generators, lambdaNodeWithType(env, typ), 1);
            addWeightedGenerator(generators, selectNodeWithType(env, typ), 1);
            addWeightedGenerator(generators, selectApplyNodeWithType(env, typ), 1);

            var literalGen = literalWithType(typ);
            if (literalGen != null) {
                generators.add(Tuple.of(8, literalGen));
                generators.add(Tuple.of(1, ifNodeWithType(env, typ)));
                generators.add(Tuple.of(1, blockNodeWithType(env, typ)));
                generators.add(Tuple.of(1, matchNodeWithType(env, typ)));
            }

            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }

    // Data declarations
    public static boolean classNameCollides(GenEnvironment env, NamespaceName nsName, String name) {
        return name.equals(nsName.name()) ||
            env.lookupValue(name).isPresent() ||
            env.lookupType(name).isPresent();
    }

    public static boolean classNameCollides(GenEnvironment env, NamespaceName nsName, DataName dataName, String name) {
        return name.equals(nsName.name()) ||
            name.equals(dataName.name().name()) ||
            env.lookupValue(name).isPresent() ||
            env.lookupType(name).isPresent();
    }

    private static ConstructorParamNode<Attributes> constructorParamNode(ConstructorName constrName, String name, BuiltInType typ) {
        var nameMeta = new FieldName(constrName, name);
        var meta = Meta.of(nameMeta, typ);
        var ascMeta = Meta.of(new BuiltInName(typ.name()), typ.kind());
        var ascription = SyntaxNodes.typeRefNode(ascMeta, typ.name());
        return SyntaxNodes.constructorParamNode(meta, name, ascription);
    }

    public static Arbitrary<ConstructorNode<Attributes>> constructorNode(GenEnvironment env, NamespaceName nsName, DataName dataName) {
        return Arbitraries.integers().between(0, 3).flatMap(numArgs -> {
            return Combinators.combine(
                nameArbitrary.filter(name -> !classNameCollides(env, nsName, dataName, name)),
                nameArbitrary.list().ofSize(numArgs).uniqueElements(),
                Arbitraries.of(Type.builtIns.toSet()).list().ofSize(numArgs)
            ).as((constructorName, fieldNames, fieldTypes) -> {
                var dataType = new TypeConstructor(dataName.name(), TypeKind.INSTANCE);

                var constrName = new ConstructorName(dataName, new QualifiedName(nsName, constructorName));
                var constrMeta = Meta.of(constrName, Type.function(Lists.immutable.ofAll(fieldTypes), dataType));

                var fields = IntStream.range(0, numArgs).mapToObj(i -> {
                    var name = fieldNames.get(i);
                    var argTyp = fieldTypes.get(i);
                    return constructorParamNode(constrName, name, argTyp);
                }).collect(Collectors2.toImmutableList());

                return SyntaxNodes.constructorNode(
                    constrMeta,
                    constructorName,
                    fields,
                    Optional.empty()
                );
            });
        });
    }

    public static Arbitrary<DataNode<Attributes>> dataNode(GenEnvironment env, NamespaceName nsName) {
        return nameArbitrary.filter(name -> !classNameCollides(env, nsName, name)).flatMap(name -> {
            var dataName = new DataName(new QualifiedName(nsName, name));

            var constructorNodes = constructorNode(env, nsName, dataName).list()
                .ofMaxSize(3)
                .uniqueElements(it -> it.name());

            return constructorNodes.map(constructors -> {
                var dataMeta = Meta.of(dataName, TypeKind.INSTANCE);
                return SyntaxNodes.dataNode(dataMeta, name, Lists.immutable.empty(), Lists.immutable.ofAll(constructors));
            });
        });
    }

    // Let declarations
    public static Arbitrary<LetNode<Attributes>> letNode(GenEnvironment env, NamespaceName nsName) {
        return Combinators.combine(
            nameArbitrary.filter(name -> env.lookupValue(name).isEmpty()),
            exprNode(env)
        ).as((name, expr) -> {
            var letName = new LetName(new QualifiedName(nsName, name));
            var letMeta = Meta.of(letName, getType(expr));
            return SyntaxNodes.letNode(letMeta, name, expr);
        });
    }

    // Declarations
    public static Arbitrary<DeclarationNode<Attributes>> declarationNode(GenEnvironment env, NamespaceName nsName) {
        return Arbitraries.frequencyOf(
            Tuple.of(1, dataNode(env, nsName)),
            Tuple.of(3, letNode(env, nsName))
        );
    }

    public static Arbitrary<ImmutableList<DeclarationNode<Attributes>>> declarations(GenEnvironment env, NamespaceName nsName, int depth) {
        return Arbitraries.recursive(
            () -> Arbitraries.just(Lists.immutable.empty()),
            (existingDecls) -> existingDecls.flatMap(decls -> {
                return declarationNode(env, nsName).map(decl -> {
                    if (decl instanceof LetNode<Attributes> let) {
                        env.putValue(let.name(), let.meta());
                    } else if (decl instanceof DataNode<Attributes> data) {
                        env.putType(data.name(), data.meta());
                        data.constructors().forEach(constr -> {
                            var constrName = (ConstructorName) getName(constr);
                            env.putValue(constr.name(), constr.meta());
                            constr.params().forEach(constrParam -> {
                                env.putField(constrName, constrParam.name(), constrParam.meta());
                            });
                        });
                    }
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
        var env = new GenEnvironment();
        var pkg = Lists.immutable.of("Mina", "Test");
        var nsName = new NamespaceName(pkg, name);
        return declarations(env, nsName, declCount).map(decls -> {
            return SyntaxNodes.namespaceNode(
                Meta.of(nsName, Type.NAMESPACE),
                SyntaxNodes.nsIdNode(Range.EMPTY, pkg, name),
                Lists.immutable.empty(), decls
            );
        });
    });
}
