package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.function.Supplier;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

public class Kindchecker {
    private DiagnosticCollector diagnostics;
    private TypeEnvironment environment;
    private UnsolvedVariableSupply varSupply;
    private KindPrinter kindPrinter = new KindPrinter();

    public Kindchecker(
            DiagnosticCollector diagnostics,
            TypeEnvironment environment,
            UnsolvedVariableSupply varSupply) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        this.varSupply = varSupply;
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

    public DataNode<Attributes> kindcheck(DataNode<Name> node) {
        var inferredData = inferData(node);
        // FIXME: Defaulting should be done after kind-checking mutually-dependent
        // groups of definitions in dependency order, not after checking each
        // definition.
        var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        return inferredData.accept(new DataNodeSubstitutionTransformer(kindDefaulting));
    }

    public TypeNode<Attributes> kindcheck(TypeNode<Name> node) {
        var inferredType = checkType(node, TypeKind.INSTANCE);
        // FIXME: Defaulting should be done after kind-checking mutually-dependent
        // groups of definitions in dependency order, not after checking each
        // definition.
        var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        return inferredType.accept(new TypeNodeSubstitutionTransformer(kindDefaulting));
    }

    Kind getKind(MetaNode<Attributes> node) {
        return (Kind) node.meta().meta().sort();
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var substituted = ((Kind) sort).substitute(environment.kindSubstitution());
        var attributes = meta.meta().withSort(substituted);
        return meta.withMeta(attributes);
    }

    void mismatchedKind(Range range, Kind actualKind, Kind expectedKind) {
        var expected = expectedKind
                .substitute(environment.kindSubstitution())
                .accept(kindPrinter);

        var actual = actualKind
                .substitute(environment.kindSubstitution())
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
                .substitute(environment.kindSubstitution())
                .accept(kindPrinter);

        var actual = actualKind
                .substitute(environment.kindSubstitution())
                .accept(kindPrinter);

        var message = Doc.group(
                Doc.text("Mismatched type application!")
                        .appendLineOrSpace(Doc.text("Expected:").appendSpace(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual:").appendSpace(actual)))
                .render(80);

        diagnostics.reportError(range, message);
    }

    void instantiateAsSubKind(UnsolvedKind unsolved, Kind superKind) {
        withScope(new InstantiateKindScope<>(), () -> {
            if (superKind instanceof UnsolvedKind otherUnsolved) {
                // Complete and Easy's InstLReach rule adapted to kinds
                environment.solveKind(otherUnsolved, unsolved);
            } else if (superKind instanceof TypeKind) {
                // Complete and Easy's InstLSolve rule adapted to kinds
                environment.solveKind(unsolved, TypeKind.INSTANCE);
            } else if (superKind instanceof HigherKind higherSup) {
                // Complete and Easy's InstLArr rule adapted to kinds
                var newHkArgs = higherSup
                        .argKinds()
                        .collect(arg -> newUnsolvedKind());

                var newHkResult = newUnsolvedKind();

                var newHk = new HigherKind(
                        newHkArgs.collect(arg -> (Kind) arg),
                        newHkResult);

                environment.solveKind(unsolved, newHk);

                newHkArgs
                        .zip(higherSup.argKinds())
                        .forEach(pair -> {
                            instantiateAsSuperKind(pair.getOne(), pair.getTwo());
                        });

                instantiateAsSubKind(
                        newHkResult,
                        higherSup.resultKind().substitute(environment.kindSubstitution()));
            }
        });
    }

    void instantiateAsSuperKind(UnsolvedKind unsolved, Kind subKind) {
        withScope(new InstantiateKindScope<>(), () -> {
            if (subKind instanceof UnsolvedKind otherUnsolved) {
                // Complete and Easy's InstRReach rule adapted to kinds
                environment.solveKind(otherUnsolved, unsolved);
            } else if (subKind instanceof TypeKind) {
                // Complete and Easy's InstRSolve rule adapted to kinds
                environment.solveKind(unsolved, TypeKind.INSTANCE);
            } else if (subKind instanceof HigherKind higherSub) {
                // Complete and Easy's InstRArr rule adapted to kinds
                var newHkArgs = higherSub
                        .argKinds()
                        .collect(arg -> newUnsolvedKind());

                var newHkResult = newUnsolvedKind();

                var newHk = new HigherKind(
                        newHkArgs.collect(arg -> (Kind) arg),
                        newHkResult);

                environment.solveKind(unsolved, newHk);

                newHkArgs
                        .zip(higherSub.argKinds())
                        .forEach(pair -> {
                            instantiateAsSubKind(pair.getOne(), pair.getTwo());
                        });

                instantiateAsSuperKind(
                        newHkResult,
                        higherSub.resultKind().substitute(environment.kindSubstitution()));
            }
        });
    }

    boolean checkSubKind(Kind subKind, Kind superKind) {
        return withScope(new CheckSubkindScope<>(), () -> {
            var solvedSubKind = subKind.substitute(environment.kindSubstitution());
            var solvedSuperKind = superKind.substitute(environment.kindSubstitution());

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
                var argsSubKinded = higherSub.argKinds()
                        .zip(higherSup.argKinds())
                        .allSatisfy(pair -> {
                            return checkSubKind(pair.getTwo(), pair.getOne());
                        });

                var resultSubKinded = checkSubKind(higherSub.resultKind(), higherSup.resultKind());

                return argsSubKinded && resultSubKinded;
            } else {
                return false;
            }
        });
    }

    DataNode<Attributes> inferData(DataNode<Name> data) {
        var dataName = (DataName) data.meta().meta();

        return withScope(new DataScope<>(dataName), () -> {
            var inferredParams = data.typeParams()
                    .collect(tyParam -> (TypeVarNode<Attributes>) inferType(tyParam));

            var inferredKind = data.typeParams().isEmpty() ? TypeKind.INSTANCE
                    : new HigherKind(
                            inferredParams.collect(param -> (Kind) param.meta().meta().sort()),
                            TypeKind.INSTANCE);

            var updatedMeta = updateMetaWith(data.meta(), inferredKind);

            environment.putType(dataName.localName(), updatedMeta);
            environment.putType(dataName.canonicalName(), updatedMeta);

            var inferredConstrs = data.constructors()
                    .collect(constr -> inferConstructor(constr, inferredKind));

            return dataNode(updatedMeta, data.name(), inferredParams, inferredConstrs);
        });
    }

    ConstructorNode<Attributes> inferConstructor(ConstructorNode<Name> constr, Kind dataKind) {
        var constrName = (ConstructorName) constr.meta().meta();

        return withScope(new ConstructorScope<>(constrName), () -> {
            var inferredParams = constr.params()
                    .collect(param -> inferConstructorParam(param));
            var checkedReturn = constr.type()
                    .map(returnType -> checkType(returnType, TypeKind.INSTANCE));

            var updatedMeta = updateMetaWith(constr.meta(), dataKind);

            return constructorNode(updatedMeta, constr.name(), inferredParams, checkedReturn);
        });
    }

    ConstructorParamNode<Attributes> inferConstructorParam(ConstructorParamNode<Name> constrParam) {
        var checkedAnnotation = checkType(constrParam.typeAnnotation(), TypeKind.INSTANCE);
        var updatedMeta = updateMetaWith(constrParam.meta(), TypeKind.INSTANCE);

        return constructorParamNode(updatedMeta, constrParam.name(), checkedAnnotation);
    }

    TypeNode<Attributes> inferType(TypeNode<Name> typ) {
        if (typ instanceof TypeLambdaNode<Name> tyLam) {
            return withScope(new TypeLambdaScope<>(), () -> {
                var inferredArgs = tyLam.args()
                        .collect(tyArg -> (TypeVarNode<Attributes>) inferType(tyArg));

                var checkedReturn = checkType(tyLam.body(), TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(tyLam.meta(), TypeKind.INSTANCE);

                return typeLambdaNode(updatedMeta, inferredArgs, checkedReturn);
            });
        } else if (typ instanceof TypeApplyNode<Name> tyApp) {
            var inferredType = inferType(tyApp.type());

            var inferredKind = getKind(inferredType);

            // Types should be fully applied
            if (inferredKind instanceof HigherKind hk &&
                    hk.argKinds().size() == tyApp.args().size()) {

                var checkedArgs = tyApp.args()
                        .zip(hk.argKinds())
                        .collect(pair -> checkType(pair.getOne(), pair.getTwo()));

                var updatedMeta = updateMetaWith(tyApp.meta(), TypeKind.INSTANCE);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);

            } else {
                var unsolvedArgs = tyApp.args()
                        .collect(arg -> newUnsolvedKind());

                var unsolvedReturn = newUnsolvedKind();

                var appliedKind = new HigherKind(
                        unsolvedArgs.collect(arg -> (Kind) arg),
                        unsolvedReturn);

                if (inferredKind instanceof UnsolvedKind unsolved) {
                    instantiateAsSubKind(unsolved, appliedKind);
                } else if (inferredKind instanceof HigherKind hk) {
                    instantiateAsSubKind(unsolvedReturn, hk.resultKind());
                }

                var checkedArgs = tyApp.args()
                        .zip(unsolvedArgs)
                        .collect(pair -> checkType(pair.getOne(), pair.getTwo()));

                if (!(inferredKind instanceof UnsolvedKind)) {
                    mismatchedTypeApplication(tyApp.range(), appliedKind, inferredKind);
                }

                var updatedMeta = updateMetaWith(tyApp.meta(), unsolvedReturn);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);
            }
        } else if (typ instanceof FunTypeNode<Name> funTy) {
            var inferredArgs = funTy.argTypes()
                    .collect(argTy -> checkType(argTy, TypeKind.INSTANCE));

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
        if (typ instanceof TypeLambdaNode<Name> tyLam &&
                expectedKind instanceof HigherKind hk &&
                tyLam.args().size() == hk.argKinds().size()) {
            return withScope(new TypeLambdaScope<>(), () -> {
                var knownArgs = tyLam.args()
                        .zip(hk.argKinds())
                        .collect(pair -> {
                            var tyArg = pair.getOne();
                            var updatedMeta = updateMetaWith(tyArg.meta(), pair.getTwo());
                            environment.putType(tyArg.name(), updatedMeta);
                            if (tyArg instanceof ForAllVarNode) {
                                return (TypeVarNode<Attributes>) forAllVarNode(updatedMeta, tyArg.name());
                            } else {
                                return (TypeVarNode<Attributes>) existsVarNode(updatedMeta, tyArg.name());
                            }
                        });

                var inferredReturn = checkType(tyLam.body(), TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(tyLam.meta(), TypeKind.INSTANCE);

                return typeLambdaNode(updatedMeta, knownArgs, inferredReturn);
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
