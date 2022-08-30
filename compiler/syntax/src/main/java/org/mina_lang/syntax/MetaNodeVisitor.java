package org.mina_lang.syntax;

import java.util.function.Function;

public interface MetaNodeVisitor<A, B> {

    // Namespaces
    B visitNamespace(NamespaceNode<A> mod);

    // Declarations
    default B visitDeclaration(DeclarationNode<A> decl) {
        return switch (decl) {
            case LetNode<A> let -> visitLet(let);
            case LetFnNode<A> letFn -> visitLetFn(letFn);
            case DataNode<A> data -> visitData(data);
        };
    }

    B visitData(DataNode<A> data);

    B visitConstructor(ConstructorNode<A> constr);

    B visitConstructorParam(ConstructorParamNode<A> constrParam);

    B visitLet(LetNode<A> let);

    B visitLetFn(LetFnNode<A> letFn);

    B visitParam(ParamNode<A> param);

    // Expressions
    default B visitExpr(ExprNode<A> expr) {
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

    B visitBlock(BlockNode<A> block);

    B visitIf(IfNode<A> ifExpr);

    B visitLambda(LambdaNode<A> lambda);

    B visitMatch(MatchNode<A> match);

    B visitApply(ApplyNode<A> apply);

    B visitReference(ReferenceNode<A> ref);

    default B visitLiteral(LiteralNode<A> literal) {
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

    B visitBoolean(BooleanNode<A> bool);

    B visitChar(CharNode<A> chr);

    B visitString(StringNode<A> str);

    B visitInt(IntNode<A> intgr);

    B visitLong(LongNode<A> lng);

    B visitFloat(FloatNode<A> flt);

    B visitDouble(DoubleNode<A> dbl);

    // Cases and patterns
    B visitCase(CaseNode<A> cse);

    default B visitPattern(PatternNode<A> pat) {
        return switch (pat) {
            case ConstructorPatternNode<A> constrPat -> visitConstructorPattern(constrPat);
            case IdPatternNode<A> idPat -> visitIdPattern(idPat);
            case LiteralPatternNode<A> litPat -> visitLiteralPattern(litPat);
        };
    }

    B visitConstructorPattern(ConstructorPatternNode<A> constrPat);

    B visitFieldPattern(FieldPatternNode<A> fieldPat);

    B visitIdPattern(IdPatternNode<A> idPat);

    B visitLiteralPattern(LiteralPatternNode<A> litPat);

    // Identifiers
    B visitQualifiedId(QualifiedIdNode<A> id);

    default <C> MetaNodeVisitor<A, C> map(Function<B, C> f) {
        return new MetaNodeVisitor<A, C>() {

            @Override
            public C visitNamespace(NamespaceNode<A> mod) {
                return f.apply(MetaNodeVisitor.this.visitNamespace(mod));
            }

            @Override
            public C visitData(DataNode<A> data) {
                return f.apply(MetaNodeVisitor.this.visitData(data));
            }

            @Override
            public C visitConstructor(ConstructorNode<A> constr) {
                return f.apply(MetaNodeVisitor.this.visitConstructor(constr));
            }

            @Override
            public C visitConstructorParam(ConstructorParamNode<A> constrParam) {
                return f.apply(MetaNodeVisitor.this.visitConstructorParam(constrParam));
            }

            @Override
            public C visitLet(LetNode<A> let) {
                return f.apply(MetaNodeVisitor.this.visitLet(let));
            }

            @Override
            public C visitLetFn(LetFnNode<A> letFn) {
                return f.apply(MetaNodeVisitor.this.visitLetFn(letFn));
            }

            @Override
            public C visitParam(ParamNode<A> param) {
                return f.apply(MetaNodeVisitor.this.visitParam(param));
            }

            @Override
            public C visitBlock(BlockNode<A> block) {
                return f.apply(MetaNodeVisitor.this.visitBlock(block));
            }

            @Override
            public C visitIf(IfNode<A> ifExpr) {
                return f.apply(MetaNodeVisitor.this.visitIf(ifExpr));
            }

            @Override
            public C visitLambda(LambdaNode<A> lambda) {
                return f.apply(MetaNodeVisitor.this.visitLambda(lambda));
            }

            @Override
            public C visitMatch(MatchNode<A> match) {
                return f.apply(MetaNodeVisitor.this.visitMatch(match));
            }

            @Override
            public C visitApply(ApplyNode<A> apply) {
                return f.apply(MetaNodeVisitor.this.visitApply(apply));
            }

            @Override
            public C visitReference(ReferenceNode<A> ref) {
                return f.apply(MetaNodeVisitor.this.visitReference(ref));
            }

            @Override
            public C visitBoolean(BooleanNode<A> bool) {
                return f.apply(MetaNodeVisitor.this.visitBoolean(bool));
            }

            @Override
            public C visitChar(CharNode<A> chr) {
                return f.apply(MetaNodeVisitor.this.visitChar(chr));
            }

            @Override
            public C visitString(StringNode<A> str) {
                return f.apply(MetaNodeVisitor.this.visitString(str));
            }

            @Override
            public C visitInt(IntNode<A> intgr) {
                return f.apply(MetaNodeVisitor.this.visitInt(intgr));
            }

            @Override
            public C visitLong(LongNode<A> lng) {
                return f.apply(MetaNodeVisitor.this.visitLong(lng));
            }

            @Override
            public C visitFloat(FloatNode<A> flt) {
                return f.apply(MetaNodeVisitor.this.visitFloat(flt));
            }

            @Override
            public C visitDouble(DoubleNode<A> dbl) {
                return f.apply(MetaNodeVisitor.this.visitDouble(dbl));
            }

            @Override
            public C visitCase(CaseNode<A> cse) {
                return f.apply(MetaNodeVisitor.this.visitCase(cse));
            }

            @Override
            public C visitConstructorPattern(ConstructorPatternNode<A> constrPat) {
                return f.apply(MetaNodeVisitor.this.visitConstructorPattern(constrPat));
            }

            @Override
            public C visitFieldPattern(FieldPatternNode<A> fieldPat) {
                return f.apply(MetaNodeVisitor.this.visitFieldPattern(fieldPat));
            }

            @Override
            public C visitIdPattern(IdPatternNode<A> idPat) {
                return f.apply(MetaNodeVisitor.this.visitIdPattern(idPat));
            }

            @Override
            public C visitLiteralPattern(LiteralPatternNode<A> litPat) {
                return f.apply(MetaNodeVisitor.this.visitLiteralPattern(litPat));
            }

            @Override
            public C visitQualifiedId(QualifiedIdNode<A> id) {
                return f.apply(MetaNodeVisitor.this.visitQualifiedId(id));
            }
        };
    }

    default <C, D extends MetaNodeVisitor<A, C>> MetaNodeVisitor<A, C> flatMap(Function<B, D> f) {
        return new MetaNodeVisitor<A, C>() {

            @Override
            public C visitNamespace(NamespaceNode<A> mod) {
                return f.apply(MetaNodeVisitor.this.visitNamespace(mod)).visitNamespace(mod);
            }

            @Override
            public C visitData(DataNode<A> data) {
                return f.apply(MetaNodeVisitor.this.visitData(data)).visitData(data);
            }

            @Override
            public C visitConstructor(ConstructorNode<A> constr) {
                return f.apply(MetaNodeVisitor.this.visitConstructor(constr)).visitConstructor(constr);
            }

            @Override
            public C visitConstructorParam(ConstructorParamNode<A> constrParam) {
                return f.apply(MetaNodeVisitor.this.visitConstructorParam(constrParam)).visitConstructorParam(constrParam);
            }

            @Override
            public C visitLet(LetNode<A> let) {
                return f.apply(MetaNodeVisitor.this.visitLet(let)).visitLet(let);
            }

            @Override
            public C visitLetFn(LetFnNode<A> letFn) {
                return f.apply(MetaNodeVisitor.this.visitLetFn(letFn)).visitLetFn(letFn);
            }

            @Override
            public C visitParam(ParamNode<A> param) {
                return f.apply(MetaNodeVisitor.this.visitParam(param)).visitParam(param);
            }

            @Override
            public C visitBlock(BlockNode<A> block) {
                return f.apply(MetaNodeVisitor.this.visitBlock(block)).visitBlock(block);
            }

            @Override
            public C visitIf(IfNode<A> ifExpr) {
                return f.apply(MetaNodeVisitor.this.visitIf(ifExpr)).visitIf(ifExpr);
            }

            @Override
            public C visitLambda(LambdaNode<A> lambda) {
                return f.apply(MetaNodeVisitor.this.visitLambda(lambda)).visitLambda(lambda);
            }

            @Override
            public C visitMatch(MatchNode<A> match) {
                return f.apply(MetaNodeVisitor.this.visitMatch(match)).visitMatch(match);
            }

            @Override
            public C visitApply(ApplyNode<A> apply) {
                return f.apply(MetaNodeVisitor.this.visitApply(apply)).visitApply(apply);
            }

            @Override
            public C visitReference(ReferenceNode<A> ref) {
                return f.apply(MetaNodeVisitor.this.visitReference(ref)).visitReference(ref);
            }

            @Override
            public C visitBoolean(BooleanNode<A> bool) {
                return f.apply(MetaNodeVisitor.this.visitBoolean(bool)).visitBoolean(bool);
            }

            @Override
            public C visitChar(CharNode<A> chr) {
                return f.apply(MetaNodeVisitor.this.visitChar(chr)).visitChar(chr);
            }

            @Override
            public C visitString(StringNode<A> str) {
                return f.apply(MetaNodeVisitor.this.visitString(str)).visitString(str);
            }

            @Override
            public C visitInt(IntNode<A> intgr) {
                return f.apply(MetaNodeVisitor.this.visitInt(intgr)).visitInt(intgr);
            }

            @Override
            public C visitLong(LongNode<A> lng) {
                return f.apply(MetaNodeVisitor.this.visitLong(lng)).visitLong(lng);
            }

            @Override
            public C visitFloat(FloatNode<A> flt) {
                return f.apply(MetaNodeVisitor.this.visitFloat(flt)).visitFloat(flt);
            }

            @Override
            public C visitDouble(DoubleNode<A> dbl) {
                return f.apply(MetaNodeVisitor.this.visitDouble(dbl)).visitDouble(dbl);
            }

            @Override
            public C visitCase(CaseNode<A> cse) {
                return f.apply(MetaNodeVisitor.this.visitCase(cse)).visitCase(cse);
            }

            @Override
            public C visitConstructorPattern(ConstructorPatternNode<A> constrPat) {
                return f.apply(MetaNodeVisitor.this.visitConstructorPattern(constrPat)).visitConstructorPattern(constrPat);
            }

            @Override
            public C visitFieldPattern(FieldPatternNode<A> fieldPat) {
                return f.apply(MetaNodeVisitor.this.visitFieldPattern(fieldPat)).visitFieldPattern(fieldPat);
            }

            @Override
            public C visitIdPattern(IdPatternNode<A> idPat) {
                return f.apply(MetaNodeVisitor.this.visitIdPattern(idPat)).visitIdPattern(idPat);
            }

            @Override
            public C visitLiteralPattern(LiteralPatternNode<A> litPat) {
                return f.apply(MetaNodeVisitor.this.visitLiteralPattern(litPat)).visitLiteralPattern(litPat);
            }

            @Override
            public C visitQualifiedId(QualifiedIdNode<A> id) {
                return f.apply(MetaNodeVisitor.this.visitQualifiedId(id)).visitQualifiedId(id);
            }
        };
    }
}
