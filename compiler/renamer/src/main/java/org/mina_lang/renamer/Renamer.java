package org.mina_lang.renamer;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;
import org.mina_lang.syntax.*;

public class Renamer {

    private DiagnosticCollector diagnostics;
    private Environment<Name> environment;

    private int localVarIndex = 0;

    public Renamer(DiagnosticCollector diagnostics, Environment<Name> globalEnv) {
        this.diagnostics = diagnostics;
        this.environment = globalEnv;
    }

    public Environment<Name> getEnvironment() {
        return environment;
    }

    public NamespaceNode<Name> rename(NamespaceNode<Void> namespace) {
        var renamer = new RenamingTransformer();
        return namespace.accept(renamer);
    }

    public MetaNode<Name> rename(MetaNode<Void> node) {
        var renamer = new RenamingTransformer();
        return node.accept(renamer);
    }

    public Void duplicateValueDefinition(String name, Meta<Name> proposed, Meta<Name> existing) {
        var originalDefinition = new DiagnosticRelatedInformation(
                existing.range(),
                "Original definition of value '" + name + "'");
        diagnostics.reportError(
                proposed.range(),
                "Duplicate definition of value '" + name + "'",
                Lists.immutable.of(originalDefinition));
        return null;
    }

    public Void duplicateTypeDefinition(String name, Meta<Name> proposed, Meta<Name> existing) {
        var originalDefinition = new DiagnosticRelatedInformation(
                existing.range(),
                "Original definition of type '" + name + "'");
        diagnostics.reportError(
                proposed.range(),
                "Duplicate definition of type '" + name + "'",
                Lists.immutable.of(originalDefinition));
        return null;
    }

    public Void duplicateFieldDefinition(ConstructorName constr, String name, Meta<Name> proposed,
            Meta<Name> existing) {
        var originalDefinition = new DiagnosticRelatedInformation(
                existing.range(),
                "Original definition of field '" + name + "' in constructor '" + constr.name().canonicalName() + "'");
        diagnostics.reportError(
                proposed.range(),
                "Duplicate definition of field '" + name + "' in constructor '" + constr.name().canonicalName() + "'",
                Lists.immutable.of(originalDefinition));
        return null;
    }

    public void undefinedType(String name, Meta<Void> meta) {
        diagnostics.reportError(meta.range(), "Reference to undefined type '" + name + "'");
    }

    public void undefinedValue(String name, Meta<Void> meta) {
        diagnostics.reportError(meta.range(), "Reference to undefined value '" + name + "'");
    }

    public void unknownConstructor(String name, Meta<Void> meta) {
        diagnostics.reportError(meta.range(), "Reference to unknown constructor '" + name + "'");
    }

    public void unknownConstructorField(ConstructorName constr, String name, Meta<Void> meta) {
        diagnostics.reportError(meta.range(),
                // TODO: Print qualified names according to how they are introduced
                "Reference to unknown field '" + name + "' in constructor '" + constr.name().canonicalName() + "'");
    }

    public NamespaceScope<Name> populateTopLevel(NamespaceNode<Void> namespace) {
        var currentNamespace = namespace.getName();
        var namespaceScope = new NamespaceScope<Name>(currentNamespace);

        namespace.declarations().forEach(decl -> {
            if (decl instanceof DataNode<Void> data) {
                var dataName = data.getName(currentNamespace);
                var dataMeta = new Meta<Name>(data.range(), dataName);

                namespaceScope.populateTypeOrElse(dataName.localName(), dataMeta, this::duplicateTypeDefinition);
                namespaceScope.populateType(dataName.canonicalName(), dataMeta);

                data.constructors().forEach(constr -> {
                    var constrName = constr.getName(dataName, currentNamespace);
                    var constrMeta = new Meta<Name>(constr.range(), constrName);

                    namespaceScope.populateValueOrElse(constrName.localName(), constrMeta,
                            this::duplicateValueDefinition);
                    namespaceScope.populateValue(constrName.canonicalName(), constrMeta);

                    namespaceScope.populateTypeOrElse(constrName.localName(), constrMeta,
                            this::duplicateTypeDefinition);
                    namespaceScope.populateType(constrName.canonicalName(), constrMeta);

                    constr.params().forEach(constrParam -> {
                        var fieldName = new FieldName(constrName, constrParam.name());
                        var fieldMeta = new Meta<Name>(constrParam.range(), fieldName);
                        namespaceScope.populateFieldOrElse(
                                constrName, constrParam.name(), fieldMeta,
                                (name, proposed, existing) -> duplicateFieldDefinition(
                                        constrName,
                                        name,
                                        proposed,
                                        existing));
                    });
                });

            } else if (decl instanceof LetFnNode<Void> letFn) {
                var letFnName = letFn.getName(currentNamespace);
                var letFnMeta = new Meta<Name>(letFn.range(), letFnName);

                namespaceScope.populateValueOrElse(letFnName.localName(), letFnMeta, this::duplicateValueDefinition);
                namespaceScope.populateValue(letFnName.canonicalName(), letFnMeta);

            } else if (decl instanceof LetNode<Void> let) {
                var letName = let.getName(currentNamespace);
                var letMeta = new Meta<Name>(let.range(), letName);

                namespaceScope.populateValueOrElse(letName.localName(), letMeta, this::duplicateValueDefinition);
                namespaceScope.populateValue(letName.canonicalName(), letMeta);
            }
        });

        return namespaceScope;
    }

    class RenamingTransformer implements MetaNodeTransformer<Void, Name> {

        @Override
        public void preVisitNamespace(NamespaceNode<Void> namespace) {
            var namespaceScope = populateTopLevel(namespace);
            environment.pushScope(namespaceScope);
        }

        @Override
        public NamespaceNode<Name> visitNamespace(Meta<Void> meta, NamespaceIdNode id,
                ImmutableList<ImportNode> imports, ImmutableList<DeclarationNode<Name>> declarations) {
            var namespaceMeta = new Meta<Name>(meta.range(), environment.enclosingNamespace().get().namespace());
            return namespaceNode(namespaceMeta, id, imports, declarations);
        }

        @Override
        public void preVisitData(DataNode<Void> data) {
            var enclosingNamespace = environment.enclosingNamespace().get();
            var dataScope = new DataScope<Name>(data.getName(enclosingNamespace.namespace()));
            environment.pushScope(dataScope);
            data.typeParams().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                dataScope.populateTypeOrElse(tyParam.name(), tyParamMeta, Renamer.this::duplicateTypeDefinition);
            });
        }

        @Override
        public DataNode<Name> visitData(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ConstructorNode<Name>> constructors) {
            return dataNode(environment.lookupType(name).get(), name, typeParams, constructors);
        }

        @Override
        public void postVisitData(DataNode<Name> data) {
            environment.popScope(DataScope.class);
        }

        @Override
        public void preVisitConstructor(ConstructorNode<Void> constr) {
            var enclosingData = environment.enclosingData().get();
            var enclosingNamespace = environment.enclosingNamespace().get();
            var constrName = constr.getName(enclosingData.data(), enclosingNamespace.namespace());
            var constrScope = new ConstructorScope<Name>(constrName);
            environment.pushScope(constrScope);
        }

        @Override
        public ConstructorNode<Name> visitConstructor(Meta<Void> meta, String name,
                ImmutableList<ConstructorParamNode<Name>> params, Optional<TypeNode<Name>> type) {
            return constructorNode(environment.lookupValue(name).get(), name, params, type);
        }

        @Override
        public void postVisitConstructor(ConstructorNode<Name> constr) {
            environment.popScope(ConstructorScope.class);
        }

        @Override
        public ConstructorParamNode<Name> visitConstructorParam(Meta<Void> meta, String name,
                TypeNode<Name> typeAnnotation) {
            var enclosingConstructor = environment.enclosingConstructor().get();
            return constructorParamNode(
                    environment.lookupField(enclosingConstructor.constr(), name).get(), name, typeAnnotation);
        }

        @Override
        public void preVisitLet(LetNode<Void> let) {
            if (environment.topScope() instanceof BlockScope<Name> blockScope) {
                var letMeta = new Meta<Name>(let.range(), new LocalName(let.name(), localVarIndex++));
                // Local let bindings are only valid within the block scope and can shadow outer
                // declarations
                blockScope.populateValueOrElse(let.name(), letMeta, Renamer.this::duplicateValueDefinition);
            }
        }

        @Override
        public LetNode<Name> visitLet(Meta<Void> meta, String name, Optional<TypeNode<Name>> type,
                ExprNode<Name> expr) {
            return letNode(environment.lookupValue(name).get(), name, type, expr);
        }

        @Override
        public void postVisitLet(LetNode<Name> let) {
            if (environment.topScope() instanceof NamespaceScope) {
                // Reset local variable counter for each new top-level let binding
                localVarIndex = 0;
            }
        }

        @Override
        public void preVisitLetFn(LetFnNode<Void> letFn) {
            var typeLambdaScope = new TypeLambdaScope<Name>();

            letFn.typeParams().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                typeLambdaScope.populateTypeOrElse(tyParam.name(), tyParamMeta, Renamer.this::duplicateTypeDefinition);
            });

            // Emulate what happens for let-bound lambdas
            //
            // Type params
            environment.pushScope(typeLambdaScope);
            // Value params
            environment.pushScope(new LambdaScope<>());
        }

        @Override
        public LetFnNode<Name> visitLetFn(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ParamNode<Name>> valueParams, Optional<TypeNode<Name>> returnType, ExprNode<Name> expr) {
            return letFnNode(environment.lookupValue(name).get(), name, typeParams, valueParams, returnType, expr);
        }

        @Override
        public void postVisitLetFn(LetFnNode<Name> letFn) {
            // Value params
            environment.popScope(LambdaScope.class);
            // Type params
            environment.popScope(TypeLambdaScope.class);
        }

        @Override
        public void preVisitParam(ParamNode<Void> param) {
            var enclosingLambda = environment.enclosingLambda().get();
            var paramMeta = new Meta<Name>(param.range(), param.getName(localVarIndex++));
            // Only check the current lambda scope because lambda params can shadow outer
            // declarations
            enclosingLambda.populateValueOrElse(param.name(), paramMeta, Renamer.this::duplicateValueDefinition);
        }

        @Override
        public ParamNode<Name> visitParam(Meta<Void> param, String name, Optional<TypeNode<Name>> typeAnnotation) {
            return paramNode(environment.lookupValue(name).get(), name, typeAnnotation);
        }

        @Override
        public void preVisitTypeLambda(TypeLambdaNode<Void> tyLam) {
            var typeLambdaScope = new TypeLambdaScope<Name>();

            environment.pushScope(typeLambdaScope);

            tyLam.args().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                environment.populateTypeOrElse(tyParam.name(), tyParamMeta, Renamer.this::duplicateTypeDefinition);
            });
        }

        @Override
        public TypeLambdaNode<Name> visitTypeLambda(Meta<Void> meta, ImmutableList<TypeVarNode<Name>> args,
                TypeNode<Name> body) {
            return typeLambdaNode(new Meta<>(meta.range(), Nameless.INSTANCE), args, body);
        }

        @Override
        public void postVisitTypeLambda(TypeLambdaNode<Name> tyLam) {
            environment.popScope(TypeLambdaScope.class);
        }

        @Override
        public FunTypeNode<Name> visitFunType(Meta<Void> meta, ImmutableList<TypeNode<Name>> argTypes,
                TypeNode<Name> returnType) {
            return funTypeNode(new Meta<>(meta.range(), Nameless.INSTANCE), argTypes, returnType);
        }

        @Override
        public TypeApplyNode<Name> visitTypeApply(Meta<Void> meta, TypeNode<Name> type,
                ImmutableList<TypeNode<Name>> args) {
            return typeApplyNode(new Meta<>(meta.range(), Nameless.INSTANCE), type, args);
        }

        @Override
        public TypeReferenceNode<Name> visitTypeReference(Meta<Void> meta, QualifiedIdNode id) {
            var lookupMeta = environment
                    .lookupTypeOrElse(id.canonicalName(), meta, Renamer.this::undefinedType)
                    .map(typeMeta -> typeMeta.withRange(meta.range()))
                    .orElseGet(() -> {
                        return new Meta<>(meta.range(), Nameless.INSTANCE);
                    });

            return typeRefNode(lookupMeta, id);
        }

        @Override
        public ForAllVarNode<Name> visitForAllVar(Meta<Void> meta, String name) {
            var lookupMeta = environment.lookupType(name).get();
            return forAllVarNode(lookupMeta.withRange(meta.range()), name);
        }

        @Override
        public ExistsVarNode<Name> visitExistsVar(Meta<Void> meta, String name) {
            var lookupMeta = environment.lookupType(name).get();
            return existsVarNode(lookupMeta.withRange(meta.range()), name);
        }

        @Override
        public void preVisitBlock(BlockNode<Void> block) {
            environment.pushScope(new BlockScope<>());
        }

        @Override
        public BlockNode<Name> visitBlock(Meta<Void> meta, ImmutableList<LetNode<Name>> declarations,
                ExprNode<Name> result) {
            return blockNode(new Meta<>(meta.range(), Nameless.INSTANCE), declarations, result);
        }

        @Override
        public void postVisitBlock(BlockNode<Name> block) {
            environment.popScope(BlockScope.class);
        }

        @Override
        public IfNode<Name> visitIf(Meta<Void> meta, ExprNode<Name> condition, ExprNode<Name> consequent,
                ExprNode<Name> alternative) {
            return ifNode(new Meta<>(meta.range(), Nameless.INSTANCE), condition, consequent, alternative);
        }

        @Override
        public void preVisitLambda(LambdaNode<Void> lambda) {
            environment.pushScope(new LambdaScope<>());
        }

        @Override
        public LambdaNode<Name> visitLambda(Meta<Void> meta, ImmutableList<ParamNode<Name>> params,
                ExprNode<Name> body) {
            return lambdaNode(new Meta<>(meta.range(), Nameless.INSTANCE), params, body);
        }

        @Override
        public void postVisitLambda(LambdaNode<Name> lambda) {
            environment.popScope(LambdaScope.class);
        }

        @Override
        public MatchNode<Name> visitMatch(Meta<Void> meta, ExprNode<Name> scrutinee,
                ImmutableList<CaseNode<Name>> cases) {
            return matchNode(new Meta<>(meta.range(), Nameless.INSTANCE), scrutinee, cases);
        }

        @Override
        public ApplyNode<Name> visitApply(Meta<Void> meta, ExprNode<Name> expr, ImmutableList<ExprNode<Name>> args) {
            return applyNode(new Meta<>(meta.range(), Nameless.INSTANCE), expr, args);
        }

        @Override
        public ReferenceNode<Name> visitReference(Meta<Void> meta, QualifiedIdNode id) {
            var lookupMeta = environment
                    .lookupValueOrElse(id.canonicalName(), meta, Renamer.this::undefinedValue)
                    .map(refMeta -> refMeta.withRange(meta.range()))
                    .orElseGet(() -> new Meta<>(meta.range(), Nameless.INSTANCE));

            return refNode(lookupMeta, id);
        }

        @Override
        public BooleanNode<Name> visitBoolean(Meta<Void> meta, boolean value) {
            return boolNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public CharNode<Name> visitChar(Meta<Void> meta, char value) {
            return charNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public StringNode<Name> visitString(Meta<Void> meta, String value) {
            return stringNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public IntNode<Name> visitInt(Meta<Void> meta, int value) {
            return intNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public LongNode<Name> visitLong(Meta<Void> meta, long value) {
            return longNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public FloatNode<Name> visitFloat(Meta<Void> meta, float value) {
            return floatNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public DoubleNode<Name> visitDouble(Meta<Void> meta, double value) {
            return doubleNode(new Meta<>(meta.range(), Nameless.INSTANCE), value);
        }

        @Override
        public void preVisitCase(CaseNode<Void> cse) {
            environment.pushScope(new CaseScope<>());
        }

        @Override
        public CaseNode<Name> visitCase(Meta<Void> meta, PatternNode<Name> pattern, ExprNode<Name> consequent) {
            return caseNode(new Meta<>(meta.range(), Nameless.INSTANCE), pattern, consequent);
        }

        @Override
        public void postVisitCase(CaseNode<Name> cse) {
            environment.popScope(CaseScope.class);
        }

        @Override
        public void preVisitAliasPattern(AliasPatternNode<Void> alias) {
            var enclosingCase = environment.enclosingCase().get();
            var aliasMeta = new Meta<Name>(alias.range(), new LocalName(alias.alias(), localVarIndex++));
            // Only check the current case scope because pattern bindings can shadow outer
            // definitions
            enclosingCase.populateValueOrElse(alias.alias(), aliasMeta, Renamer.this::duplicateValueDefinition);
        }

        @Override
        public AliasPatternNode<Name> visitAliasPattern(Meta<Void> meta, String alias, PatternNode<Name> pattern) {
            return aliasPatternNode(environment.lookupValue(alias).get(), alias, pattern);
        }

        @Override
        public void preVisitConstructorPattern(ConstructorPatternNode<Void> constrPat) {
            var constrMeta = environment.lookupValueOrElse(
                    constrPat.id().canonicalName(),
                    constrPat.meta(),
                    Renamer.this::unknownConstructor);

            var constrName = constrMeta
                    .filter(meta -> meta.meta() instanceof ConstructorName)
                    .map(meta -> (ConstructorName) meta.meta());

            environment.pushScope(new ConstructorPatternScope<>(constrName));
        }

        @Override
        public ConstructorPatternNode<Name> visitConstructorPattern(Meta<Void> meta, QualifiedIdNode id,
                ImmutableList<FieldPatternNode<Name>> fields) {
            var lookupMeta = environment.lookupValue(id.canonicalName())
                    .map(constrMeta -> constrMeta.withRange(meta.range()))
                    .orElseGet(() -> new Meta<>(meta.range(), Nameless.INSTANCE));

            return constructorPatternNode(lookupMeta, id, fields);
        }

        @Override
        public void postVisitConstructorPattern(ConstructorPatternNode<Name> constrPat) {
            if (environment.topScope() instanceof ConstructorPatternScope<Name> constrPatternScope) {
                constrPatternScope.constr().ifPresent(constrName -> {
                    if (constrPat.meta().meta().equals(constrName)) {
                        environment.popScope(ConstructorPatternScope.class);
                    }
                });
            }
        }

        @Override
        public void preVisitFieldPattern(FieldPatternNode<Void> fieldPat) {
            var enclosingCase = environment.enclosingCase().get();
            var enclosingConstructorPattern = environment.enclosingConstructorPattern();

            enclosingConstructorPattern.flatMap(ConstructorPatternScope::constr).ifPresent(constr -> {
                if (environment.lookupField(constr, fieldPat.field()).isEmpty()) {
                    unknownConstructorField(constr, fieldPat.field(), fieldPat.meta());
                }

                if (fieldPat.pattern().isEmpty()) {
                    var patName = new LocalName(fieldPat.field(), localVarIndex++);
                    var patMeta = new Meta<Name>(fieldPat.range(), patName);

                    // Only check the current case scope because pattern bindings can shadow outer
                    // definitions
                    enclosingCase.populateValueOrElse(
                            fieldPat.field(), patMeta,
                            Renamer.this::duplicateValueDefinition);
                }
            });
        }

        @Override
        public FieldPatternNode<Name> visitFieldPattern(Meta<Void> meta, String field,
                Optional<PatternNode<Name>> pattern) {
            var fieldPatternMeta = pattern
                    // There's a nested pattern, so don't introduce a name for this field
                    .map(pat -> new Meta<Name>(meta.range(), Nameless.INSTANCE))
                    .or(() -> environment.lookupValue(field))
                    .orElseGet(() -> new Meta<Name>(meta.range(), Nameless.INSTANCE));
            return fieldPatternNode(fieldPatternMeta, field, pattern);
        }

        @Override
        public void preVisitIdPattern(IdPatternNode<Void> idPat) {
            var enclosingCase = environment.enclosingCase().get();
            var idPatName = new LocalName(idPat.name(), localVarIndex++);
            var idPatMeta = new Meta<Name>(idPat.range(), idPatName);
            // Only check the current case scope because pattern bindings can shadow outer
            // definitions
            enclosingCase.populateValueOrElse(idPat.name(), idPatMeta, Renamer.this::duplicateValueDefinition);
        }

        @Override
        public IdPatternNode<Name> visitIdPattern(Meta<Void> meta, String name) {
            return idPatternNode(environment.lookupValue(name).get(), name);
        }

        @Override
        public LiteralPatternNode<Name> visitLiteralPattern(Meta<Void> meta, LiteralNode<Name> literal) {
            return literalPatternNode(new Meta<>(meta.range(), Nameless.INSTANCE), literal);
        }
    }
}
