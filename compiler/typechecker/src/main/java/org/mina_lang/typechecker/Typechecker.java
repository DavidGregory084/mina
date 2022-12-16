package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;
import java.util.function.Function;
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
    private SortSubstitutionTransformer sortTransformer;
    private SortPrinter sortPrinter = new SortPrinter(new KindPrinter(), new TypePrinter());

    public Typechecker(DiagnosticCollector diagnostics, TypeEnvironment environment) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        this.varSupply = new UnsolvedVariableSupply();
        this.kindchecker = new Kindchecker(diagnostics, environment, varSupply);
        this.sortTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(),
                environment.kindSubstitution());
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
        var metaTransformer = new MetaNodeSubstitutionTransformer(sortTransformer);
        return inferNamespace(namespace).accept(metaTransformer);
    }

    public DeclarationNode<Attributes> typecheck(DeclarationNode<Name> node) {
        var metaTransformer = new MetaNodeSubstitutionTransformer(sortTransformer);
        return inferDeclaration(node).accept(metaTransformer);
    }

    public ExprNode<Attributes> typecheck(ExprNode<Name> node) {
        var metaTransformer = new MetaNodeSubstitutionTransformer(sortTransformer);
        return inferExpr(node).accept(metaTransformer);
    }

    Kind getKind(MetaNode<Attributes> node) {
        return (Kind) node.meta().meta().sort();
    }

    Type getType(MetaNode<Attributes> node) {
        return (Type) node.meta().meta().sort();
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var substituted = sort.accept(sortTransformer);
        var attributes = meta.meta().withSort(substituted);
        return meta.withMeta(attributes);
    }

    void putTypeDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putType(name.localName(), meta);
        scope.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putValue(name.localName(), meta);
        scope.putValue(name.canonicalName(), meta);
        return;
    }

    void putTypeDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putType(name.localName(), meta);
        environment.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putValue(name.localName(), meta);
        environment.putValue(name.canonicalName(), meta);
        return;
    }

    void mismatchedType(Range range, Type actualType, Type expectedType) {
        var expected = expectedType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(sortPrinter);

        var actual = actualType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(sortPrinter);

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
                .accept(sortPrinter);

        var actual = actualType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(sortPrinter);

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
            } else if (superType instanceof TypeLambda tyLam) {
                // Complete and Easy's InstLAllR rule
                instantiateAsSubType(unsolved, tyLam.instantiateAsSuperTypeIn(environment, varSupply));
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
            } else if (subType instanceof TypeLambda tyLam) {
                // Complete and Easy's InstRAllL rule
                instantiateAsSuperType(unsolved, tyLam.instantiateAsSubTypeIn(environment, varSupply));
            }
        });
    }

    boolean checkSubType(Type subType, Type superType) {
        return withScope(new CheckSubtypeScope<>(), () -> {
            var solvedSubType = subType.substitute(
                    environment.typeSubstitution(),
                    environment.kindSubstitution());

            var solvedSuperType = superType.substitute(
                    environment.typeSubstitution(),
                    environment.kindSubstitution());

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
                    && !unsolvedSub.isFreeIn(solvedSuperType)
                    && kindchecker.checkSubKind(unsolvedSub.kind(), solvedSuperType.kind())) {
                // Complete and Easy's <:InstantiateL rule
                instantiateAsSubType(unsolvedSub, solvedSuperType);

                return true;

            } else if (solvedSuperType instanceof UnsolvedType unsolvedSup
                    && !unsolvedSup.isFreeIn(solvedSubType)
                    && kindchecker.checkSubKind(solvedSubType.kind(), unsolvedSup.kind())) {
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
            } else if (solvedSuperType instanceof TypeLambda tyLam) {
                // Complete and Easy's <:ForallR rule
                return checkSubType(solvedSubType, tyLam.instantiateAsSuperTypeIn(environment, varSupply));

            } else if (solvedSubType instanceof TypeLambda tyLam) {
                // Complete and Easy's <:ForallL rule
                return checkSubType(tyLam.instantiateAsSubTypeIn(environment, varSupply), solvedSuperType);

            } else {
                return false;
            }
        });
    }

    NamespaceScope<Attributes> populateTopLevel(NamespaceNode<Name> namespace) {
        var currentNamespace = (NamespaceName) namespace.meta().meta();
        var namespaceScope = new NamespaceScope<Attributes>(currentNamespace);

        namespace.declarations().forEach(decl -> {
            if (decl instanceof LetFnNode<Name> letFn) {
                var letFnType = newUnsolvedType(TypeKind.INSTANCE);
                var letFnMeta = updateMetaWith(letFn.meta(), letFnType);
                putValueDeclaration(namespaceScope, letFnMeta);

            } else if (decl instanceof LetNode<Name> let) {
                var letType = newUnsolvedType(TypeKind.INSTANCE);
                var letMeta = updateMetaWith(let.meta(), letType);
                putValueDeclaration(namespaceScope, letMeta);

            } else if (decl instanceof DataNode<Name> data) {
                var dataKind = newUnsolvedKind();
                var dataMeta = updateMetaWith(data.meta(), dataKind);

                putTypeDeclaration(namespaceScope, dataMeta);

                data.constructors().forEach(constr -> {
                    var constrName = (ConstructorName) constr.meta().meta();
                    var constrType = newUnsolvedType(dataKind);
                    var constrMeta = updateMetaWith(constr.meta(), constrType);

                    putValueDeclaration(namespaceScope, constrMeta);

                    constr.params().forEach(constrParam -> {
                        var fieldType = newUnsolvedType(TypeKind.INSTANCE);
                        var fieldMeta = updateMetaWith(constrParam.meta(), fieldType);

                        namespaceScope.putField(constrName, constrParam.name(), fieldMeta);
                    });
                });
            }
        });

        return namespaceScope;
    }

    Type createConstructorType(
            DataNode<Attributes> data,
            ConstructorNode<Attributes> constr) {
        var dataName = (DataName) data.meta().meta().name();
        var dataKind = getKind(data);

        var typeFolder = new TypeAnnotationFolder(environment);

        var typeParamTypes = data.typeParams()
                .collect(tyParam -> typeFolder.visitTypeVar(tyParam));

        var constrParamTypes = constr.params()
                .collect(param -> typeFolder.visitType(param.typeAnnotation()));

        var constrReturnType = constr.type()
                .map(typ -> typeFolder.visitType(typ))
                .orElseGet(() -> {
                    var tyCon = new TypeConstructor(dataName.name(), dataKind);
                    return typeParamTypes.isEmpty() ? tyCon
                            : new TypeApply(tyCon, typeParamTypes, TypeKind.INSTANCE);
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

    Type createFieldType(
            DataNode<Attributes> data,
            ConstructorParamNode<Attributes> constrParam) {
        var typeFolder = new TypeAnnotationFolder(environment);
        data.typeParams().forEach(tyParam -> typeFolder.visitTypeVar(tyParam));
        return typeFolder.visitType(constrParam.typeAnnotation());
    }

    Type createLetFnType(
            ImmutableList<TypeVar> typeParamTypes,
            ImmutableList<ParamNode<Attributes>> valueParams,
            Type returnType) {

        var valueParamTypes = valueParams.collect(param -> {
            var paramType = environment.lookupValue(param.name()).get();
            return (Type) paramType.meta().sort();
        });

        var functionType = Type.function(valueParamTypes, returnType);

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
                        .collect(tyParam -> (TypeVarNode<Attributes>) kindchecker.inferType(tyParam));
                var tyParamTypes = kindedTyParams
                        .collect(tyParam -> (TypeVar) typeFolder.visitTypeVar(tyParam));

                return fn.value(kindedTyParams, tyParamTypes, typeFolder);
            });
        }
    }

    <A> A withPolyInstantiation(Type inferredType, Function<Type, A> fn) {
        if (inferredType instanceof TypeLambda tyLam) {
            return withScope(new InstantiateTypeScope<>(), () -> {
                // Keep instantiating binders until we have something else
                return withPolyInstantiation(tyLam.instantiateAsSubTypeIn(environment, varSupply), fn);
            });
        } else {
            return fn.apply(inferredType);
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

            putTypeDeclaration(kindedData.meta());

            kindedData.constructors().forEach(kindedConstr -> {
                var constrType = createConstructorType(kindedData, kindedConstr);
                var constrAttrs = kindedConstr.meta().meta().withSort(constrType);
                var constrTypeMeta = kindedConstr.meta().withMeta(constrAttrs);

                putValueDeclaration(constrTypeMeta);

                kindedConstr.params().forEach(constrParam -> {
                    var constrName = (ConstructorName) kindedConstr.meta().meta().name();
                    var fieldType = createFieldType(kindedData, constrParam);
                    var fieldAttrs = constrParam.meta().meta().withSort(fieldType);
                    var fieldTypeMeta = constrParam.meta().withMeta(fieldAttrs);

                    environment.putField(constrName, constrParam.name(), fieldTypeMeta);
                });
            });

            return kindedData;

        } else if (declaration instanceof LetFnNode<Name> letFn) {
            var letFnNode = withTypeParams(letFn::typeParams, (tyParams, tyParamTypes, typeFolder) -> {
                return withScope(new LambdaScope<>(), () -> {
                    var inferredParams = letFn.valueParams()
                            .collect(param -> inferParam(typeFolder, param));
                    var kindedReturn = letFn.returnType()
                            .map(kindchecker::kindcheck);

                    var expectedType = kindedReturn.map(typeFolder::visitType);

                    var expectedLetFnType = createLetFnType(
                            tyParamTypes,
                            inferredParams,
                            expectedType.orElseGet(() -> newUnsolvedType(TypeKind.INSTANCE)));

                    putValueDeclaration(updateMetaWith(letFn.meta(), expectedLetFnType));

                    var checkedBody = expectedType
                            .map(expected -> checkExpr(letFn.expr(), expected))
                            .orElseGet(() -> inferExpr(letFn.expr()));

                    var letFnType = createLetFnType(
                            tyParamTypes,
                            inferredParams,
                            getType(checkedBody));

                    var typedMeta = updateMetaWith(letFn.meta(), letFnType);

                    return letFnNode(typedMeta, letFn.name(), tyParams, inferredParams, kindedReturn, checkedBody);
                });
            });

            putValueDeclaration(letFnNode.meta());

            return letFnNode;

        } else if (declaration instanceof LetNode<Name> let) {
            var letNode = let.type().map(typ -> {
                var kindedType = kindchecker.kindcheck(typ);
                var expectedType = kindedType.accept(new TypeAnnotationFolder(environment));

                putValueDeclaration(updateMetaWith(let.meta(), expectedType));

                var checkedExpr = checkExpr(let.expr(), expectedType);

                var typedMeta = updateMetaWith(let.meta(), expectedType);

                return letNode(typedMeta, let.name(), kindedType, checkedExpr);
            }).orElseGet(() -> {
                var inferredExpr = inferExpr(let.expr());
                var inferredType = getType(inferredExpr);

                var typedMeta = updateMetaWith(let.meta(), inferredType);

                return letNode(typedMeta, let.name(), Optional.empty(), inferredExpr);
            });

            putValueDeclaration(letNode.meta());

            return letNode;
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
            return withScope(new BlockScope<>(), () -> {
                var inferredDeclarations = block
                        .declarations()
                        .collect(let -> (LetNode<Attributes>) inferDeclaration(let));

                var inferredResult = block.result().map(this::inferExpr);
                var inferredType = inferredResult.map(this::getType).orElse(Type.UNIT);

                var updatedMeta = updateMetaWith(block.meta(), inferredType);

                return blockNode(updatedMeta, inferredDeclarations, inferredResult);
            });
        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);

            var consequent = inferExpr(ifExpr.consequent());
            var consequentType = getType(consequent);

            var alternative = checkExpr(ifExpr.alternative(), consequentType);

            var updatedMeta = updateMetaWith(ifExpr.meta(), consequentType);

            return ifNode(updatedMeta, condition, consequent, alternative);
        } else if (expr instanceof LambdaNode<Name> lambda) {
            return withScope(new LambdaScope<>(), () -> {
                var typeFolder = new TypeAnnotationFolder(environment);

                var inferredArgs = lambda.params()
                        .collect(param -> inferParam(typeFolder, param));

                var inferredBody = inferExpr(lambda.body());

                var inferredType = Type.function(
                        inferredArgs.collect(this::getType),
                        getType(inferredBody));

                var updatedMeta = updateMetaWith(lambda.meta(), inferredType);

                return lambdaNode(updatedMeta, inferredArgs, inferredBody);
            });
        } else if (expr instanceof MatchNode<Name> match) {
            var scrutinee = inferExpr(match.scrutinee());
            var scrutineeType = getType(scrutinee);

            var firstCase = match.cases()
                    .getFirstOptional()
                    .map(cse -> inferCase(cse, scrutineeType));

            var firstCaseType = firstCase
                    .map(this::getType);

            var restCases = firstCaseType
                    .map(inferredType -> {
                        return match.cases()
                                .drop(1)
                                .collect(restCase -> checkCase(restCase, scrutineeType, inferredType));
                    });

            var cases = firstCase.flatMap(first -> {
                return restCases.map(rest -> {
                    return Lists.immutable
                            .of(first)
                            .newWithAll(rest);
                });
            }).orElseGet(() -> Lists.immutable.empty());

            var matchType = firstCaseType
                    .orElseGet(() -> newUnsolvedType(TypeKind.INSTANCE));

            var updatedMeta = updateMetaWith(match.meta(), matchType);

            return matchNode(updatedMeta, scrutinee, cases);

        } else if (expr instanceof LiteralNode<Name> literal) {
            return inferLiteral(literal);
        } else if (expr instanceof ApplyNode<Name> apply) {
            var inferredExpr = inferExpr(apply.expr());

            return withPolyInstantiation(getType(inferredExpr), inferredType -> {
                if (Type.isFunction(inferredType) &&
                        inferredType instanceof TypeApply funType &&
                        apply.args().size() == (funType.typeArguments().size() - 1)) {

                    var checkedArgs = apply.args()
                            .zip(funType.typeArguments().take(funType.typeArguments().size() - 1))
                            .collect(pair -> checkExpr(pair.getOne(), pair.getTwo()));

                    var updatedMeta = updateMetaWith(apply.meta(), funType.typeArguments().getLast());

                    return applyNode(updatedMeta, inferredExpr, checkedArgs);
                } else {
                    var unsolvedArgs = apply.args()
                            .collect(arg -> newUnsolvedType(TypeKind.INSTANCE));

                    var unsolvedReturn = newUnsolvedType(TypeKind.INSTANCE);

                    var appliedType = Type.function(
                            unsolvedArgs.collect(arg -> (Type) arg),
                            unsolvedReturn);

                    // Don't need to do an occurs check here as these are fresh unsolved types
                    if (inferredType instanceof UnsolvedType unsolved) {
                        instantiateAsSubType(unsolved, appliedType);
                    } else if (Type.isFunction(inferredType) &&
                            inferredType instanceof TypeApply funType) {
                        // We have an argument mismatch, but we can instantiate the return type
                        // to improve type errors
                        instantiateAsSubType(unsolvedReturn, funType.typeArguments().getLast());
                    }

                    var checkedArgs = apply.args()
                            .zip(unsolvedArgs)
                            .collect(pair -> checkExpr(pair.getOne(), pair.getTwo()));

                    if (!(inferredType instanceof UnsolvedType)) {
                        mismatchedApplication(apply.range(), appliedType, inferredType);
                    }

                    var updatedMeta = updateMetaWith(apply.meta(), unsolvedReturn);

                    return applyNode(updatedMeta, inferredExpr, checkedArgs);
                }
            });
        } else if (expr instanceof ReferenceNode<Name> reference) {
            var envType = environment.lookupValue(reference.id().canonicalName()).get();
            var updatedMeta = updateMetaWith(reference.meta(), envType.meta().sort());

            return refNode(updatedMeta, reference.id());
        }

        return null;
    }

    CaseNode<Attributes> inferCase(CaseNode<Name> cse, Type scrutineeType) {
        return withScope(new CaseScope<>(), () -> {
            var inferredPattern = checkPattern(cse.pattern(), scrutineeType);
            var inferredConsequent = inferExpr(cse.consequent());
            var inferredType = getType(inferredConsequent);
            var updatedMeta = updateMetaWith(cse.meta(), inferredType);
            return caseNode(updatedMeta, inferredPattern, inferredConsequent);
        });
    }

    PatternNode<Attributes> inferPattern(PatternNode<Name> pattern) {
        var enclosingCase = environment.enclosingCase().get();

        if (pattern instanceof IdPatternNode<Name> idPat) {
            var unsolvedType = newUnsolvedType(TypeKind.INSTANCE);
            var updatedMeta = updateMetaWith(idPat.meta(), unsolvedType);
            enclosingCase.putValue(idPat.name(), updatedMeta);

            return idPatternNode(updatedMeta, idPat.name());

        } else if (pattern instanceof AliasPatternNode<Name> aliasPat) {
            var inferredPattern = inferPattern(aliasPat.pattern());
            var inferredType = getType(inferredPattern);
            var updatedMeta = updateMetaWith(aliasPat.meta(), inferredType);
            enclosingCase.putValue(aliasPat.alias(), updatedMeta);

            return aliasPatternNode(updatedMeta, aliasPat.alias(), inferredPattern);

        } else if (pattern instanceof LiteralPatternNode<Name> litPat) {
            var inferredLiteral = inferLiteral(litPat.literal());
            var inferredType = getType(inferredLiteral);
            var updatedMeta = updateMetaWith(litPat.meta(), inferredType);

            return literalPatternNode(updatedMeta, inferredLiteral);

        } else if (pattern instanceof ConstructorPatternNode<Name> constrPat) {
            var constrMeta = environment.lookupValue(constrPat.id().canonicalName()).get();
            var constrName = (ConstructorName) constrMeta.meta().name();
            var constrType = (Type) constrMeta.meta().sort();

            // We need to keep this instantiation to use with our field patterns
            var instantiator = (constrType instanceof TypeLambda tyLam)
                    ? Optional.of(tyLam.subTypeInstantiationIn(environment, varSupply))
                    : Optional.<TypeInstantiationTransformer>empty();

            if (constrType instanceof TypeLambda tyLam) {
                constrType = tyLam.body().accept(instantiator.get());
            }

            if (Type.isFunction(constrType) && constrType instanceof TypeApply tyApp) {
                constrType = tyApp.typeArguments().getLast();
            }

            var inferredFields = constrPat.fields()
                    .collect(field -> inferFieldPattern(constrName, field, instantiator));

            var updatedMeta = updateMetaWith(constrPat.meta(), constrType);

            return constructorPatternNode(updatedMeta, constrPat.id(), inferredFields);
        }

        return null;
    }

    FieldPatternNode<Attributes> inferFieldPattern(ConstructorName constrName, FieldPatternNode<Name> fieldPat,
            Optional<TypeInstantiationTransformer> instantiator) {
        var enclosingCase = environment.enclosingCase().get();

        var fieldMeta = environment.lookupField(constrName, fieldPat.field()).get();
        var fieldType = (Type) fieldMeta.meta().sort();
        var instantiatedFieldType = instantiator.map(inst -> fieldType.accept(inst)).orElse(fieldType);

        var inferredPattern = fieldPat.pattern().map(pattern -> checkPattern(pattern, instantiatedFieldType));

        var updatedMeta = updateMetaWith(fieldPat.meta(), instantiatedFieldType);

        if (fieldPat.pattern().isEmpty()) {
            enclosingCase.putValue(fieldPat.field(), updatedMeta);
        }

        return fieldPatternNode(updatedMeta, fieldPat.field(), inferredPattern);
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
        if (expectedType instanceof TypeLambda tyLam) {
            return withScope(new InstantiateTypeScope<>(), () -> {
                return checkExpr(expr, tyLam.instantiateAsSuperTypeIn(environment, varSupply));
            });
        } else if (expr instanceof BlockNode<Name> block) {
            return withScope(new BlockScope<>(), () -> {
                var inferredDeclarations = block
                        .declarations()
                        .collect(let -> (LetNode<Attributes>) inferDeclaration(let));

                var checkedResult = block.result().map(res -> checkExpr(res, expectedType));

                if (checkedResult.isEmpty() && !checkSubType(Type.UNIT, expectedType)) {
                    mismatchedType(block.range(), Type.UNIT, expectedType);
                }

                var updatedMeta = updateMetaWith(block.meta(), expectedType);

                return blockNode(updatedMeta, inferredDeclarations, checkedResult);
            });
        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);
            var consequent = checkExpr(ifExpr.consequent(), expectedType);
            var alternative = checkExpr(ifExpr.alternative(), expectedType);

            var updatedMeta = updateMetaWith(ifExpr.meta(), expectedType);

            return ifNode(updatedMeta, condition, consequent, alternative);

        } else if (expr instanceof LambdaNode<Name> lambda &&
                expectedType instanceof TypeApply funType &&
                lambda.params().size() == (funType.typeArguments().size() - 1)) {
            return withScope(new LambdaScope<>(), () -> {
                var knownParams = lambda.params()
                        .zip(funType.typeArguments().take(funType.typeArguments().size() - 1))
                        .collect(pair -> {
                            var param = pair.getOne();
                            var kindedAnnotation = param.typeAnnotation().map(kindchecker::kindcheck);
                            var annotatedType = kindedAnnotation
                                    .map(annot -> annot.accept(new TypeAnnotationFolder(environment)));
                            var updatedMeta = annotatedType
                                    .map(annotType -> updateMetaWith(param.meta(), annotType))
                                    .orElseGet(() -> updateMetaWith(param.meta(), pair.getTwo()));
                            environment.putValue(param.name(), updatedMeta);
                            return paramNode(updatedMeta, param.name(), kindedAnnotation);
                        });

                var checkedReturn = checkExpr(lambda.body(), funType.typeArguments().getLast());

                var appliedType = Type.function(
                        knownParams.collect(this::getType),
                        getType(checkedReturn));

                // We could make this error more local by checking the expected types of the
                // params
                // against their annotations above, but I think this gives more context to the
                // error
                if (!checkSubType(appliedType, expectedType)) {
                    mismatchedType(lambda.range(), appliedType, expectedType);
                }

                var updatedMeta = updateMetaWith(lambda.meta(), expectedType);

                return lambdaNode(updatedMeta, knownParams, checkedReturn);
            });
        } else if (expr instanceof MatchNode<Name> match) {
            var scrutinee = inferExpr(match.scrutinee());
            var scrutineeType = getType(scrutinee);

            var cases = match.cases()
                    .collect(cse -> checkCase(cse, scrutineeType, expectedType));

            var updatedMeta = updateMetaWith(match.meta(), expectedType);

            return matchNode(updatedMeta, scrutinee, cases);

        } else if (expr instanceof LiteralNode<Name> literal) {
            return checkLiteral(literal, expectedType);
        } else {
            var inferredExpr = inferExpr(expr);
            var inferredType = getType(inferredExpr);

            // TODO: Add an error type to place in Meta when we have a mismatch?
            if (!checkSubType(inferredType, expectedType)) {
                mismatchedType(inferredExpr.range(), inferredType, expectedType);
            }

            return inferredExpr;
        }
    }

    CaseNode<Attributes> checkCase(CaseNode<Name> cse, Type scrutineeType, Type expectedType) {
        return withScope(new CaseScope<>(), () -> {
            var inferredPattern = checkPattern(cse.pattern(), scrutineeType);
            var inferredConsequent = checkExpr(cse.consequent(), expectedType);
            var inferredType = getType(inferredConsequent);
            var updatedMeta = updateMetaWith(cse.meta(), inferredType);
            return caseNode(updatedMeta, inferredPattern, inferredConsequent);
        });
    }

    PatternNode<Attributes> checkPattern(PatternNode<Name> pattern, Type expectedType) {
        if (expectedType instanceof TypeLambda tyLam) {
            return withScope(new InstantiateTypeScope<>(), () -> {
                return checkPattern(pattern, tyLam.instantiateAsSuperTypeIn(environment, varSupply));
            });
        } else {
            var enclosingCase = environment.enclosingCase().get();

            if (pattern instanceof IdPatternNode<Name> idPat) {
                var updatedMeta = updateMetaWith(idPat.meta(), expectedType);
                enclosingCase.putValue(idPat.name(), updatedMeta);

                return idPatternNode(updatedMeta, idPat.name());

            } else if (pattern instanceof AliasPatternNode<Name> aliasPat) {
                var inferredPattern = checkPattern(aliasPat.pattern(), expectedType);
                var updatedMeta = updateMetaWith(aliasPat.meta(), expectedType);
                enclosingCase.putValue(aliasPat.alias(), updatedMeta);

                return aliasPatternNode(updatedMeta, aliasPat.alias(), inferredPattern);

            } else if (pattern instanceof LiteralPatternNode<Name> litPat) {
                var inferredLiteral = checkLiteral(litPat.literal(), expectedType);
                var updatedMeta = updateMetaWith(litPat.meta(), expectedType);

                return literalPatternNode(updatedMeta, inferredLiteral);

            } else if (pattern instanceof ConstructorPatternNode<Name> constrPat) {
                var constrMeta = environment.lookupValue(constrPat.id().canonicalName()).get();
                var constrName = (ConstructorName) constrMeta.meta().name();
                var constrType = (Type) constrMeta.meta().sort();

                // We need to keep this instantiation to use with our field patterns
                var instantiator = (constrType instanceof TypeLambda tyLam)
                        ? Optional.of(tyLam.subTypeInstantiationIn(environment, varSupply))
                        : Optional.<TypeInstantiationTransformer>empty();

                if (constrType instanceof TypeLambda tyLam) {
                    constrType = tyLam.body().accept(instantiator.get());
                }

                if (Type.isFunction(constrType) && constrType instanceof TypeApply tyApp) {
                    constrType = tyApp.typeArguments().getLast();
                }

                // We do this here to ensure that we solve our newly instantiated type
                // using the expected type before typechecking fields
                if (!checkSubType(constrType, expectedType)) {
                    mismatchedType(constrPat.range(), constrType, expectedType);
                }

                var inferredFields = constrPat.fields()
                        .collect(field -> inferFieldPattern(constrName, field, instantiator));

                var updatedMeta = updateMetaWith(constrPat.meta(), expectedType);

                return constructorPatternNode(updatedMeta, constrPat.id(), inferredFields);
            }

        }

        return null;
    }

    LiteralNode<Attributes> checkLiteral(LiteralNode<Name> literal, Type expectedType) {
        // TODO: Add an error type to place in Meta when we have a mismatch?
        if (literal instanceof BooleanNode<Name> bool) {
            var actualType = Type.BOOLEAN;
            var updatedMeta = updateMetaWith(bool.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(bool.range(), actualType, expectedType);
            }
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var actualType = Type.CHAR;
            var updatedMeta = updateMetaWith(chr.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(chr.range(), actualType, expectedType);
            }
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var actualType = Type.STRING;
            var updatedMeta = updateMetaWith(str.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(str.range(), actualType, expectedType);
            }
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var actualType = Type.INT;
            var updatedMeta = updateMetaWith(intgr.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(intgr.range(), actualType, expectedType);
            }
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var actualType = Type.LONG;
            var updatedMeta = updateMetaWith(lng.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(lng.range(), actualType, expectedType);
            }
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var actualType = Type.FLOAT;
            var updatedMeta = updateMetaWith(flt.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(flt.range(), actualType, expectedType);
            }
            return floatNode(updatedMeta, flt.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var actualType = Type.DOUBLE;
            var updatedMeta = updateMetaWith(dbl.meta(), expectedType);
            if (!checkSubType(actualType, expectedType)) {
                mismatchedType(dbl.range(), actualType, expectedType);
            }
            return doubleNode(updatedMeta, dbl.value());
        }

        return null;
    }
}
