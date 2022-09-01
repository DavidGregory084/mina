package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public interface MetaNodeTransformer<A, B> {

    // Namespaces
    NamespaceNode<B> visitNamespace(Meta<A> meta, NamespaceIdNode<Void> id, ImmutableList<ImportNode<Void>> imports,
            ImmutableList<DeclarationNode<B>> declarations);

    // Declarations
    default DeclarationNode<B> visitDeclaration(DeclarationNode<A> decl) {
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

    DataNode<B> visitData(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ConstructorNode<B>> constructors);

    ConstructorNode<B> visitConstructor(Meta<A> meta, String name, ImmutableList<ConstructorParamNode<B>> params,
            Optional<TypeNode<B>> type);

    ConstructorParamNode<B> visitConstructorParam(Meta<A> meta, String name, TypeNode<B> typeAnnotation);

    LetNode<B> visitLet(Meta<A> meta, String name, Optional<TypeNode<B>> type, ExprNode<B> expr);

    LetFnNode<B> visitLetFn(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ParamNode<B>> valueParams, Optional<TypeNode<B>> returnType, ExprNode<B> expr);

    ParamNode<B> visitParam(Meta<A> param, String name, Optional<TypeNode<B>> typeAnnotation);

    // Types
    default TypeNode<B> visitType(TypeNode<A> typ) {
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

    TypeLambdaNode<B> visitTypeLambda(Meta<A> meta, ImmutableList<TypeVarNode<B>> args, TypeNode<B> body);

    FunTypeNode<B> visitFunType(Meta<A> meta, ImmutableList<TypeNode<B>> argTypes, TypeNode<B> returnType);

    TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type, ImmutableList<TypeNode<B>> args);

    TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode<B> id);

    default TypeVarNode<B> visitTypeVar(TypeVarNode<A> tyVar) {
        return switch (tyVar) {
            case ForAllVarNode<A> forAll ->
                visitForAllVar(forAll.meta(), forAll.name());
            case ExistsVarNode<A> exists ->
                visitExistsVar(exists.meta(), exists.name());
        };
    }

    ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name);

    ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name);

    // Expressions
    default ExprNode<B> visitExpr(ExprNode<A> expr) {
        return switch (expr) {
            case BlockNode<A> block ->
                visitBlock(
                        block.meta(),
                        block.declarations().collect(let -> {
                            return visitLet(
                                    let.meta(),
                                    let.name(),
                                    let.type().map(this::visitType),
                                    visitExpr(let.expr()));
                        }),
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

    BlockNode<B> visitBlock(Meta<A> meta, ImmutableList<LetNode<B>> declarations, ExprNode<B> result);

    IfNode<B> visitIf(Meta<A> meta, ExprNode<B> condition, ExprNode<B> consequence, ExprNode<B> alternative);

    LambdaNode<B> visitLambda(Meta<A> meta, ImmutableList<ParamNode<B>> params, ExprNode<B> body);

    MatchNode<B> visitMatch(Meta<A> meta, ExprNode<B> scrutinee, ImmutableList<CaseNode<B>> cases);

    ApplyNode<B> visitApply(Meta<A> meta, ExprNode<B> expr, ImmutableList<ExprNode<B>> args);

    ReferenceNode<B> visitReference(Meta<A> meta, QualifiedIdNode<B> id);

    default LiteralNode<B> visitLiteral(LiteralNode<A> literal) {
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

    BooleanNode<B> visitBoolean(Meta<A> meta, boolean value);

    CharNode<B> visitChar(Meta<A> meta, char value);

    StringNode<B> visitString(Meta<A> meta, String value);

    IntNode<B> visitInt(Meta<A> meta, int value);

    LongNode<B> visitLong(Meta<A> meta, long value);

    FloatNode<B> visitFloat(Meta<A> meta, float value);

    DoubleNode<B> visitDouble(Meta<A> meta, double value);

    // Cases and patterns
    CaseNode<B> visitCase(Meta<A> meta, PatternNode<B> pattern, ExprNode<B> consequent);

    default PatternNode<B> visitPattern(PatternNode<A> pat) {
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

    ConstructorPatternNode<B> visitConstructorPattern(Meta<A> meta, Optional<String> alias, QualifiedIdNode<B> id,
            ImmutableList<FieldPatternNode<B>> fields);

    FieldPatternNode<B> visitFieldPattern(Meta<A> meta, String field, Optional<PatternNode<B>> pattern);

    IdPatternNode<B> visitIdPattern(Meta<A> meta, Optional<String> alias, String name);

    LiteralPatternNode<B> visitLiteralPattern(Meta<A> meta, Optional<String> alias, LiteralNode<B> literal);

    // Identifiers
    QualifiedIdNode<B> visitQualifiedId(QualifiedIdNode<A> id);
}
