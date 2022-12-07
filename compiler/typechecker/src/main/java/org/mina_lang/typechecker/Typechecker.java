package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.*;
import org.mina_lang.common.scopes.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

public class Typechecker {

    private DiagnosticCollector diagnostics;
    private TypeEnvironment environment;
    private UnsolvedVariableSupply varSupply;
    private Kindchecker kindchecker;
    private TypePrinter typePrinter = new TypePrinter();

    public Typechecker(DiagnosticCollector diagnostics, TypeEnvironment environment) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        this.varSupply = new UnsolvedVariableSupply();
        this.kindchecker = new Kindchecker(diagnostics, environment, varSupply);
    }

    public TypeEnvironment getEnvironment() {
        return environment;
    }

    <A> A withScope(Scope<Attributes> scope, Supplier<A> fn) {
        environment.pushScope(scope);
        var result = fn.get();
        environment.popScope(scope.getClass());
        return result;
    }

    void withScope(Scope<Attributes> scope, Runnable fn) {
        environment.pushScope(scope);
        fn.run();
        environment.popScope(scope.getClass());
        return;
    }

    UnsolvedKind newUnsolvedKind() {
        var newUnsolved = varSupply.newUnsolvedKind();
        environment.putUnsolvedKind(newUnsolved);
        return newUnsolved;
    }

    UnsolvedType newUnsolvedType() {
        var newUnsolved = varSupply.newUnsolvedType(newUnsolvedKind());
        environment.putUnsolvedType(newUnsolved);
        return newUnsolved;
    }

    UnsolvedType newUnsolvedType(Kind kind) {
        var newUnsolved = varSupply.newUnsolvedType(kind);
        environment.putUnsolvedType(newUnsolved);
        return newUnsolved;
    }

    public NamespaceNode<Attributes> typecheck(NamespaceNode<Name> namespace) {
        return inferNamespace(namespace);
    }

    public DeclarationNode<Attributes> typecheck(DeclarationNode<Name> node) {
        return inferDeclaration(node);
    }

    public ExprNode<Attributes> typecheck(ExprNode<Name> node) {
        return inferExpr(node);
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
        return meta.withMeta(attributes);
    }

    void mismatchedType(Range range, Type actualType, Type expectedType) {
        var expected = expectedType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(typePrinter);

        var actual = actualType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(typePrinter);

        var message = Doc.group(
                Doc.text("Mismatched type!")
                        .appendLineOrSpace(Doc.text("Expected: ").append(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual: ").append(actual)))
                .render(80);

        diagnostics.reportError(range, message);
    }

    void mismatchedApplication(Range range, Type actualType, Type expectedType) {
        var expected = expectedType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(typePrinter);

        var actual = actualType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(typePrinter);

        var message = Doc.group(
                Doc.text("Mismatched application!")
                        .appendLineOrSpace(Doc.text("Expected:").appendSpace(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual:").appendSpace(actual)))
                .render(80);

        diagnostics.reportError(range, message);
    }

    void instantiateAsSubType(UnsolvedType unsolved, Type superType) {
        withScope(new InstantiateTypeScope<>(), () -> {
            if (superType instanceof UnsolvedType otherUnsolved) {
                // Complete and Easy's InstLReach rule
                environment.solveType(otherUnsolved, unsolved);
            } else if (superType instanceof ForAllVar forall) {
                // Complete and Easy's InstLSolve rule
                environment.solveType(unsolved, forall);
            } else if (superType instanceof ExistsVar exists) {
                environment.solveType(unsolved, exists);
            } else if (superType instanceof BuiltInType builtInSup) {
                environment.solveType(unsolved, builtInSup);
            } else if (superType instanceof TypeConstructor tyConSup) {
                environment.solveType(unsolved, tyConSup);
            } else if (Type.isFunction(superType) &&
                    superType instanceof TypeApply funTypeSup) {
                // Complete and Easy's InstLArr rule
                var funTypeArgs = funTypeSup
                        .typeArguments()
                        .take(funTypeSup.typeArguments().size() - 1)
                        .collect(arg -> newUnsolvedType(TypeKind.INSTANCE));

                var funTypeReturn = newUnsolvedType(TypeKind.INSTANCE);

                var funType = Type.function(
                        funTypeArgs.collect(tyArg -> (Type) tyArg),
                        funTypeReturn);

                environment.solveType(unsolved, funType);

                funTypeArgs
                        .zip(funTypeSup.typeArguments())
                        .forEach(pair -> {
                            instantiateAsSuperType(
                                    pair.getOne(),
                                    pair.getTwo().substitute(
                                            environment.typeSubstitution(),
                                            environment.kindSubstitution()));
                        });

                instantiateAsSubType(
                        funTypeReturn,
                        funTypeSup.typeArguments().getLast()
                                .substitute(environment.typeSubstitution(), environment.kindSubstitution()));

            } else if (superType instanceof TypeApply tyAppSup) {
                // Complete and Easy's InstLArr rule extended to other type constructors
                var tyAppArgs = tyAppSup
                        .typeArguments()
                        .collect(arg -> newUnsolvedType());

                var tyAppSub = new TypeApply(
                        tyAppSup.type(),
                        tyAppArgs.collect(arg -> (Type) arg),
                        tyAppSup.kind());

                environment.solveType(unsolved, tyAppSub);

                tyAppArgs
                        .zip(tyAppSup.typeArguments())
                        .forEach(pair -> {
                            instantiateAsSubType(
                                    pair.getOne(),
                                    pair.getTwo().substitute(
                                            environment.typeSubstitution(),
                                            environment.kindSubstitution()));
                        });
            }
        });
    }

    void instantiateAsSuperType(UnsolvedType unsolved, Type subType) {
        withScope(new InstantiateTypeScope<>(), () -> {
            if (subType instanceof UnsolvedType otherUnsolved) {
                // Complete and Easy's InstRReach rule
                environment.solveType(otherUnsolved, unsolved);
            } else if (subType instanceof ForAllVar forall) {
                // Complete and Easy's InstRSolve rule
                environment.solveType(unsolved, forall);
            } else if (subType instanceof ExistsVar exists) {
                environment.solveType(unsolved, exists);
            } else if (subType instanceof BuiltInType builtIn) {
                environment.solveType(unsolved, builtIn);
            } else if (subType instanceof TypeConstructor tyCon) {
                environment.solveType(unsolved, tyCon);
            } else if (Type.isFunction(subType) &&
                    subType instanceof TypeApply funTypeSub) {
                // Complete and Easy's InstLArr rule
                var funTypeArgs = funTypeSub
                        .typeArguments()
                        .take(funTypeSub.typeArguments().size() - 1)
                        .collect(arg -> newUnsolvedType(TypeKind.INSTANCE));

                var funTypeReturn = newUnsolvedType(TypeKind.INSTANCE);

                var funType = Type.function(
                        funTypeArgs.collect(tyArg -> (Type) tyArg),
                        funTypeReturn);

                environment.solveType(unsolved, funType);

                funTypeArgs
                        .zip(funTypeSub.typeArguments())
                        .forEach(pair -> {
                            instantiateAsSubType(
                                    pair.getOne(),
                                    pair.getTwo().substitute(
                                            environment.typeSubstitution(),
                                            environment.kindSubstitution()));
                        });

                instantiateAsSuperType(
                        funTypeReturn,
                        funTypeSub.typeArguments().getLast()
                                .substitute(environment.typeSubstitution(), environment.kindSubstitution()));

            } else if (subType instanceof TypeApply tyAppSub) {
                // Complete and Easy's InstRArr rule extended to other type constructors
                var tyAppArgs = tyAppSub
                        .typeArguments()
                        .collect(arg -> newUnsolvedType());

                var tyAppSup = new TypeApply(
                        tyAppSub.type(),
                        tyAppArgs.collect(arg -> (Type) arg),
                        tyAppSub.kind());

                environment.solveType(unsolved, tyAppSup);

                tyAppArgs
                        .zip(tyAppSub.typeArguments())
                        .forEach(pair -> {
                            instantiateAsSuperType(
                                    pair.getOne(),
                                    pair.getTwo().substitute(
                                            environment.typeSubstitution(),
                                            environment.kindSubstitution()));
                        });
            }
        });
    }

    boolean checkSubType(Type subType, Type superType) {
        return withScope(new CheckSubtypeScope<>(), () -> {
            var solvedSubType = subType
                    .substitute(environment.typeSubstitution(), environment.kindSubstitution());
            var solvedSuperType = superType
                    .substitute(environment.typeSubstitution(), environment.kindSubstitution());

            if (solvedSubType instanceof ForAllVar subTy &&
                    solvedSuperType instanceof ForAllVar supTy &&
                    subTy.name().equals(supTy.name())) {
                // Complete and Easy's <:Var rule
                return true;
            } else if (solvedSubType instanceof ExistsVar subTy &&
                    solvedSuperType instanceof ExistsVar supTy &&
                    subTy.name().equals(supTy.name())) {
                return true;
            } else if (solvedSubType instanceof BuiltInType subTy &&
                    solvedSuperType instanceof BuiltInType supTy &&
                    subTy.equals(supTy)) {
                return true;
            } else if (solvedSubType instanceof TypeConstructor subTy &&
                    solvedSuperType instanceof TypeConstructor supTy &&
                    subTy.name().equals(supTy.name())) {
                return true;
            } else if (solvedSubType instanceof UnsolvedType unsolvedSub &&
                    solvedSuperType instanceof UnsolvedType unsolvedSuper &&
                    unsolvedSub.id() == unsolvedSuper.id()) {
                // Complete and Easy's <:Exvar rule
                return true;
            } else if (solvedSubType instanceof UnsolvedType unsolvedSub
                    && !unsolvedSub.isFreeIn(solvedSuperType)) {
                // Complete and Easy's <:InstantiateL rule
                instantiateAsSubType(unsolvedSub, solvedSuperType);

                return true;

            } else if (solvedSuperType instanceof UnsolvedType unsolvedSup
                    && !unsolvedSup.isFreeIn(solvedSubType)) {
                // Complete and Easy's <:InstantiateR rule
                instantiateAsSuperType(unsolvedSup, solvedSubType);

                return true;

            } else if (Type.isFunction(solvedSubType) && solvedSubType instanceof TypeApply tyAppSub &&
                    Type.isFunction(solvedSuperType) && solvedSuperType instanceof TypeApply tyAppSup &&
                    tyAppSub.typeArguments().size() == tyAppSup.typeArguments().size()) {
                // Complete and Easy's <:-> rule
                var argsSubTyped = tyAppSub.typeArguments().take(tyAppSub.typeArguments().size() - 1)
                        .zip(tyAppSup.typeArguments().take(tyAppSup.typeArguments().size() - 1))
                        .allSatisfy(pair -> {
                            return checkSubType(pair.getTwo(), pair.getOne());
                        });

                var resultSubTyped = checkSubType(
                        tyAppSub.typeArguments().getLast(),
                        tyAppSup.typeArguments().getLast());

                return argsSubTyped && resultSubTyped;
            } else if (solvedSubType instanceof TypeApply tyAppSub &&
                    solvedSuperType instanceof TypeApply tyAppSup &&
                    tyAppSub.typeArguments().size() == tyAppSup.typeArguments().size()) {
                // Complete and Easy's <:-> rule extended to other type constructors
                var tyConSubTyped = checkSubType(tyAppSub.type(), tyAppSup.type());

                var tyArgsSubTyped = tyAppSub.typeArguments()
                        .zip(tyAppSup.typeArguments())
                        .allSatisfy(pair -> {
                            return checkSubType(pair.getOne(), pair.getTwo());
                        });

                return tyConSubTyped && tyArgsSubTyped;

            } else {
                return false;
            }
        });
    }

    NamespaceScope<Attributes> populateTopLevel(NamespaceNode<Name> namespace) {
        var currentNamespace = (NamespaceName) namespace.meta().meta();
        var namespaceScope = new NamespaceScope<Attributes>(currentNamespace);

        namespace.declarations().forEach(decl -> {
            if (decl instanceof DataNode<Name> data) {
                var dataName = (DataName) data.meta().meta();
                var dataKind = newUnsolvedKind();
                var dataMeta = updateMetaWith(data.meta(), dataKind);

                namespaceScope.putType(dataName.localName(), dataMeta);
                namespaceScope.putType(dataName.canonicalName(), dataMeta);

                data.constructors().forEach(constr -> {
                    var constrName = (ConstructorName) constr.meta().meta();
                    var constrType = newUnsolvedType(dataKind);
                    var constrMeta = updateMetaWith(data.meta(), constrType);

                    namespaceScope.putValue(constrName.localName(), constrMeta);
                    namespaceScope.putValue(constrName.canonicalName(), constrMeta);

                    constr.params().forEach(constrParam -> {
                        var fieldType = newUnsolvedType(TypeKind.INSTANCE);
                        var fieldMeta = updateMetaWith(constrParam.meta(), fieldType);

                        namespaceScope.putField(constrName, constrParam.name(), fieldMeta);
                    });
                });

            } else if (decl instanceof LetFnNode<Name> letFn) {
                var letFnName = (LetName) letFn.meta().meta();
                var letFnType = newUnsolvedType();
                var letFnMeta = updateMetaWith(letFn.meta(), letFnType);

                namespaceScope.putValue(letFnName.localName(), letFnMeta);
                namespaceScope.putValue(letFnName.canonicalName(), letFnMeta);

            } else if (decl instanceof LetNode<Name> let) {
                var letName = (LetName) let.meta().meta();
                var letType = newUnsolvedType();
                var letMeta = updateMetaWith(let.meta(), letType);

                namespaceScope.putValue(letName.localName(), letMeta);
                namespaceScope.putValue(letName.canonicalName(), letMeta);
            }
        });

        return namespaceScope;
    }

    Type createConstructorType(
            DataNode<Attributes> data,
            ConstructorNode<Attributes> constr) {
        var dataName = (DataName) data.meta().meta().name();
        var dataKind = (Kind) data.meta().meta().sort();

        var typeFolder = new TypeAnnotationFolder(environment);

        var typeParamTypes = data.typeParams()
                .collect(tyParam -> typeFolder.visitTypeVar(tyParam));

        var constrParamTypes = constr.params()
                .collect(param -> typeFolder.visitType(param.typeAnnotation()));

        var constrReturnType = constr.type()
                .map(typ -> typeFolder.visitType(typ))
                .orElseGet(() -> {
                    var tyCon = new TypeConstructor(dataName.name(), dataKind);
                    return new TypeApply(tyCon, typeParamTypes, TypeKind.INSTANCE);
                });

        var constrFnType = Type.function(constrParamTypes, constrReturnType);

        if (typeParamTypes.isEmpty()) {
            return constrFnType;
        } else {
            return new TypeLambda(
                    typeParamTypes.collect(tyParam -> (TypeVar) tyParam),
                    constrFnType,
                    dataKind);
        }
    }

    Type createLetFnType(
            ImmutableList<TypeVar> typeParamTypes,
            ImmutableList<ParamNode<Attributes>> valueParams,
            ExprNode<Attributes> bodyExpr) {

        var valueParamTypes = valueParams.collect(param -> {
            var paramType = environment.lookupValue(param.name()).get();
            return (Type) paramType.meta().sort();
        });

        var bodyType = (Type) bodyExpr.meta().meta().sort();

        var functionType = Type.function(valueParamTypes, bodyType);

        if (typeParamTypes.isEmpty()) {
            return functionType;
        } else {
            return new TypeLambda(
                    typeParamTypes,
                    functionType,
                    new HigherKind(typeParamTypes.collect(param -> param.kind()), TypeKind.INSTANCE));
        }
    }

    <A> A withTypeParams(
            Supplier<ImmutableList<TypeVarNode<Name>>> getTyParams,
            Function3<ImmutableList<TypeVarNode<Attributes>>, ImmutableList<TypeVar>, TypeAnnotationFolder, A> fn) {
        var typeFolder = new TypeAnnotationFolder(environment);
        var tyParams = getTyParams.get();

        if (tyParams.isEmpty()) {
            return fn.value(Lists.immutable.empty(), Lists.immutable.empty(), typeFolder);
        } else {
            return withScope(new TypeLambdaScope<>(), () -> {
                var kindedTyParams = tyParams
                        .collect(tyParam -> (TypeVarNode<Attributes>) kindchecker.kindcheck(tyParam));
                var tyParamTypes = kindedTyParams
                        .collect(tyParam -> (TypeVar) typeFolder.visitTypeVar(tyParam));

                return fn.value(kindedTyParams, tyParamTypes, typeFolder);
            });
        }
    }

    NamespaceNode<Attributes> inferNamespace(NamespaceNode<Name> namespace) {
        return withScope(populateTopLevel(namespace), () -> {
            var updatedMeta = updateMetaWith(namespace.meta(), Type.NAMESPACE);
            var inferredDecls = namespace.declarations().collect(this::inferDeclaration);
            return namespaceNode(updatedMeta, namespace.id(), namespace.imports(), inferredDecls);
        });
    }

    DeclarationNode<Attributes> inferDeclaration(DeclarationNode<Name> declaration) {
        if (declaration instanceof DataNode<Name> data) {
            var kindedData = kindchecker.kindcheck(data);

            var dataName = (DataName) kindedData.meta().meta().name();
            environment.putType(dataName.localName(), kindedData.meta());
            environment.putType(dataName.canonicalName(), kindedData.meta());

            kindedData.constructors().forEach(kindedConstr -> {
                var constrName = (ConstructorName) kindedConstr.meta().meta().name();
                var constrType = createConstructorType(kindedData, kindedConstr);
                var constrAttrs = kindedConstr.meta().meta().withSort(constrType);
                var constrTypeMeta = kindedConstr.meta().withMeta(constrAttrs);
                environment.putValue(constrName.localName(), constrTypeMeta);
                environment.putValue(constrName.canonicalName(), constrTypeMeta);
            });

            return kindedData;

        } else if (declaration instanceof LetFnNode<Name> letFn) {
            return withTypeParams(letFn::typeParams, (tyParams, tyParamTypes, typeFolder) -> {
                return withScope(new LambdaScope<>(), () -> {
                    var letFnName = (LetName) letFn.meta().meta();

                    var checkedParams = letFn.valueParams()
                            .collect(param -> inferParam(typeFolder, param));
                    var checkedReturn = letFn.returnType()
                            .map(kindchecker::inferType);

                    var expectedType = checkedReturn.map(typeFolder::visitType);

                    var checkedBody = expectedType
                            .map(expected -> checkExpr(letFn.expr(), expected))
                            .orElseGet(() -> inferExpr(letFn.expr()));

                    var letFnType = createLetFnType(tyParamTypes, checkedParams, checkedBody);

                    var typedMeta = updateMetaWith(letFn.meta(), letFnType);
                    environment.putValue(letFnName.localName(), typedMeta);
                    environment.putValue(letFnName.canonicalName(), typedMeta);

                    return letFnNode(typedMeta, letFn.name(), tyParams, checkedParams, checkedReturn, checkedBody);
                });
            });
        } else if (declaration instanceof LetNode<Name> let) {
            var letName = (LetName) let.meta().meta();

            return let.type().map(typ -> {
                var kindedType = kindchecker.kindcheck(typ);
                var expectedType = kindedType.accept(new TypeAnnotationFolder(environment));
                var checkedExpr = checkExpr(let.expr(), expectedType);
                var checkedType = (Type) checkedExpr.meta().meta().sort();
                var typedMeta = updateMetaWith(let.meta(), checkedType);
                environment.putValue(letName.localName(), typedMeta);
                environment.putValue(letName.canonicalName(), typedMeta);
                return letNode(typedMeta, let.name(), kindedType, checkedExpr);
            }).orElseGet(() -> {
                var inferredExpr = inferExpr(let.expr());
                var inferredType = (Type) inferredExpr.meta().meta().sort();
                var typedMeta = updateMetaWith(let.meta(), inferredType);
                environment.putValue(letName.localName(), typedMeta);
                environment.putValue(letName.canonicalName(), typedMeta);
                return letNode(typedMeta, let.name(), Optional.empty(), inferredExpr);
            });
        }

        return null;
    }

    ParamNode<Attributes> inferParam(TypeAnnotationFolder typeFolder, ParamNode<Name> param) {
        var checkedAnnotation = param.typeAnnotation()
                .map(tyAnn -> kindchecker.kindcheck(tyAnn));

        var paramType = checkedAnnotation
                .map(kindedAnn -> kindedAnn.accept(typeFolder))
                .orElseGet(() -> newUnsolvedType(TypeKind.INSTANCE));

        var updatedMeta = updateMetaWith(param.meta(), paramType);

        environment.putValue(param.name(), updatedMeta);

        return paramNode(updatedMeta, param.name(), checkedAnnotation);
    }

    ExprNode<Attributes> inferExpr(ExprNode<Name> expr) {
        if (expr instanceof BlockNode<Name> block) {

        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);
            var consequent = inferExpr(ifExpr.consequent());
            var consequentType = (Type) consequent.meta().meta().sort();
            var alternative = checkExpr(ifExpr.alternative(), consequentType);
            var updatedMeta = updateMetaWith(ifExpr.meta(), consequentType);
            return ifNode(updatedMeta, condition, consequent, alternative);
        } else if (expr instanceof LambdaNode<Name> lambda) {

        } else if (expr instanceof MatchNode<Name> match) {

        } else if (expr instanceof LiteralNode<Name> literal) {
            return inferLiteral(literal);
        } else if (expr instanceof ApplyNode<Name> apply) {
            // var inferredExpr = inferExpr(apply.expr());
            // var inferredType = (Type) inferredExpr.meta().meta().sort();
            // if (Type.isFunction(inferredType)) {
            //     var funType = (TypeApply) inferredType;
            //     var argTypes = funType.typeArguments().take(funType.typeArguments().size() - 1);
            //     apply.args().collect(this::inferExpr).zip(argTypes);
            // }
        } else if (expr instanceof ReferenceNode<Name> reference) {
            var envType = environment.lookupValue(reference.id().canonicalName()).get();
            var updatedMeta = updateMetaWith(reference.meta(), envType.meta().sort());

            return refNode(updatedMeta, reference.id());
        }

        return null;
    }

    LiteralNode<Attributes> inferLiteral(LiteralNode<Name> literal) {
        if (literal instanceof BooleanNode<Name> bool) {
            var updatedMeta = updateMetaWith(bool.meta(), Type.BOOLEAN);
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var updatedMeta = updateMetaWith(chr.meta(), Type.CHAR);
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var updatedMeta = updateMetaWith(str.meta(), Type.STRING);
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var updatedMeta = updateMetaWith(intgr.meta(), Type.INT);
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var updatedMeta = updateMetaWith(lng.meta(), Type.LONG);
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var updatedMeta = updateMetaWith(flt.meta(), Type.FLOAT);
            return floatNode(updatedMeta, flt.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var updatedMeta = updateMetaWith(dbl.meta(), Type.DOUBLE);
            return doubleNode(updatedMeta, dbl.value());
        }

        return null;
    }

    ExprNode<Attributes> checkExpr(ExprNode<Name> expr, Type expectedType) {
        if (expr instanceof LambdaNode<Name> lambda) {

        } else if (expr instanceof LiteralNode<Name> literal) {
            return checkLiteral(literal, expectedType);
        } else {
            var inferredExpr = inferExpr(expr);
            var inferredType = (Type) inferredExpr.meta().meta().sort();

            // TODO: Add an error type to place in Meta when we have a mismatch?
            if (!checkSubType(inferredType, expectedType)) {
                mismatchedType(inferredExpr.meta().range(), inferredType, expectedType);
            }

            return inferredExpr;
        }

        return null;
    }

    LiteralNode<Attributes> checkLiteral(LiteralNode<Name> literal, Type expectedType) {
        // TODO: Add an error type to place in Meta when we have a mismatch?
        if (literal instanceof BooleanNode<Name> bool) {
            var actualType = Type.BOOLEAN;
            var updatedMeta = updateMetaWith(bool.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(bool.range(), actualType, expectedType);
            }
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var actualType = Type.CHAR;
            var updatedMeta = updateMetaWith(chr.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(chr.range(), actualType, expectedType);
            }
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var actualType = Type.STRING;
            var updatedMeta = updateMetaWith(str.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(str.range(), actualType, expectedType);
            }
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var actualType = Type.INT;
            var updatedMeta = updateMetaWith(intgr.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(intgr.range(), actualType, expectedType);
            }
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var actualType = Type.LONG;
            var updatedMeta = updateMetaWith(lng.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(lng.range(), actualType, expectedType);
            }
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var actualType = Type.FLOAT;
            var updatedMeta = updateMetaWith(flt.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(flt.range(), actualType, expectedType);
            }
            return floatNode(updatedMeta, flt.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var actualType = Type.DOUBLE;
            var updatedMeta = updateMetaWith(dbl.meta(), actualType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(dbl.range(), actualType, expectedType);
            }
            return doubleNode(updatedMeta, dbl.value());
        }

        return null;
    }
}
