/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.common.Location;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.DiagnosticRelatedInformation;
import org.mina_lang.common.diagnostics.LocalDiagnosticReporter;
import org.mina_lang.common.names.*;
import org.mina_lang.renamer.scopes.*;
import org.mina_lang.syntax.*;

import java.util.Comparator;
import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class Renamer {

    private LocalDiagnosticReporter diagnostics;
    private NameEnvironment environment;

    private Graph<DeclarationName, DefaultEdge> declarationGraph = GraphTypeBuilder
            .<DeclarationName, DefaultEdge>directed()
            .allowingSelfLoops(true)
            .edgeClass(DefaultEdge.class)
            .buildGraph();

    private DOTExporter<DeclarationName, DefaultEdge> dotExporter = new DOTExporter<>();

    private int localVarIndex = 0;

    public Renamer(LocalDiagnosticReporter diagnostics, NameEnvironment environment) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        dotExporter.setVertexAttributeProvider(nsName -> Maps.mutable.of(
                "label", DefaultAttribute.createAttribute(nsName.localName())));
    }

    public NameEnvironment getEnvironment() {
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
                new Location(diagnostics.getSourceUri(), existing.range()),
                "Original definition of value '" + name + "'");
        diagnostics.reportError(
                proposed.range(),
                "Duplicate definition of value '" + name + "'",
                Lists.immutable.of(originalDefinition));
        return null;
    }

    public Void duplicateTypeDefinition(String name, Meta<Name> proposed, Meta<Name> existing) {
        var originalDefinition = new DiagnosticRelatedInformation(
                new Location(diagnostics.getSourceUri(), existing.range()),
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
                new Location(diagnostics.getSourceUri(), existing.range()),
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
        diagnostics.reportError(
                meta.range(),
                // TODO: Print qualified names according to how they are introduced
                "Reference to unknown field '" + name + "' in constructor '" + constr.name().canonicalName() + "'");
    }

    public NamespaceNamingScope populateTopLevel(NamespaceNode<Void> namespace) {
        var currentNamespace = namespace.getName();
        var namespaceScope = new NamespaceNamingScope(currentNamespace);

        namespace.declarationGroups().forEach(decls -> {
            decls.forEach(decl -> {
                if (decl instanceof LetFnNode<Void> letFn) {
                    var letFnName = letFn.getName(currentNamespace);
                    var letFnMeta = new Meta<Name>(letFn.range(), letFnName);

                    namespaceScope.putValueIfAbsentOrElse(letFnName.localName(), letFnMeta,
                            this::duplicateValueDefinition);
                    namespaceScope.putValueIfAbsent(letFnName.canonicalName(), letFnMeta);

                } else if (decl instanceof LetNode<Void> let) {
                    var letName = let.getName(currentNamespace);
                    var letMeta = new Meta<Name>(let.range(), letName);

                    namespaceScope.putValueIfAbsentOrElse(letName.localName(), letMeta, this::duplicateValueDefinition);
                    namespaceScope.putValueIfAbsent(letName.canonicalName(), letMeta);
                } else if (decl instanceof DataNode<Void> data) {
                    var dataName = data.getName(currentNamespace);
                    var dataMeta = new Meta<Name>(data.range(), dataName);

                    namespaceScope.putTypeIfAbsentOrElse(dataName.localName(), dataMeta, this::duplicateTypeDefinition);
                    namespaceScope.putTypeIfAbsent(dataName.canonicalName(), dataMeta);

                    data.constructors().forEach(constr -> {
                        var constrName = constr.getName(dataName, currentNamespace);
                        var constrMeta = new Meta<Name>(constr.range(), constrName);

                        namespaceScope.putValueIfAbsentOrElse(constrName.localName(), constrMeta,
                                this::duplicateValueDefinition);
                        namespaceScope.putValueIfAbsent(constrName.canonicalName(), constrMeta);

                        constr.params().forEach(constrParam -> {
                            var fieldName = new FieldName(constrName, constrParam.name());
                            var fieldMeta = new Meta<Name>(constrParam.range(), fieldName);
                            namespaceScope.putFieldIfAbsentOrElse(
                                    constrName, constrParam.name(), fieldMeta,
                                    (name, proposed, existing) -> duplicateFieldDefinition(
                                            constrName,
                                            name,
                                            proposed,
                                            existing));
                        });
                    });
                }
            });
        });

        return namespaceScope;
    }

    void updateDeclarationGraph(DeclarationName source, DeclarationName target) {
        declarationGraph.addVertex(source);
        declarationGraph.addVertex(target);
        declarationGraph.addEdge(source, target);
    }

    void updateDeclarationGraph(Meta<Name> refMeta) {
        environment.enclosingNamespace().ifPresent(namespace -> {
            environment.enclosingDeclaration().ifPresent(declaration -> {
                if (refMeta.meta() instanceof DeclarationName declName &&
                        namespace.namespace().equals(declName.name().ns())) {
                    updateDeclarationGraph(declName, declaration.declarationName());
                }
            });
        });
    }

    class RenamingTransformer implements MetaNodeTransformer<Void, Name> {

        @Override
        public void preVisitNamespace(NamespaceNode<Void> namespace) {
            var namespaceScope = populateTopLevel(namespace);
            environment.pushScope(namespaceScope);
        }

        @Override
        public NamespaceNode<Name> visitNamespace(
                Meta<Void> meta,
                NamespaceIdNode id,
                ImmutableList<ImportNode> imports,
                ImmutableList<ImmutableList<DeclarationNode<Name>>> declarationGroups) {

            var namespaceMeta = new Meta<Name>(meta.range(), environment.enclosingNamespace().get().namespace());

            var strongConnectivityInspector = new KosarajuStrongConnectivityInspector<>(declarationGraph);

            var connectedComponents = DirectedAcyclicGraph
                    .<Graph<DeclarationName, DefaultEdge>, DefaultEdge>createBuilder(DefaultEdge.class)
                    .addGraph(strongConnectivityInspector.getCondensation())
                    .build();

            if (connectedComponents.vertexSet().isEmpty()) {
                return new NamespaceNode<>(namespaceMeta, id, imports, declarationGroups);
            } else {
                var sortedDeclarations = Lists.mutable
                        .<ImmutableList<DeclarationNode<Name>>>empty();

                var unsortedDeclarations = declarationGroups.getFirst()
                        .<DeclarationName, DeclarationNode<Name>>toMap(
                                decl -> (DeclarationName) decl.meta().meta(),
                                decl -> decl);

                connectedComponents.iterator().forEachRemaining(subGraph -> {
                    var declarationGroup = subGraph.vertexSet().stream()
                            .filter(unsortedDeclarations::containsKey)
                            .map(unsortedDeclarations::get)
                            .sorted(Comparator.comparing(decl -> decl.range().start()))
                            .collect(Collectors2.toImmutableList());

                    declarationGroup.forEach(decl -> {
                        unsortedDeclarations.remove(decl.meta().meta());
                    });

                    if (!declarationGroup.isEmpty()) {
                        sortedDeclarations.add(declarationGroup);
                    }
                });

                var disconnectedComponents = unsortedDeclarations.valuesView().toImmutableList();

                if (!disconnectedComponents.isEmpty()) {
                    sortedDeclarations.add(disconnectedComponents);
                }

                return new NamespaceNode<>(namespaceMeta, id, imports, sortedDeclarations.toImmutable());
            }
        }

        @Override
        public void preVisitData(DataNode<Void> data) {
            var enclosingNamespace = environment.enclosingNamespace().get();
            var dataName = data.getName(enclosingNamespace.namespace());
            var dataScope = new DataNamingScope(dataName);
            environment.pushScope(dataScope);
            data.typeParams().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                dataScope.putTypeIfAbsentOrElse(tyParam.name(), tyParamMeta, Renamer.this::duplicateTypeDefinition);
            });
        }

        @Override
        public DataNode<Name> visitData(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ConstructorNode<Name>> constructors) {
            return dataNode(environment.lookupType(name).get(), name, typeParams, constructors);
        }

        @Override
        public void postVisitData(DataNode<Name> data) {
            environment.popScope(DataNamingScope.class);
        }

        @Override
        public void preVisitConstructor(ConstructorNode<Void> constr) {
            var enclosingData = environment.enclosingData().get();
            var enclosingNamespace = environment.enclosingNamespace().get();
            var constrName = constr.getName(enclosingData.data(), enclosingNamespace.namespace());
            environment.pushScope(new ConstructorNamingScope(constrName));
            // Data declarations must be compiled before any references to their constructors
            updateDeclarationGraph(enclosingData.data(), constrName);
            // Data declarations depend upon references to other data types in their constructors
            updateDeclarationGraph(constrName, enclosingData.data());
        }

        @Override
        public ConstructorNode<Name> visitConstructor(Meta<Void> meta, String name,
                ImmutableList<ConstructorParamNode<Name>> params, Optional<TypeNode<Name>> type) {
            return constructorNode(environment.lookupValue(name).get(), name, params, type);
        }

        @Override
        public void postVisitConstructor(ConstructorNode<Name> constr) {
            environment.popScope(ConstructorNamingScope.class);
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
            if (environment.topScope() instanceof BlockNamingScope blockScope) {
                var letMeta = new Meta<Name>(let.range(), new LocalName(let.name(), localVarIndex++));
                // Local let bindings are only valid within the block scope and can shadow outer
                // declarations
                blockScope.putValueIfAbsentOrElse(let.name(), letMeta, Renamer.this::duplicateValueDefinition);
            } else {
                var letMeta = environment.lookupValue(let.name()).get();
                environment.pushScope(new LetNamingScope((LetName) letMeta.meta()));
            }
        }

        @Override
        public LetNode<Name> visitLet(Meta<Void> meta, String name, Optional<TypeNode<Name>> type,
                ExprNode<Name> expr) {
            return letNode(environment.lookupValue(name).get(), name, type, expr);
        }

        @Override
        public void postVisitLet(LetNode<Name> let) {
            if (!(environment.topScope() instanceof BlockNamingScope)) {
                environment.popScope(LetNamingScope.class);
            }
        }

        @Override
        public void preVisitLetFn(LetFnNode<Void> letFn) {
            var letMeta = environment.lookupValue(letFn.name()).get();
            var letScope = new LetNamingScope((LetName) letMeta.meta());

            letFn.typeParams().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                letScope.putTypeIfAbsentOrElse(tyParam.name(), tyParamMeta,
                        Renamer.this::duplicateTypeDefinition);
            });

            // Type params
            environment.pushScope(letScope);
            // Value params
            environment.pushScope(new LambdaNamingScope());
        }

        @Override
        public LetFnNode<Name> visitLetFn(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ParamNode<Name>> valueParams, Optional<TypeNode<Name>> returnType, ExprNode<Name> expr) {
            return letFnNode(environment.lookupValue(name).get(), name, typeParams, valueParams, returnType, expr);
        }

        @Override
        public void postVisitLetFn(LetFnNode<Name> letFn) {
            // Value params
            environment.popScope(LambdaNamingScope.class);
            // Type params
            environment.popScope(LetNamingScope.class);
        }

        @Override
        public void preVisitParam(ParamNode<Void> param) {
            var enclosingLambda = environment.enclosingLambda().get();
            var paramMeta = new Meta<Name>(param.range(), param.getName(localVarIndex++));
            // Only check the current lambda scope because lambda params can shadow outer
            // declarations
            enclosingLambda.putValueIfAbsentOrElse(param.name(), paramMeta, Renamer.this::duplicateValueDefinition);
        }

        @Override
        public ParamNode<Name> visitParam(Meta<Void> param, String name, Optional<TypeNode<Name>> typeAnnotation) {
            return paramNode(environment.lookupValue(name).get(), name, typeAnnotation);
        }

        @Override
        public void preVisitQuantifiedType(QuantifiedTypeNode<Void> quant) {
            var quantifiedTypeScope = new QuantifiedTypeNamingScope();

            environment.pushScope(quantifiedTypeScope);

            quant.args().forEach(tyParam -> {
                var tyParamMeta = new Meta<Name>(tyParam.range(), tyParam.getName());
                environment.putTypeIfAbsentOrElse(tyParam.name(), tyParamMeta, Renamer.this::duplicateTypeDefinition);
            });
        }

        @Override
        public QuantifiedTypeNode<Name> visitQuantifiedType(Meta<Void> meta, ImmutableList<TypeVarNode<Name>> args,
                                                            TypeNode<Name> body) {
            return quantifiedTypeNode(new Meta<>(meta.range(), Nameless.INSTANCE), args, body);
        }

        @Override
        public void postVisitQuantifiedType(QuantifiedTypeNode<Name> quant) {
            environment.popScope(QuantifiedTypeNamingScope.class);
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

            updateDeclarationGraph(lookupMeta);

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
            environment.pushScope(new BlockNamingScope());
        }

        @Override
        public BlockNode<Name> visitBlock(Meta<Void> meta, ImmutableList<LetNode<Name>> declarations,
                Optional<ExprNode<Name>> result) {
            return blockNode(new Meta<>(meta.range(), Nameless.INSTANCE), declarations, result);
        }

        @Override
        public void postVisitBlock(BlockNode<Name> block) {
            environment.popScope(BlockNamingScope.class);
        }

        @Override
        public IfNode<Name> visitIf(Meta<Void> meta, ExprNode<Name> condition, ExprNode<Name> consequent,
                ExprNode<Name> alternative) {
            return ifNode(new Meta<>(meta.range(), Nameless.INSTANCE), condition, consequent, alternative);
        }

        @Override
        public void preVisitLambda(LambdaNode<Void> lambda) {
            environment.pushScope(new LambdaNamingScope());
        }

        @Override
        public LambdaNode<Name> visitLambda(Meta<Void> meta, ImmutableList<ParamNode<Name>> params,
                ExprNode<Name> body) {
            return lambdaNode(new Meta<>(meta.range(), Nameless.INSTANCE), params, body);
        }

        @Override
        public void postVisitLambda(LambdaNode<Name> lambda) {
            environment.popScope(LambdaNamingScope.class);
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
        public SelectNode<Name> visitSelect(Meta<Void> meta, ExprNode<Name> receiver, ReferenceNode<Name> selection) {
            return selectNode(new Meta<>(meta.range(), Nameless.INSTANCE), receiver, selection);
        }

        @Override
        public ReferenceNode<Name> visitReference(Meta<Void> meta, QualifiedIdNode id) {
            var lookupMeta = environment
                    .lookupValueOrElse(id.canonicalName(), meta, Renamer.this::undefinedValue)
                    .map(refMeta -> refMeta.withRange(meta.range()))
                    .orElseGet(() -> new Meta<>(meta.range(), Nameless.INSTANCE));

            updateDeclarationGraph(lookupMeta);

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
            environment.pushScope(new CaseNamingScope());
        }

        @Override
        public CaseNode<Name> visitCase(Meta<Void> meta, PatternNode<Name> pattern, ExprNode<Name> consequent) {
            return caseNode(new Meta<>(meta.range(), Nameless.INSTANCE), pattern, consequent);
        }

        @Override
        public void postVisitCase(CaseNode<Name> cse) {
            environment.popScope(CaseNamingScope.class);
        }

        @Override
        public void preVisitAliasPattern(AliasPatternNode<Void> alias) {
            var enclosingCase = environment.enclosingCase().get();
            var aliasMeta = new Meta<Name>(alias.range(), new LocalName(alias.alias(), localVarIndex++));
            // Only check the current case scope because pattern bindings can shadow outer
            // definitions
            enclosingCase.putValueIfAbsentOrElse(alias.alias(), aliasMeta, Renamer.this::duplicateValueDefinition);
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

            environment.pushScope(new ConstructorPatternNamingScope(constrName));
        }

        @Override
        public ConstructorPatternNode<Name> visitConstructorPattern(Meta<Void> meta, QualifiedIdNode id,
                ImmutableList<FieldPatternNode<Name>> fields) {
            var lookupMeta = environment.lookupValue(id.canonicalName())
                    .map(constrMeta -> constrMeta.withRange(meta.range()))
                    .orElseGet(() -> new Meta<>(meta.range(), Nameless.INSTANCE));

            updateDeclarationGraph(lookupMeta);

            return constructorPatternNode(lookupMeta, id, fields);
        }

        @Override
        public void postVisitConstructorPattern(ConstructorPatternNode<Name> constrPat) {
            environment.popScope(ConstructorPatternNamingScope.class);
        }

        @Override
        public void preVisitFieldPattern(FieldPatternNode<Void> fieldPat) {
            var enclosingCase = environment.enclosingCase().get();
            var enclosingConstructorPattern = environment.enclosingConstructorPattern();

            enclosingConstructorPattern.flatMap(ConstructorPatternNamingScope::constr).ifPresent(constr -> {
                if (environment.lookupField(constr, fieldPat.field()).isEmpty()) {
                    unknownConstructorField(constr, fieldPat.field(), fieldPat.meta());
                }

                if (fieldPat.pattern().isEmpty()) {
                    var patName = new LocalName(fieldPat.field(), localVarIndex++);
                    var patMeta = new Meta<Name>(fieldPat.range(), patName);

                    // Only check the current case scope because pattern bindings can shadow outer
                    // definitions
                    enclosingCase.putValueIfAbsentOrElse(
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
            enclosingCase.putValueIfAbsentOrElse(idPat.name(), idPatMeta, Renamer.this::duplicateValueDefinition);
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
