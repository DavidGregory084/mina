package org.mina_lang.renamer;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;
import org.mina_lang.syntax.*;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class Renamer {

    private DiagnosticCollector diagnostics;
    private NameEnvironment environment;
    private NamespaceName currentNamespace;

    public Renamer(DiagnosticCollector diagnostics, NameEnvironment globalEnv) {
        this.diagnostics = diagnostics;
        this.environment = globalEnv;
    }

    public NameEnvironment getEnvironment() {
        return environment;
    }

    public NamespaceNode<Name> rename(NamespaceNode<Void> namespace) {
        var renamer = new RenamingTransformer();
        populateTopLevel(namespace);
        return namespace.accept(renamer);
    }

    public MetaNode<Name> rename(MetaNode<Void> node) {
        var renamer = new RenamingTransformer();
        return node.accept(renamer);
    }

    public void populateTopLevel(NamespaceNode<Void> namespace) {
        currentNamespace = namespace.getName();
        var namespaceMeta = new Meta<Name>(namespace.range(), currentNamespace);

        environment.put(currentNamespace.localName(), namespaceMeta);
        environment.put(currentNamespace.canonicalName(), namespaceMeta);

        namespace.declarations().forEach(decl -> {
            if (decl instanceof DataNode<Void> data) {
                var dataName = data.getName(currentNamespace);
                var dataMeta = new Meta<Name>(data.range(), dataName);

                environment.put(dataName.localName(), dataMeta);
                environment.put(dataName.canonicalName(), dataMeta);

                data.constructors().forEach(constr -> {
                    var constrName = constr.getName(dataName, currentNamespace);
                    var constrMeta = new Meta<Name>(constr.range(), constrName);
                    environment.put(constrName.localName(), constrMeta);
                    environment.put(constrName.canonicalName(), constrMeta);
                });

            } else if (decl instanceof LetFnNode<Void> letFn) {
                var letFnName = letFn.getName(currentNamespace);
                var letFnMeta = new Meta<Name>(letFn.range(), letFnName);

                environment.put(letFnName.localName(), letFnMeta);
                environment.put(letFnName.canonicalName(), letFnMeta);

            } else if (decl instanceof LetNode<Void> let) {
                var letName = let.getName(currentNamespace);
                var letMeta = new Meta<Name>(let.range(), letName);

                environment.put(letName.localName(), letMeta);
                environment.put(letName.canonicalName(), letMeta);
            }
        });
    }

    class RenamingTransformer implements MetaNodeTransformer<Void, Name> {

        @Override
        public NamespaceNode<Name> visitNamespace(Meta<Void> meta, NamespaceIdNode id,
                ImmutableList<ImportNode> imports, ImmutableList<DeclarationNode<Name>> declarations) {
                    System.out.println(environment.names());
            return namespaceNode(environment.get(id.ns()), id, imports, declarations);
        }

        @Override
        public DataNode<Name> visitData(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ConstructorNode<Name>> constructors) {
            return dataNode(environment.get(name), name, typeParams, constructors);
        }

        @Override
        public ConstructorNode<Name> visitConstructor(Meta<Void> meta, String name,
                ImmutableList<ConstructorParamNode<Name>> params, Optional<TypeNode<Name>> type) {
            return constructorNode(environment.get(name), name, params, type);
        }

        @Override
        public ConstructorParamNode<Name> visitConstructorParam(Meta<Void> meta, String name,
                TypeNode<Name> typeAnnotation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LetNode<Name> visitLet(Meta<Void> meta, String name, Optional<TypeNode<Name>> type,
                ExprNode<Name> expr) {
            return null;
        }

        @Override
        public LetFnNode<Name> visitLetFn(Meta<Void> meta, String name, ImmutableList<TypeVarNode<Name>> typeParams,
                ImmutableList<ParamNode<Name>> valueParams, Optional<TypeNode<Name>> returnType, ExprNode<Name> expr) {
            return letFnNode(environment.get(name), name, typeParams, valueParams, returnType, expr);
        }

        @Override
        public ParamNode<Name> visitParam(Meta<Void> param, String name, Optional<TypeNode<Name>> typeAnnotation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TypeLambdaNode<Name> visitTypeLambda(Meta<Void> meta, ImmutableList<TypeVarNode<Name>> args,
                TypeNode<Name> body) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FunTypeNode<Name> visitFunType(Meta<Void> meta, ImmutableList<TypeNode<Name>> argTypes,
                TypeNode<Name> returnType) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TypeApplyNode<Name> visitTypeApply(Meta<Void> meta, TypeNode<Name> type,
                ImmutableList<TypeNode<Name>> args) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TypeReferenceNode<Name> visitTypeReference(Meta<Void> meta, QualifiedIdNode<Name> id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ForAllVarNode<Name> visitForAllVar(Meta<Void> meta, String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ExistsVarNode<Name> visitExistsVar(Meta<Void> meta, String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockNode<Name> visitBlock(Meta<Void> meta, ImmutableList<LetNode<Name>> declarations,
                ExprNode<Name> result) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IfNode<Name> visitIf(Meta<Void> meta, ExprNode<Name> condition, ExprNode<Name> consequence,
                ExprNode<Name> alternative) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LambdaNode<Name> visitLambda(Meta<Void> meta, ImmutableList<ParamNode<Name>> params,
                ExprNode<Name> body) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MatchNode<Name> visitMatch(Meta<Void> meta, ExprNode<Name> scrutinee,
                ImmutableList<CaseNode<Name>> cases) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ApplyNode<Name> visitApply(Meta<Void> meta, ExprNode<Name> expr, ImmutableList<ExprNode<Name>> args) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ReferenceNode<Name> visitReference(Meta<Void> meta, QualifiedIdNode<Name> id) {
            // TODO Auto-generated method stub
            return null;
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
        public CaseNode<Name> visitCase(Meta<Void> meta, PatternNode<Name> pattern, ExprNode<Name> consequent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ConstructorPatternNode<Name> visitConstructorPattern(Meta<Void> meta, Optional<String> alias,
                QualifiedIdNode<Name> id, ImmutableList<FieldPatternNode<Name>> fields) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FieldPatternNode<Name> visitFieldPattern(Meta<Void> meta, String field,
                Optional<PatternNode<Name>> pattern) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IdPatternNode<Name> visitIdPattern(Meta<Void> meta, Optional<String> alias, String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LiteralPatternNode<Name> visitLiteralPattern(Meta<Void> meta, Optional<String> alias,
                LiteralNode<Name> literal) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QualifiedIdNode<Name> visitQualifiedId(Meta<Void> meta, Optional<NamespaceIdNode> ns, String name) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
