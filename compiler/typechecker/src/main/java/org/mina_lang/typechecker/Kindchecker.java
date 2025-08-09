/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import com.opencastsoftware.prettier4j.Doc;
import com.opencastsoftware.yvette.Range;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.LocalDiagnosticReporter;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;
import org.mina_lang.typechecker.scopes.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class Kindchecker {
    private LocalDiagnosticReporter diagnostics;
    private TypeEnvironment environment;
    private UnsolvedVariableSupply varSupply;
    private TypeAnnotationFolder typeFolder;
    private SortSubstitutionTransformer sortTransformer;
    private KindPrinter kindPrinter = new KindPrinter();

    public Kindchecker(
            LocalDiagnosticReporter diagnostics,
            TypeEnvironment environment,
            UnsolvedVariableSupply varSupply,
            SortSubstitutionTransformer sortTransformer) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        this.varSupply = varSupply;
        this.sortTransformer = sortTransformer;
        this.typeFolder = new TypeAnnotationFolder(environment);
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

    public DataNode<Attributes> kindcheck(DataNode<Name> node) {
        return inferData(node);
    }

    public TypeNode<Attributes> kindcheck(TypeNode<Name> node) {
        var inferredType = checkType(node, TypeKind.INSTANCE);
        var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        return inferredType.accept(new TypeNodeSubstitutionTransformer(kindDefaulting));
    }

    Kind getKind(MetaNode<Attributes> node) {
        return (Kind) node.meta().meta().sort();
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var substituted = sort.accept(sortTransformer);
        var attributes = meta.meta().withSort(substituted);
        return meta.withMeta(attributes);
    }

    Type createConstructorType(
            DataName dataName,
            Kind dataKind,
            List<Type> typeParamTypes,
            List<ConstructorParamNode<Attributes>> constrParamNodes,
            Optional<TypeNode<Attributes>> constrReturnTypeNode) {

        var constrParamTypes = constrParamNodes.stream()
            .map(param -> typeFolder.visitType(param.typeAnnotation()))
            .toList();

        var constrReturnType = constrReturnTypeNode
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
            return new QuantifiedType(
                typeParamTypes.stream().map(tyParam -> (TypeVar) tyParam).toList(),
                constrFnType,
                dataKind);
        }
    }

    void mismatchedKind(Range range, Kind actualKind, Kind expectedKind) {
        var expected = expectedKind
                .accept(sortTransformer.getKindTransformer())
                .accept(kindPrinter);

        var actual = actualKind
                .accept(sortTransformer.getKindTransformer())
                .accept(kindPrinter);

        var message = Doc.group(
                Doc.text("Mismatched kind!")
                        .appendLineOrSpace(Doc.text("Expected:").appendSpace(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual:").appendSpace(actual)))
                .render(80);

        diagnostics.reportError(range, message);
    }

    void mismatchedTypeApplication(Range range, Kind actualKind, Kind expectedKind) {
        var expected = expectedKind
                .accept(sortTransformer.getKindTransformer())
                .accept(kindPrinter);

        var actual = actualKind
                .accept(sortTransformer.getKindTransformer())
                .accept(kindPrinter);

        var message = Doc.group(
                Doc.text("Mismatched type application!")
                        .appendLineOrSpace(Doc.text("Expected:").appendSpace(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual:").appendSpace(actual)))
                .render(80);

        diagnostics.reportError(range, message);
    }

    void instantiateAsSubKind(UnsolvedKind unsolved, Kind superKind) {
        if (superKind instanceof UnsolvedKind otherUnsolved) {
            // Complete and Easy's InstLReach rule adapted to kinds
            environment.solveKind(otherUnsolved, unsolved);
        } else if (superKind instanceof TypeKind) {
            // Complete and Easy's InstLSolve rule adapted to kinds
            environment.solveKind(unsolved, TypeKind.INSTANCE);
        } else if (superKind instanceof HigherKind higherSup) {
            // Complete and Easy's InstLArr rule adapted to kinds
            var newHkArgs = higherSup.argKinds().stream()
                .map(arg -> newUnsolvedKind())
                .toList();

            var newHkResult = newUnsolvedKind();

            var newHk = new HigherKind(
                    newHkArgs.stream().map(arg -> (Kind) arg).toList(),
                    newHkResult);

            environment.solveKind(unsolved, newHk);

            IntStream.range(0, Math.min(newHkArgs.size(), higherSup.argKinds().size())).forEach(index -> {
                instantiateAsSuperKind(newHkArgs.get(index), higherSup.argKinds().get(index));
            });

            instantiateAsSubKind(
                    newHkResult,
                    higherSup.resultKind().accept(sortTransformer.getKindTransformer()));
        }
    }

    void instantiateAsSuperKind(UnsolvedKind unsolved, Kind subKind) {
        if (subKind instanceof UnsolvedKind otherUnsolved) {
            // Complete and Easy's InstRReach rule adapted to kinds
            environment.solveKind(otherUnsolved, unsolved);
        } else if (subKind instanceof TypeKind) {
            // Complete and Easy's InstRSolve rule adapted to kinds
            environment.solveKind(unsolved, TypeKind.INSTANCE);
        } else if (subKind instanceof HigherKind higherSub) {
            // Complete and Easy's InstRArr rule adapted to kinds
            var newHkArgs = higherSub.argKinds().stream()
                .map(arg -> newUnsolvedKind())
                .toList();

            var newHkResult = newUnsolvedKind();

            var newHk = new HigherKind(
                newHkArgs.stream().map(arg -> (Kind) arg).toList(),
                newHkResult);

            environment.solveKind(unsolved, newHk);

            IntStream.range(0, Math.min(newHkArgs.size(), higherSub.argKinds().size())).forEach(index -> {
                instantiateAsSubKind(newHkArgs.get(index), higherSub.argKinds().get(index));
            });

            instantiateAsSuperKind(
                    newHkResult,
                    higherSub.resultKind().accept(sortTransformer.getKindTransformer()));
        }
    }

    boolean checkSubKind(Kind subKind, Kind superKind) {
            var solvedSubKind = subKind.accept(sortTransformer.getKindTransformer());
            var solvedSuperKind = superKind.accept(sortTransformer.getKindTransformer());

            if (solvedSubKind == TypeKind.INSTANCE && solvedSuperKind == TypeKind.INSTANCE) {
                // Complete and Easy's <:Var rule adapted to kinds
                return true;
            } else if (solvedSubKind instanceof UnsolvedKind unsolvedSub &&
                    solvedSuperKind instanceof UnsolvedKind unsolvedSuper &&
                    unsolvedSub.id() == unsolvedSuper.id()) {
                // Complete and Easy's <:Exvar rule adapted to kinds
                return true;
            } else if (solvedSubKind instanceof UnsolvedKind unsolvedSub
                    && !unsolvedSub.isFreeIn(solvedSuperKind)) {
                // Complete and Easy's <:InstantiateL rule adapted to kinds
                instantiateAsSubKind(unsolvedSub, solvedSuperKind);

                return true;

            } else if (solvedSuperKind instanceof UnsolvedKind unsolvedSup
                    && !unsolvedSup.isFreeIn(solvedSubKind)) {
                // Complete and Easy's <:InstantiateR rule adapted to kinds
                instantiateAsSuperKind(unsolvedSup, solvedSubKind);

                return true;

            } else if (solvedSubKind instanceof HigherKind higherSub &&
                    solvedSuperKind instanceof HigherKind higherSup &&
                    higherSub.argKinds().size() == higherSup.argKinds().size()) {
                // Complete and Easy's <:-> rule adapted to kinds
                var argsSubKinded = IntStream.range(0, higherSub.argKinds().size()).allMatch(index -> {
                    return checkSubKind(higherSup.argKinds().get(index), higherSub.argKinds().get(index));
                });

                var resultSubKinded = checkSubKind(higherSub.resultKind(), higherSup.resultKind());

                return argsSubKinded && resultSubKinded;
            } else {
                return false;
            }
    }

    DataNode<Attributes> inferData(DataNode<Name> data) {
        var dataName = (DataName) data.meta().meta();

        return withScope(new DataTypingScope(dataName), () -> {
            var inferredParams = data.typeParams().stream()
                .map(tyParam -> (TypeVarNode<Attributes>) inferType(tyParam))
                .toList();

            var inferredKind = data.typeParams().isEmpty() ? TypeKind.INSTANCE
                    : new HigherKind(
                            inferredParams.stream().map(param -> (Kind) param.meta().meta().sort()).toList(),
                            TypeKind.INSTANCE);

            var updatedMeta = updateMetaWith(data.meta(), inferredKind);

            environment.putType(dataName.localName(), updatedMeta);

            environment.putType(dataName.canonicalName(), updatedMeta);

            var dataTypeParams = inferredParams.stream().map(tyParam -> tyParam.accept(typeFolder)).toList();

            var inferredConstrs = data.constructors().stream()
                .map(constr -> inferConstructor(dataName, inferredKind, dataTypeParams, constr))
                .toList();

            return dataNode(updatedMeta, data.name(), inferredParams, inferredConstrs);
        });
    }

    ConstructorNode<Attributes> inferConstructor(
            DataName dataName,
            Kind dataKind,
            List<Type> dataTypeParams,
            ConstructorNode<Name> constr) {
        var constrName = (ConstructorName) constr.meta().meta();

        return withScope(new ConstructorTypingScope(constrName), () -> {
            var inferredParams = constr.params().stream()
                .map(param -> inferConstructorParam(param))
                .toList();
            var checkedReturn = constr.type()
                .map(returnType -> checkType(returnType, TypeKind.INSTANCE));

            var constructorType = createConstructorType(
                    dataName,
                    dataKind,
                    dataTypeParams,
                    inferredParams,
                    checkedReturn);

            var updatedMeta = updateMetaWith(constr.meta(), constructorType);

            return constructorNode(updatedMeta, constr.name(), inferredParams, checkedReturn);
        });
    }

    ConstructorParamNode<Attributes> inferConstructorParam(ConstructorParamNode<Name> constrParam) {
        var checkedAnnotation = checkType(constrParam.typeAnnotation(), TypeKind.INSTANCE);
        var paramType = checkedAnnotation.accept(typeFolder);

        var updatedMeta = updateMetaWith(constrParam.meta(), paramType);

        return constructorParamNode(updatedMeta, constrParam.name(), checkedAnnotation);
    }

    TypeNode<Attributes> inferType(TypeNode<Name> typ) {
        if (typ instanceof QuantifiedTypeNode<Name> quant) {
            return withScope(new QuantifiedTypingScope(), () -> {
                var inferredArgs = quant.args().stream()
                    .map(tyArg -> (TypeVarNode<Attributes>) inferType(tyArg))
                    .toList();

                var checkedReturn = checkType(quant.body(), TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(quant.meta(), TypeKind.INSTANCE);

                return quantifiedTypeNode(updatedMeta, inferredArgs, checkedReturn);
            });
        } else if (typ instanceof TypeApplyNode<Name> tyApp) {
            var inferredType = inferType(tyApp.type());

            var inferredKind = getKind(inferredType);

            // Types should be fully applied
            if (inferredKind instanceof HigherKind hk &&
                    hk.argKinds().size() == tyApp.args().size()) {

                var checkedArgs = IntStream.range(0, Math.min(tyApp.args().size(), hk.argKinds().size()))
                    .mapToObj(index -> checkType(tyApp.args().get(index), hk.argKinds().get(index)))
                    .toList();

                var updatedMeta = updateMetaWith(tyApp.meta(), TypeKind.INSTANCE);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);

            } else {
                var unsolvedArgs = tyApp.args().stream()
                    .map(arg -> newUnsolvedKind())
                    .toList();

                var unsolvedReturn = newUnsolvedKind();

                var appliedKind = new HigherKind(
                    unsolvedArgs.stream().map(arg -> (Kind) arg).toList(),
                    unsolvedReturn);

                if (inferredKind instanceof UnsolvedKind unsolved) {
                    instantiateAsSubKind(unsolved, appliedKind);
                } else if (inferredKind instanceof HigherKind hk) {
                    instantiateAsSubKind(unsolvedReturn, hk.resultKind());
                }

                var checkedArgs = IntStream.range(0, Math.min(tyApp.args().size(), unsolvedArgs.size()))
                    .mapToObj(index -> checkType(tyApp.args().get(index), unsolvedArgs.get(index)))
                    .toList();

                if (!(inferredKind instanceof UnsolvedKind)) {
                    mismatchedTypeApplication(tyApp.range(), appliedKind, inferredKind);
                }

                var updatedMeta = updateMetaWith(tyApp.meta(), unsolvedReturn);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);
            }
        } else if (typ instanceof FunTypeNode<Name> funTy) {
            var inferredArgs = funTy.argTypes().stream()
                .map(argTy -> checkType(argTy, TypeKind.INSTANCE))
                .toList();

            var inferredReturn = checkType(funTy.returnType(), TypeKind.INSTANCE);

            // Function types are essentially poly-kinded so any number of args
            // produces a proper type
            var updatedMeta = updateMetaWith(funTy.meta(), TypeKind.INSTANCE);

            return funTypeNode(updatedMeta, inferredArgs, inferredReturn);

        } else if (typ instanceof ForAllVarNode<Name> forall) {
            var unsolvedKind = newUnsolvedKind();
            var updatedMeta = updateMetaWith(forall.meta(), unsolvedKind);

            environment.putTypeIfAbsent(forall.name(), updatedMeta);

            return forAllVarNode(updatedMeta, forall.name());

        } else if (typ instanceof ExistsVarNode<Name> exists) {
            var unsolvedKind = newUnsolvedKind();
            var updatedMeta = updateMetaWith(exists.meta(), unsolvedKind);

            environment.putTypeIfAbsent(exists.name(), updatedMeta);

            return existsVarNode(updatedMeta, exists.name());

        } else if (typ instanceof TypeReferenceNode<Name> tyRef) {
            var envType = environment.lookupType(tyRef.id().canonicalName()).get();
            var updatedMeta = updateMetaWith(tyRef.meta(), envType.meta().sort());

            return typeRefNode(updatedMeta, tyRef.id());
        }

        return null;
    }

    TypeNode<Attributes> checkType(TypeNode<Name> typ, Kind expectedKind) {
        if (typ instanceof QuantifiedTypeNode<Name> quant &&
                expectedKind instanceof HigherKind hk &&
                quant.args().size() == hk.argKinds().size()) {
            return withScope(new QuantifiedTypingScope(), () -> {
                var knownArgs = IntStream.range(0, quant.args().size())
                    .mapToObj(index -> {
                        var tyArg = quant.args().get(index);
                        var updatedMeta = updateMetaWith(tyArg.meta(), hk.argKinds().get(index));
                        environment.putType(tyArg.name(), updatedMeta);
                        if (tyArg instanceof ForAllVarNode) {
                            return (TypeVarNode<Attributes>) forAllVarNode(updatedMeta, tyArg.name());
                        } else {
                            return (TypeVarNode<Attributes>) existsVarNode(updatedMeta, tyArg.name());
                        }
                    });

                var inferredReturn = checkType(quant.body(), TypeKind.INSTANCE);

                if (!checkSubKind(TypeKind.INSTANCE, expectedKind)) {
                    mismatchedKind(quant.range(), TypeKind.INSTANCE, expectedKind);
                }

                var updatedMeta = updateMetaWith(quant.meta(), TypeKind.INSTANCE);

                return quantifiedTypeNode(updatedMeta, knownArgs.toList(), inferredReturn);
            });
        } else {
            var inferredType = inferType(typ);
            var inferredKind = getKind(inferredType);

            if (!checkSubKind(inferredKind, expectedKind)) {
                // TODO: Differentiate between a subkinding error and an occurs check failure
                mismatchedKind(inferredType.range(), inferredKind, expectedKind);
            }

            return inferredType;
        }
    }

}
