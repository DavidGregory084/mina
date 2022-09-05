package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface MetaNodeVisitor<A, B> {

    // Namespaces
    B visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<B> declarations);

    // Declarations
    default B visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    B visitData(Meta<A> meta, String name, ImmutableList<B> typeParams, ImmutableList<B> constructors);

    B visitConstructor(Meta<A> meta, String name, ImmutableList<B> params, Optional<B> type);

    B visitConstructorParam(Meta<A> meta, String name, B typeAnnotation);

    B visitLet(Meta<A> meta, String name, Optional<B> type, B expr);

    B visitLetFn(Meta<A> meta, String name, ImmutableList<B> typeParams, ImmutableList<B> valueParams,
            Optional<B> returnType, B expr);

    B visitParam(Meta<A> param, String name, Optional<B> typeAnnotation);

    // Types
    default B visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    B visitTypeLambda(Meta<A> meta, ImmutableList<B> args, B body);

    B visitFunType(Meta<A> meta, ImmutableList<B> argTypes, B returnType);

    B visitTypeApply(Meta<A> meta, B type, ImmutableList<B> args);

    B visitTypeReference(Meta<A> meta, B id);

    default B visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }

    B visitForAllVar(Meta<A> meta, String name);

    B visitExistsVar(Meta<A> meta, String name);

    // Expressions
    default B visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    B visitBlock(Meta<A> meta, ImmutableList<B> declarations, B result);

    B visitIf(Meta<A> meta, B condition, B consequence, B alternative);

    B visitLambda(Meta<A> meta, ImmutableList<B> params, B body);

    B visitMatch(Meta<A> meta, B scrutinee, ImmutableList<B> cases);

    B visitApply(Meta<A> meta, B expr, ImmutableList<B> args);

    B visitReference(Meta<A> meta, B id);

    default B visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }

    B visitBoolean(Meta<A> meta, boolean value);

    B visitChar(Meta<A> meta, char value);

    B visitString(Meta<A> meta, String value);

    B visitInt(Meta<A> meta, int value);

    B visitLong(Meta<A> meta, long value);

    B visitFloat(Meta<A> meta, float value);

    B visitDouble(Meta<A> meta, double value);

    // Cases and patterns
    B visitCase(Meta<A> meta, B pattern, B consequent);

    default B visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    B visitConstructorPattern(Meta<A> meta, Optional<String> alias, B id, ImmutableList<B> fields);

    B visitFieldPattern(Meta<A> meta, String field, Optional<B> pattern);

    B visitIdPattern(Meta<A> meta, Optional<String> alias, String name);

    B visitLiteralPattern(Meta<A> meta, Optional<String> alias, B literal);

    // Identifiers
    B visitQualifiedId(Meta<A> meta, Optional<NamespaceIdNode> ns, String name);
}
