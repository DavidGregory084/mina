package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.function.Supplier;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.Scope;
import org.mina_lang.common.scopes.TypeLambdaScope;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

public class Kindchecker {
    private DiagnosticCollector diagnostics;
    private Environment<Attributes> environment;
    private UnsolvedVariableSupply varSupply;
    private KindPrinter kindPrinter = new KindPrinter();

    public Kindchecker(
            DiagnosticCollector diagnostics,
            Environment<Attributes> environment,
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

    public DataNode<Attributes> kindcheck(DataNode<Name> node) {
        var inferredData = inferData(node);
        // FIXME: Defaulting should be done after kind-checking mutually-dependent
        // groups of definitions in dependency order, not after checking each
        // definition.
        // environment.defaultKinds();
        // var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        // return inferredData.accept(new TypeNodeSubstitutionTransformer(kindDefaulting));
        return inferredData;
    }

    public ConstructorNode<Attributes> kindcheck(ConstructorNode<Name> node) {
        var inferredConstr = inferConstructor(node);
        // FIXME: Defaulting should be done after kind-checking mutually-dependent
        // groups of definitions in dependency order, not after checking each
        // definition.
        // environment.defaultKinds();
        // var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        // return inferredConstr.accept(new TypeNodeSubstitutionTransformer(kindDefaulting));
        return inferredConstr;
    }

    public TypeNode<Attributes> kindcheck(TypeNode<Name> node) {
        var inferredType = inferType(node);
        // FIXME: Defaulting should be done after kind-checking mutually-dependent
        // groups of definitions in dependency order, not after checking each
        // definition.
        environment.defaultKinds();
        var kindDefaulting = new KindDefaultingTransformer(environment.kindSubstitution());
        return inferredType.accept(new TypeNodeSubstitutionTransformer(kindDefaulting));
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
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
        if (superKind instanceof UnsolvedKind otherUnsolved) {
            // Complete and Easy's InstLReach for types
            environment.solveKind(otherUnsolved, unsolved);
        } else if (superKind instanceof TypeKind) {
            // Complete and Easy's InstLSolve for types
            environment.solveKind(unsolved, TypeKind.INSTANCE);
        } else if (superKind instanceof HigherKind higherSup) {
            // Complete and Easy's InstLArr for types
            var newHkArgs = higherSup
                    .argKinds()
                    .collect(arg -> varSupply.newUnsolvedKind());

            var newHkResult = varSupply.newUnsolvedKind();

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
    }

    void instantiateAsSuperKind(UnsolvedKind unsolved, Kind subKind) {
        if (subKind instanceof UnsolvedKind otherUnsolved) {
            // Complete and Easy's InstRReach for types
            environment.solveKind(otherUnsolved, unsolved);
        } else if (subKind instanceof TypeKind) {
            // Complete and Easy's InstRSolve for types
            environment.solveKind(unsolved, TypeKind.INSTANCE);
        } else if (subKind instanceof HigherKind higherSub) {
            // Complete and Easy's InstRArr for types
            var newHkArgs = higherSub
                    .argKinds()
                    .collect(arg -> varSupply.newUnsolvedKind());

            var newHkResult = varSupply.newUnsolvedKind();

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
    }

    boolean checkSubKind(Kind subKind, Kind superKind) {
        var solvedSubKind = subKind.substitute(environment.kindSubstitution());
        var solvedSuperKind = superKind.substitute(environment.kindSubstitution());

        if (solvedSubKind == TypeKind.INSTANCE && solvedSuperKind == TypeKind.INSTANCE) {
            // Complete and Easy's <:Var rule for types
            return true;
        } else if (solvedSubKind instanceof UnsolvedKind unsolvedSub &&
                solvedSuperKind instanceof UnsolvedKind unsolvedSuper &&
                unsolvedSub.id() == unsolvedSuper.id()) {
            // Complete and Easy's <:Exvar rule for types
            return true;
        } else if (solvedSubKind instanceof UnsolvedKind unsolvedSub
                && !unsolvedSub.isFreeIn(solvedSuperKind)) {
            // Complete and Easy's <:InstantiateL for types
            instantiateAsSubKind(unsolvedSub, solvedSuperKind);

            return true;

        } else if (solvedSuperKind instanceof UnsolvedKind unsolvedSup
                && !unsolvedSup.isFreeIn(solvedSubKind)) {
            // Complete and Easy's <:InstantiateR for types
            instantiateAsSuperKind(unsolvedSup, solvedSubKind);

            return true;

        } else if (solvedSubKind instanceof HigherKind higherSub &&
                solvedSuperKind instanceof HigherKind higherSup &&
                higherSub.argKinds().size() == higherSup.argKinds().size()) {
            // Complete and Easy's <:-> for types
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
    }

    DataNode<Attributes> inferData(DataNode<Name> data) {
        return null;
    }

    ConstructorNode<Attributes> inferConstructor(ConstructorNode<Name> constr) {
        return null;
    }

    TypeNode<Attributes> inferType(TypeNode<Name> typ) {
        if (typ instanceof TypeLambdaNode<Name> tyLam) {
            return withScope(new TypeLambdaScope<>(), () -> {
                var inferredArgs = tyLam.args()
                        .collect(tyArg -> (TypeVarNode<Attributes>) inferType(tyArg));

                var checkedReturn = checkType(tyLam.body(), TypeKind.INSTANCE);

                var inferredKind = new HigherKind(
                        inferredArgs.collect(arg -> (Kind) arg.meta().meta().sort()),
                        (Kind) checkedReturn.meta().meta().sort());

                var updatedMeta = updateMetaWith(
                        tyLam.meta(),
                        inferredKind.substitute(environment.kindSubstitution()));

                return typeLambdaNode(updatedMeta, inferredArgs, checkedReturn);
            });
        } else if (typ instanceof TypeApplyNode<Name> tyApp) {
            var inferredType = inferType(tyApp.type());

            var inferredTypeKind = ((Kind) inferredType.meta().meta().sort())
                    .substitute(environment.kindSubstitution());

            // Types should be fully applied
            if (inferredTypeKind instanceof HigherKind hk &&
                    hk.argKinds().size() == tyApp.args().size()) {

                var checkedArgs = tyApp.args()
                        .zip(hk.argKinds())
                        .collect(pair -> checkType(pair.getOne(), pair.getTwo()));

                var updatedMeta = updateMetaWith(tyApp.meta(), TypeKind.INSTANCE);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);

            } else {
                var unsolvedArgs = tyApp.args()
                        .collect(arg -> varSupply.newUnsolvedKind());

                var unsolvedReturn = varSupply.newUnsolvedKind();

                var appliedKind = new HigherKind(
                        unsolvedArgs.collect(arg -> (Kind) arg),
                        unsolvedReturn);

                if (inferredTypeKind instanceof UnsolvedKind unsolved) {
                    environment.solveKind(unsolved, appliedKind);
                } else {
                    mismatchedTypeApplication(tyApp.range(), appliedKind, inferredTypeKind);
                }

                var checkedArgs = tyApp.args()
                        .zip(unsolvedArgs)
                        .collect(pair -> checkType(pair.getOne(), pair.getTwo()));

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
            var unsolvedKind = varSupply.newUnsolvedKind();
            var updatedMeta = updateMetaWith(forall.meta(), unsolvedKind);

            environment.populateType(forall.name(), updatedMeta);

            return forAllVarNode(updatedMeta, forall.name());

        } else if (typ instanceof ExistsVarNode<Name> exists) {
            var unsolvedKind = varSupply.newUnsolvedKind();
            var updatedMeta = updateMetaWith(exists.meta(), unsolvedKind);

            environment.populateType(exists.name(), updatedMeta);

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
                            if (pair.getOne() instanceof ForAllVarNode<Name> forall) {
                                var updatedMeta = updateMetaWith(forall.meta(), pair.getTwo());
                                environment.populateType(forall.name(), updatedMeta);
                                return (TypeVarNode<Attributes>) forAllVarNode(updatedMeta, forall.name());
                            } else if (pair.getOne() instanceof ExistsVarNode<Name> exists) {
                                var updatedMeta = updateMetaWith(exists.meta(), pair.getTwo());
                                environment.populateType(exists.name(), updatedMeta);
                                return (TypeVarNode<Attributes>) existsVarNode(updatedMeta, exists.name());
                            }
                            return null;
                        });

                var knownArgKinds = knownArgs
                        .collect(tyArg -> (Kind) tyArg.meta().meta().sort());

                var checkedReturn = checkType(tyLam.body(), TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(
                        tyLam.meta(),
                        new HigherKind(knownArgKinds, TypeKind.INSTANCE).substitute(environment.kindSubstitution()));

                return typeLambdaNode(updatedMeta, knownArgs, checkedReturn);
            });
        } else {
            var inferredType = inferType(typ);
            var inferredKind = (Kind) inferredType.meta().meta().sort();

            if (!checkSubKind(inferredKind, expectedKind)) {
                // TODO: Differentiate between a subkinding error and an occurs check failure
                mismatchedKind(inferredType.meta().range(), inferredKind, expectedKind);
            }

            return inferredType;
        }
    }

}
