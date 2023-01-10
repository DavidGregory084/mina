package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.Scope;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;
import org.mina_lang.typechecker.scopes.*;

import com.opencastsoftware.prettier4j.Doc;

public class Typechecker {

    private DiagnosticCollector diagnostics;
    private TypeEnvironment environment;
    private UnsolvedVariableSupply varSupply;
    private Kindchecker kindchecker;
    private TypeAnnotationFolder typeFolder;
    private SortSubstitutionTransformer sortTransformer;
    private SortPrinter sortPrinter = new SortPrinter(new KindPrinter(), new TypePrinter());

    public Typechecker(DiagnosticCollector diagnostics, TypeEnvironment environment) {
        this.diagnostics = diagnostics;
        this.environment = environment;

        this.varSupply = new UnsolvedVariableSupply();

        this.typeFolder = new TypeAnnotationFolder(environment);

        this.sortTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(),
                environment.kindSubstitution());

        this.kindchecker = new Kindchecker(diagnostics, environment, varSupply, sortTransformer);
    }

    public TypeEnvironment getEnvironment() {
        return environment;
    }

    <A> A withScope(TypingScope scope, Supplier<A> fn) {
        environment.pushScope(scope);
        var result = fn.get();
        environment.popScope(scope.getClass());
        return result;
    }

    void withScope(TypingScope scope, Runnable fn) {
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

    public ImmutableList<DeclarationNode<Attributes>> typecheck(ImmutableList<DeclarationNode<Name>> node) {
        var metaTransformer = new MetaNodeSubstitutionTransformer(sortTransformer);
        return inferDeclarationGroup(node).collect(decl -> decl.accept(metaTransformer));
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

    void noUniqueType(Range range, DeclarationName name, Type inferredType,
            ImmutableSortedSet<UnsolvedType> unsolvedVars) {
        var inferred = inferredType
                .substitute(environment.typeSubstitution(), environment.kindSubstitution())
                .accept(sortPrinter);

        var unsolvedVarDocs = unsolvedVars
                .collect(UnsolvedType::name)
                .collect(Doc::text);

        var message = Doc.group(
                Doc.text("Couldn't infer a unique type for " + name.localName() + "!")
                        .appendLineOrSpace(Doc.text("Found:").appendSpace(inferred))
                        .appendLineOr(", ", Doc.text("where").appendSpace(
                                Doc.intersperse(Doc.text(", "), unsolvedVarDocs.stream()))
                                .appendSpace(
                                        unsolvedVars.size() > 1 ? Doc.text("are unsolved variables.")
                                                : Doc.text("is an unsolved variable."))))
                .render(80);

        diagnostics.reportError(range, message);
    }

    TypeInstantiationTransformer subTypeInstantiation(TypeLambda tyLam) {
        var instantiated = Maps.mutable.<TypeVar, MonoType>empty();

        tyLam.args().forEach(tyParam -> {
            if (tyParam instanceof ForAllVar forall) {
                var unsolved = varSupply.newUnsolvedType(forall.kind());
                environment.putUnsolvedType(unsolved);
                instantiated.put(forall, unsolved);
            } else if (tyParam instanceof ExistsVar exists) {
                var typeVarName = new ExistsVarName(exists.name());
                var typeVarAttrs = new Attributes(typeVarName, exists.kind());
                environment.putType(exists.name(), Meta.of(typeVarAttrs));
            }
        });

        return new TypeInstantiationTransformer(instantiated.toImmutable());
    }

    Type instantiateAsSubType(TypeLambda tyLam) {
        return tyLam.body().accept(subTypeInstantiation(tyLam));
    }

    TypeInstantiationTransformer superTypeInstantiation(TypeLambda tyLam) {
        var instantiated = Maps.mutable.<TypeVar, MonoType>empty();

        tyLam.args().forEach(tyParam -> {
            if (tyParam instanceof ForAllVar forall) {
                var typeVarName = new ForAllVarName(forall.name());
                var typeVarAttrs = new Attributes(typeVarName, forall.kind());
                environment.putType(forall.name(), Meta.of(typeVarAttrs));
            } else if (tyParam instanceof ExistsVar exists) {
                var unsolved = varSupply.newUnsolvedType(exists.kind());
                environment.putUnsolvedType(unsolved);
                instantiated.put(exists, unsolved);
            }
        });

        return new TypeInstantiationTransformer(instantiated.toImmutable());
    }

    Type instantiateAsSuperType(TypeLambda tyLam) {
        return tyLam.body().accept(superTypeInstantiation(tyLam));
    }

    void instantiateAsSubType(UnsolvedType unsolved, Type superType) {
        withScope(new InstantiateTypeScope(), () -> {
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
                instantiateAsSubType(unsolved, tyLam.body().accept(superTypeInstantiation(tyLam)));
            }
        });
    }

    void instantiateAsSuperType(UnsolvedType unsolved, Type subType) {
        withScope(new InstantiateTypeScope(), () -> {
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
                instantiateAsSuperType(unsolved, tyLam.body().accept(subTypeInstantiation(tyLam)));
            }
        });
    }

    boolean checkSubType(Type subType, Type superType) {
        return withScope(new CheckSubtypeScope(), () -> {
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
                return checkSubType(solvedSubType, instantiateAsSuperType(tyLam));

            } else if (solvedSubType instanceof TypeLambda tyLam) {
                // Complete and Easy's <:ForallL rule
                return checkSubType(instantiateAsSubType(tyLam), solvedSuperType);

            } else {
                return false;
            }
        });
    }

    NamespaceTypingScope populateTopLevel(NamespaceNode<Name> namespace) {
        var currentNamespace = (NamespaceName) namespace.meta().meta();
        var namespaceScope = new NamespaceTypingScope(currentNamespace);

        namespace.declarationGroups().forEach(decls -> {
            decls.forEach(decl -> {
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
        });

        return namespaceScope;
    }

    void updateTopLevel(DeclarationNode<Attributes> decl) {
        if (decl instanceof LetNode<Attributes> let) {
            putValueDeclaration(let.meta());
        } else if (decl instanceof LetFnNode<Attributes> letFn) {
            putValueDeclaration(letFn.meta());
        } else if (decl instanceof DataNode<Attributes> data) {
            putTypeDeclaration(data.meta());

            data.constructors().forEach(constr -> {
                putValueDeclaration(constr.meta());

                constr.params().forEach(constrParam -> {
                    environment.putField(
                            (ConstructorName) constr.meta().meta().name(),
                            constrParam.name(),
                            constrParam.meta());
                });
            });
        }
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
            BiFunction<ImmutableList<TypeVarNode<Attributes>>, ImmutableList<TypeVar>, A> fn) {
        var tyParams = getTyParams.get();

        if (tyParams.isEmpty()) {
            return fn.apply(Lists.immutable.empty(), Lists.immutable.empty());
        } else {
            return withScope(new TypeLambdaTypingScope(), () -> {
                var kindedTyParams = tyParams
                        .collect(tyParam -> (TypeVarNode<Attributes>) kindchecker.inferType(tyParam));
                var tyParamTypes = kindedTyParams
                        .collect(tyParam -> (TypeVar) typeFolder.visitTypeVar(tyParam));

                return fn.apply(kindedTyParams, tyParamTypes);
            });
        }
    }

    <A> A withPolyInstantiation(Type inferredType, Function<Type, A> fn) {
        if (inferredType instanceof TypeLambda tyLam) {
            return withScope(new InstantiateTypeScope(), () -> {
                // Keep instantiating binders until we have something else
                return withPolyInstantiation(instantiateAsSubType(tyLam), fn);
            });
        } else {
            return fn.apply(inferredType);
        }
    }

    NamespaceNode<Attributes> inferNamespace(NamespaceNode<Name> namespace) {
        return withScope(populateTopLevel(namespace), () -> {
            var updatedMeta = updateMetaWith(namespace.meta(), Type.NAMESPACE);
            var inferredDecls = namespace.declarationGroups().collect(this::inferDeclarationGroup);
            return new NamespaceNode<>(updatedMeta, namespace.id(), namespace.imports(), inferredDecls);
        });
    }

    ImmutableList<DeclarationNode<Attributes>> inferDeclarationGroup(
            ImmutableList<DeclarationNode<Name>> declarationGroup) {
        var inferredGroup = declarationGroup
                .collect(this::inferDeclaration)
                .collect(this::defaultKinds)
                .collect(this::checkPrincipalTypes);

        inferredGroup.forEach(this::updateTopLevel);

        return inferredGroup;
    }

    DeclarationNode<Attributes> defaultKinds(DeclarationNode<Attributes> declaration) {
        if (declaration instanceof DataNode<Attributes> data) {
            var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
            var sortTransformer = new SortSubstitutionTransformer(environment.typeSubstitution(), kindDefaulting);
            return data.accept(new MetaNodeSubstitutionTransformer(sortTransformer));
        } else {
            return declaration;
        }
    }

    DeclarationNode<Attributes> checkPrincipalTypes(DeclarationNode<Attributes> declaration) {
        if (declaration instanceof DataNode) {
            return declaration;
        } else {
            var inferredType = getType(declaration);
            var unsolvedVars = inferredType.accept(new FreeUnsolvedVariablesFolder(environment));

            if (!unsolvedVars.isEmpty()) {
                noUniqueType(
                    declaration.range(),
                    (DeclarationName) declaration.meta().meta().name(),
                    inferredType,
                    unsolvedVars);
            }

            return declaration;
        }
    }

    DeclarationNode<Attributes> inferDeclaration(DeclarationNode<Name> declaration) {
        if (declaration instanceof DataNode<Name> data) {
            var kindedData = kindchecker.kindcheck(data);
            var dataName = (DataName) kindedData.meta().meta().name();

            putTypeDeclaration(kindedData.meta());

            kindedData.constructors().forEach(kindedConstr -> {
                var constrName = (ConstructorName) kindedConstr.meta().meta().name();

                putValueDeclaration(kindedConstr.meta());

                kindedConstr.params().forEach(constrParam -> {
                    environment.putField(constrName, constrParam.name(), constrParam.meta());
                });

                kindedConstr.type().ifPresent(constrTypeNode -> {
                    withScope(new ConstructorTypingScope(constrName), () -> {
                        var typeParamTypes = kindedData.typeParams()
                                .collect(tyParam -> tyParam.accept(typeFolder));
                        var constrType = constrTypeNode.accept(typeFolder);

                        var dataTyCon = new TypeConstructor(dataName.name(), getKind(kindedData));

                        var dataType = typeParamTypes.isEmpty() ? dataTyCon
                                : instantiateAsSubType(new TypeLambda(
                                        typeParamTypes.collect(tyParam -> (TypeVar) tyParam),
                                        new TypeApply(dataTyCon, typeParamTypes, TypeKind.INSTANCE),
                                        TypeKind.INSTANCE));

                        // Constructor return type should be a valid instantiation of the data type
                        if (!checkSubType(dataType, constrType)) {
                            mismatchedType(constrTypeNode.range(), constrType, dataType);
                        }
                    });
                });
            });

            return kindedData;

        } else if (declaration instanceof LetFnNode<Name> letFn) {
            var letFnNode = withTypeParams(letFn::typeParams, (tyParams, tyParamTypes) -> {
                return withScope(new LambdaTypingScope(), () -> {
                    var inferredParams = letFn.valueParams()
                            .collect(this::inferParam);
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

    ParamNode<Attributes> inferParam(ParamNode<Name> param) {
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
            return withScope(new BlockTypingScope(), () -> {
                var inferredDeclarations = block
                        .declarations()
                        .collect(let -> (LetNode<Attributes>) inferDeclaration(let));

                var inferredResult = block.result().map(this::inferExpr);
                var inferredType = inferredResult.map(this::getType).orElse(Type.UNIT);

                var updatedMeta = updateMetaWith(block.meta(), inferredType);

                return blockNode(updatedMeta, inferredDeclarations, inferredResult);
            });
        } else if (expr instanceof LambdaNode<Name> lambda) {
            return withScope(new LambdaTypingScope(), () -> {
                var inferredArgs = lambda.params().collect(this::inferParam);

                var inferredBody = inferExpr(lambda.body());

                var inferredType = Type.function(
                        inferredArgs.collect(this::getType),
                        getType(inferredBody));

                var updatedMeta = updateMetaWith(lambda.meta(), inferredType);

                return lambdaNode(updatedMeta, inferredArgs, inferredBody);
            });
        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);

            var consequent = inferExpr(ifExpr.consequent());
            var consequentType = getType(consequent);

            var alternative = checkExpr(ifExpr.alternative(), consequentType);

            var updatedMeta = updateMetaWith(ifExpr.meta(), consequentType);

            return ifNode(updatedMeta, condition, consequent, alternative);

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
        return withScope(new CaseTypingScope(), () -> {
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
                    ? Optional.of(subTypeInstantiation(tyLam))
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
            return withScope(new InstantiateTypeScope(), () -> {
                return checkExpr(expr, instantiateAsSuperType(tyLam));
            });
        } else if (expr instanceof BlockNode<Name> block) {
            return withScope(new BlockTypingScope(), () -> {
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
        } else if (expr instanceof LambdaNode<Name> lambda &&
                expectedType instanceof TypeApply funType &&
                lambda.params().size() == (funType.typeArguments().size() - 1)) {
            return withScope(new LambdaTypingScope(), () -> {
                var knownParams = lambda.params()
                        .zip(funType.typeArguments().take(funType.typeArguments().size() - 1))
                        .collect(pair -> checkParam(pair.getOne(), pair.getTwo()));

                var checkedReturn = checkExpr(lambda.body(), funType.typeArguments().getLast());

                var appliedType = Type.function(
                        knownParams.collect(this::getType),
                        getType(checkedReturn));

                // We could make this error more local by checking the expected types of the
                // params against their annotations above, but I think this gives more context
                // to the error
                if (!checkSubType(appliedType, expectedType)) {
                    mismatchedType(lambda.range(), appliedType, expectedType);
                }

                var updatedMeta = updateMetaWith(lambda.meta(), expectedType);

                return lambdaNode(updatedMeta, knownParams, checkedReturn);
            });
        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);
            var consequent = checkExpr(ifExpr.consequent(), expectedType);
            var alternative = checkExpr(ifExpr.alternative(), expectedType);

            var updatedMeta = updateMetaWith(ifExpr.meta(), expectedType);

            return ifNode(updatedMeta, condition, consequent, alternative);

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

    ParamNode<Attributes> checkParam(ParamNode<Name> param, Type expectedType) {
        var kindedAnnotation = param.typeAnnotation()
                .map(kindchecker::kindcheck);

        var annotatedType = kindedAnnotation
                .map(annot -> annot.accept(typeFolder));

        var updatedMeta = annotatedType
                .map(annotType -> updateMetaWith(param.meta(), annotType))
                .orElseGet(() -> updateMetaWith(param.meta(), expectedType));

        environment.putValue(param.name(), updatedMeta);

        return paramNode(updatedMeta, param.name(), kindedAnnotation);
    }

    CaseNode<Attributes> checkCase(CaseNode<Name> cse, Type scrutineeType, Type expectedType) {
        return withScope(new CaseTypingScope(), () -> {
            var inferredPattern = checkPattern(cse.pattern(), scrutineeType);
            var inferredConsequent = checkExpr(cse.consequent(), expectedType);
            var inferredType = getType(inferredConsequent);
            var updatedMeta = updateMetaWith(cse.meta(), inferredType);
            return caseNode(updatedMeta, inferredPattern, inferredConsequent);
        });
    }

    PatternNode<Attributes> checkPattern(PatternNode<Name> pattern, Type expectedType) {
        if (expectedType instanceof TypeLambda tyLam) {
            return withScope(new InstantiateTypeScope(), () -> {
                return checkPattern(pattern, instantiateAsSuperType(tyLam));
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
                        ? Optional.of(subTypeInstantiation(tyLam))
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
