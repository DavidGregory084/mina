package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public interface MetaNodeVisitor<A, B> {

    // Namespaces
    B visitNamespace(Meta<A> meta, NamespaceIdNode<Void> id, ImmutableList<ImportNode<Void>> imports,
            ImmutableList<B> declarations);

    // Declarations
    default B visitDeclaration(DeclarationNode<A> decl) {
        return switch (decl) {
            case LetNode<A> let ->
                visitLet(
                        let.meta(),
                        let.name(),
                        let.type().map(this::visitType),
                        visitExpr(let.expr()));
            case LetFnNode<A> letFn ->
                visitLetFn(
                        letFn.meta(),
                        letFn.name(),
                        letFn.typeParams().collect(this::visitTypeVar),
                        letFn.valueParams().collect(param -> {
                            return visitParam(
                                    param.meta(),
                                    param.name(),
                                    param.typeAnnotation().map(this::visitType));
                        }),
                        letFn.returnType().map(this::visitType),
                        visitExpr(letFn.expr()));
            case DataNode<A> data ->
                visitData(
                        data.meta(),
                        data.name(),
                        data.typeParams().collect(this::visitTypeVar),
                        data.constructors().collect(constr -> {
                            return visitConstructor(
                                    constr.meta(),
                                    constr.name(),
                                    constr.params().collect(param -> {
                                        return visitConstructorParam(
                                                param.meta(),
                                                param.name(),
                                                visitType(param.typeAnnotation()));
                                    }),
                                    constr.type().map(this::visitType));
                        }));
        };
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
        return switch (typ) {
            case TypeLambdaNode<A> tyLam ->
                visitTypeLambda(
                        tyLam.meta(),
                        tyLam.args().collect(this::visitTypeVar),
                        visitType(tyLam.body()));
            case FunTypeNode<A> funTyp ->
                visitFunType(
                        funTyp.meta(),
                        funTyp.argTypes().collect(this::visitType),
                        visitType(funTyp.returnType()));
            case TypeApplyNode<A> tyApp ->
                visitTypeApply(
                        tyApp.meta(),
                        visitType(tyApp.type()),
                        tyApp.args().collect(this::visitType));
            case TypeReferenceNode<A> tyRef ->
                visitTypeReference(
                        tyRef.meta(),
                        visitQualifiedId(tyRef.id()));
            case TypeVarNode<A> tyVar ->
                visitTypeVar(tyVar);
        };
    }

    B visitTypeLambda(Meta<A> meta, ImmutableList<B> args, B body);

    B visitFunType(Meta<A> meta, ImmutableList<B> argTypes, B returnType);

    B visitTypeApply(Meta<A> meta, B type, ImmutableList<B> args);

    B visitTypeReference(Meta<A> meta, B id);

    default B visitTypeVar(TypeVarNode<A> tyVar) {
        return switch (tyVar) {
            case ForAllVarNode<A> forAll ->
                visitForAllVar(forAll.meta(), forAll.name());
            case ExistsVarNode<A> exists ->
                visitExistsVar(exists.meta(), exists.name());
        };
    }

    B visitForAllVar(Meta<A> meta, String name);

    B visitExistsVar(Meta<A> meta, String name);

    // Expressions
    default B visitExpr(ExprNode<A> expr) {
        return switch (expr) {
            case BlockNode<A> block ->
                visitBlock(
                        block.meta(),
                        block.declarations().collect(this::visitDeclaration),
                        visitExpr(block.result()));
            case IfNode<A> ifExpr ->
                visitIf(
                        ifExpr.meta(),
                        visitExpr(ifExpr.condition()),
                        visitExpr(ifExpr.consequent()),
                        visitExpr(ifExpr.alternative()));
            case LambdaNode<A> lambda ->
                visitLambda(
                        lambda.meta(),
                        lambda.params().collect(param -> {
                            return visitParam(
                                    param.meta(),
                                    param.name(),
                                    param.typeAnnotation().map(this::visitType));
                        }),
                        visitExpr(lambda.body()));
            case MatchNode<A> match ->
                visitMatch(
                        match.meta(),
                        visitExpr(match.scrutinee()),
                        match.cases().collect(cse -> {
                            return visitCase(
                                    cse.meta(),
                                    visitPattern(cse.pattern()),
                                    visitExpr(cse.consequent()));
                        }));
            case ApplyNode<A> apply ->
                visitApply(
                        apply.meta(),
                        visitExpr(apply.expr()),
                        apply.args().collect(this::visitExpr));
            case ReferenceNode<A> ref ->
                visitReference(
                        ref.meta(),
                        visitQualifiedId(ref.id()));
            case LiteralNode<A> literal ->
                visitLiteral(literal);
        };
    }

    B visitBlock(Meta<A> meta, ImmutableList<B> declarations, B result);

    B visitIf(Meta<A> meta, B condition, B consequence, B alternative);

    B visitLambda(Meta<A> meta, ImmutableList<B> params, B body);

    B visitMatch(Meta<A> meta, B scrutinee, ImmutableList<B> cases);

    B visitApply(Meta<A> meta, B expr, ImmutableList<B> args);

    B visitReference(Meta<A> meta, B id);

    default B visitLiteral(LiteralNode<A> literal) {
        return switch (literal) {
            case BooleanNode<A> bool ->
                visitBoolean(bool.meta(), bool.value());
            case CharNode<A> chr ->
                visitChar(chr.meta(), chr.value());
            case DoubleNode<A> dbl ->
                visitDouble(dbl.meta(), dbl.value());
            case FloatNode<A> flt ->
                visitFloat(flt.meta(), flt.value());
            case IntNode<A> intgr ->
                visitInt(intgr.meta(), intgr.value());
            case LongNode<A> lng ->
                visitLong(lng.meta(), lng.value());
            case StringNode<A> str ->
                visitString(str.meta(), str.value());
        };
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
        return switch (pat) {
            case IdPatternNode<A> idPat ->
                visitIdPattern(
                        idPat.meta(),
                        idPat.alias(),
                        idPat.name());
            case LiteralPatternNode<A> litPat ->
                visitLiteralPattern(
                        litPat.meta(),
                        litPat.alias(),
                        visitLiteral(litPat.literal()));
            case ConstructorPatternNode<A> constrPat ->
                visitConstructorPattern(
                        constrPat.meta(),
                        constrPat.alias(),
                        visitQualifiedId(constrPat.id()),
                        constrPat.fields().collect(fieldPat -> {
                            return visitFieldPattern(
                                    fieldPat.meta(),
                                    fieldPat.field(),
                                    fieldPat.pattern().map(this::visitPattern));
                        }));
        };
    }

    B visitConstructorPattern(Meta<A> meta, Optional<String> alias, B id, ImmutableList<B> fields);

    B visitFieldPattern(Meta<A> meta, String field, Optional<B> pattern);

    B visitIdPattern(Meta<A> meta, Optional<String> alias, String name);

    B visitLiteralPattern(Meta<A> meta, Optional<String> alias, B literal);

    // Identifiers
    B visitQualifiedId(QualifiedIdNode<A> id);
}
