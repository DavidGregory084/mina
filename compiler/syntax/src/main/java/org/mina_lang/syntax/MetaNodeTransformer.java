package org.mina_lang.syntax;

public interface MetaNodeTransformer<A, B> extends MetaNodeVisitor<A, MetaNode<B>> {

    // Files
    @Override
    CompilationUnitNode<B> visitCompilationUnit(CompilationUnitNode<A> unit);

    // Modules
    @Override
    ModuleNode<B> visitModule(ModuleNode<A> mod);

    // Declarations
    @Override
    default DeclarationNode<B> visitDeclaration(DeclarationNode<A> decl) {
        return switch (decl) {
            case LetNode<A> let -> visitLet(let);
            case LetFnNode<A> letFn -> visitLetFn(letFn);
            case DataNode<A> data -> visitData(data);
        };
    }

    @Override
    DataNode<B> visitData(DataNode<A> data);

    @Override
    ConstructorNode<B> visitConstructor(ConstructorNode<A> constr);

    @Override
    ConstructorParamNode<B> visitConstructorParam(ConstructorParamNode<A> constrParam);

    @Override
    LetNode<B> visitLet(LetNode<A> let);

    @Override
    LetFnNode<B> visitLetFn(LetFnNode<A> letFn);

    @Override
    ParamNode<B> visitParam(ParamNode<A> param);

    // Expressions
    @Override
    default ExprNode<B> visitExpr(ExprNode<A> expr) {
        return switch (expr) {
            case BlockNode<A> block -> visitBlock(block);
            case IfNode<A> ifExpr -> visitIf(ifExpr);
            case LambdaNode<A> lambda -> visitLambda(lambda);
            case MatchNode<A> match -> visitMatch(match);
            case ApplyNode<A> apply -> visitApply(apply);
            case ReferenceNode<A> ref -> visitReference(ref);
            case LiteralNode<A> literal -> visitLiteral(literal);
        };
    }

    @Override
    BlockNode<B> visitBlock(BlockNode<A> blockExpr);

    @Override
    IfNode<B> visitIf(IfNode<A> ifExpr);

    @Override
    LambdaNode<B> visitLambda(LambdaNode<A> lambda);

    @Override
    MatchNode<B> visitMatch(MatchNode<A> match);

    @Override
    ApplyNode<B> visitApply(ApplyNode<A> apply);

    @Override
    ReferenceNode<B> visitReference(ReferenceNode<A> ref);

    @Override
    default LiteralNode<B> visitLiteral(LiteralNode<A> literal) {
        return switch (literal) {
            case BooleanNode<A> bool -> visitBoolean(bool);
            case CharNode<A> chr -> visitChar(chr);
            case DoubleNode<A> dbl -> visitDouble(dbl);
            case FloatNode<A> flt -> visitFloat(flt);
            case IntNode<A> intgr -> visitInt(intgr);
            case LongNode<A> lng -> visitLong(lng);
            case StringNode<A> str -> visitString(str);
        };
    }

    @Override
    BooleanNode<B> visitBoolean(BooleanNode<A> bool);

    @Override
    CharNode<B> visitChar(CharNode<A> chr);

    @Override
    DoubleNode<B> visitDouble(DoubleNode<A> dbl);

    @Override
    FloatNode<B> visitFloat(FloatNode<A> flt);

    @Override
    IntNode<B> visitInt(IntNode<A> intgr);

    @Override
    LongNode<B> visitLong(LongNode<A> lng);

    @Override
    StringNode<B> visitString(StringNode<A> str);

    // Cases and patterns
    @Override
    CaseNode<B> visitCase(CaseNode<A> cse);

    @Override
    default PatternNode<B> visitPattern(PatternNode<A> pat) {
        return switch (pat) {
            case ConstructorPatternNode<A> constrPat -> visitConstructorPattern(constrPat);
            case IdPatternNode<A> idPat -> visitIdPattern(idPat);
            case LiteralPatternNode<A> litPat -> visitLiteralPattern(litPat);
        };
    }

    @Override
    ConstructorPatternNode<B> visitConstructorPattern(ConstructorPatternNode<A> constrPat);

    @Override
    FieldPatternNode<B> visitFieldPattern(FieldPatternNode<A> fieldPat);

    @Override
    IdPatternNode<B> visitIdPattern(IdPatternNode<A> idPat);

    @Override
    LiteralPatternNode<B> visitLiteralPattern(LiteralPatternNode<A> litPat);

    // Identifiers
    @Override
    QualifiedIdNode<B> visitQualifiedId(QualifiedIdNode<A> id);
}
