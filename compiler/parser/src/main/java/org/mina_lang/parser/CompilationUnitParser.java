package org.mina_lang.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.mina_lang.common.Position;
import org.mina_lang.common.Range;
import org.mina_lang.parser.MinaParser.*;
import org.mina_lang.syntax.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class CompilationUnitParser {

    private ANTLRErrorListener errorListener;

    private CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor();
    private ModuleVisitor moduleVisitor = new ModuleVisitor();
    private ImportVisitor importVisitor = new ImportVisitor();
    private DeclarationVisitor declarationVisitor = new DeclarationVisitor();
    private TypeVisitor typeVisitor = new TypeVisitor();
    private ExprVisitor exprVisitor = new ExprVisitor();
    private LiteralVisitor literalVisitor = new LiteralVisitor();
    private MatchCaseVisitor matchCaseVisitor = new MatchCaseVisitor();
    private PatternVisitor patternVisitor = new PatternVisitor();
    private FieldPatternVisitor fieldPatternVisitor = new FieldPatternVisitor();
    private QualifiedIdVisitor qualifiedIdVisitor = new QualifiedIdVisitor();

    public CompilationUnitParser(ANTLRErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public CompilationUnitVisitor getCompilationUnitVisitor() {
        return compilationUnitVisitor;
    }

    public ModuleVisitor getModuleVisitor() {
        return moduleVisitor;
    }

    public ImportVisitor getImportVisitor() {
        return importVisitor;
    }

    public DeclarationVisitor getDeclarationVisitor() {
        return declarationVisitor;
    }

    public TypeVisitor getTypeVisitor() {
        return typeVisitor;
    }

    public ExprVisitor getExprVisitor() {
        return exprVisitor;
    }

    public LiteralVisitor getLiteralVisitor() {
        return literalVisitor;
    }

    public MatchCaseVisitor getMatchCaseVisitor() {
        return matchCaseVisitor;
    }

    public PatternVisitor getPatternVisitor() {
        return patternVisitor;
    }

    public FieldPatternVisitor getFieldPatternVisitor() {
        return fieldPatternVisitor;
    }

    public QualifiedIdVisitor getQualifiedIdVisitor() {
        return qualifiedIdVisitor;
    }

    public CompilationUnitNode<Void> parse(String source) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream);
    }

    public CompilationUnitNode<Void> parse(CharStream charStream) {
        return parse(charStream, CompilationUnitParser::getCompilationUnitVisitor, MinaParser::compilationUnit);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            String source,
            Function<CompilationUnitParser, C> visitor,
            Function<MinaParser, A> startRule) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, visitor, startRule);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            CharStream charStream,
            Function<CompilationUnitParser, C> visitor,
            Function<MinaParser, A> startRule) {
        var lexer = new MinaLexer(charStream);
        lexer.addErrorListener(errorListener);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream);
        parser.addErrorListener(errorListener);
        return visitor.apply(this).visitNullable(startRule.apply(parser));
    }

    static Range tokenRange(Token token) {
        var tokenLine = token.getLine() - 1;
        var startChar = token.getCharPositionInLine();
        var tokenLength = token.getText().length();
        return new Range(tokenLine, startChar, tokenLength);
    }

    static Range contextRange(ParserRuleContext ctx) {
        var startToken = ctx.getStart();
        var startLine = startToken.getLine() - 1;
        var startChar = startToken.getCharPositionInLine();
        var startPos = new Position(startLine, startChar);
        var endToken = ctx.getStop();
        var endLine = endToken.getLine() - 1;
        var endTokenLength = endToken.getText().length();
        var endChar = endToken.getCharPositionInLine() + endTokenLength;
        var endPos = new Position(endLine, endChar);
        return new Range(startPos, endPos);
    }

    abstract class Visitor<A extends ParserRuleContext, B> extends MinaBaseVisitor<B> {
        public B visitNullable(ParseTree tree) {
            return tree != null ? visit(tree) : null;
        }

        public <C extends ParserRuleContext, D> ImmutableList<D> visitRepeated(List<C> contexts,
                Visitor<C, D> visitor) {
            return contexts.stream()
                    .map(ctx -> visitor.visit(ctx))
                    .collect(Collectors2.toImmutableList());
        }

        public <C extends ParserRuleContext, D extends ParserRuleContext, E> ImmutableList<E> visitNullableRepeated(
                C context, Function<C, List<D>> rule,
                Visitor<D, E> visitor) {
            return context == null ? Lists.immutable.<E>empty()
                    : rule.apply(context).stream()
                            .map(ctx -> visitor.visit(ctx))
                            .collect(Collectors2.toImmutableList());
        }

        public B visitAlternatives(ParseTree... tree) {
            return Stream.of(tree)
                    .filter(t -> t != null)
                    .findFirst()
                    .map(t -> visit(t))
                    .orElse(null);
        }
    }

    class CompilationUnitVisitor extends Visitor<CompilationUnitContext, CompilationUnitNode<Void>> {

        @Override
        public CompilationUnitNode<Void> visitCompilationUnit(CompilationUnitContext ctx) {
            var modules = visitRepeated(ctx.module(), moduleVisitor);
            return compilationUnitNode(contextRange(ctx), modules);
        }
    }

    class ModuleVisitor extends Visitor<ModuleContext, ModuleNode<Void>> {

        @Override
        public ModuleNode<Void> visitModule(ModuleContext ctx) {
            var modId = Optional.ofNullable(ctx.moduleId());

            var modIdSegments = modId
                    .map(ModuleIdContext::ID)
                    .map(ids -> {
                        return ids.stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    });

            var pkg = modIdSegments
                    .map(ids -> ids.notEmpty() ? ids.take(ids.size() - 1) : ids)
                    .orElse(Lists.immutable.empty());

            var mod = modIdSegments
                    .flatMap(ids -> ids.getLastOptional())
                    .orElse(null);

            var imports = visitRepeated(ctx.importDeclaration(), importVisitor);

            var declarations = visitRepeated(ctx.declaration(), declarationVisitor);

            return moduleNode(contextRange(ctx), pkg, mod, imports, declarations);
        }
    }

    class ImportVisitor extends Visitor<ImportDeclarationContext, ImportNode<Void>> {

        @Override
        public ImportNode<Void> visitImportDeclaration(ImportDeclarationContext ctx) {
            var selector = Optional.ofNullable(ctx.importSelector());

            var modId = selector.map(ImportSelectorContext::moduleId);

            var modIdSegments = modId
                    .map(ModuleIdContext::ID)
                    .map(ids -> {
                        return ids.stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    });

            var pkg = modIdSegments
                    .map(ids -> ids.notEmpty() ? ids.take(ids.size() - 1) : ids)
                    .orElse(Lists.immutable.empty());

            var mod = modIdSegments
                    .flatMap(ids -> ids.getLastOptional())
                    .orElse(null);

            var symbol = selector
                    .map(ImportSelectorContext::ID)
                    .map(TerminalNode::getText)
                    .map(name -> Lists.immutable.of(name));

            var importSymbols = selector
                    .map(ImportSelectorContext::importSymbols)
                    .map(syms -> {
                        return syms.ID().stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    });

            var symbols = symbol
                    .or(() -> importSymbols)
                    .orElse(Lists.immutable.empty());

            return importNode(contextRange(ctx), pkg, mod, symbols);
        }
    }

    class DeclarationVisitor extends Visitor<DeclarationContext, DeclarationNode<Void>> {

        @Override
        public DataDeclarationNode<Void> visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            return dataDeclarationNode(contextRange(ctx), name);
        }

        @Override
        public LetDeclarationNode<Void> visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var expr = exprVisitor.visitNullable(ctx.expr());
            return letDeclarationNode(contextRange(ctx), name, expr);
        }

        @Override
        public DeclarationNode<Void> visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.dataDeclaration(), ctx.letDeclaration());
        }
    }

    class TypeVisitor extends Visitor<TypeContext, TypeNode<Void>> {

        @Override
        public TypeNode<Void> visitType(TypeContext ctx) {
            return visitAlternatives(ctx.typeLambda(), ctx.funType(), ctx.applicableType());
        }

        @Override
        public TypeLambdaNode<Void> visitTypeLambda(TypeLambdaContext ctx) {
            var params = ctx.typeParams().typeVar().stream()
                    .map(param -> visitTypeVar(param))
                    .collect(Collectors2.toImmutableList());

            var bodyNode = visitNullable(ctx.type());

            return typeLambdaNode(contextRange(ctx), params, bodyNode);
        }

        @Override
        public FunTypeNode<Void> visitFunType(FunTypeContext ctx) {
            var funTypeParams = Optional.ofNullable(ctx.funTypeParams());

            var paramTypeNode = funTypeParams
                    .map(FunTypeParamsContext::applicableType)
                    .map(this::visitNullable)
                    .map(Lists.immutable::of);

            var paramTypeNodes = paramTypeNode.orElse(
                    visitNullableRepeated(ctx.funTypeParams(), FunTypeParamsContext::type, this));

            var returnTypeNode = visitNullable(ctx.type());

            return funTypeNode(contextRange(ctx), paramTypeNodes, returnTypeNode);
        }

        @Override
        public TypeNode<Void> visitApplicableType(ApplicableTypeContext ctx) {
            var typeNode = visitAlternatives(ctx.parenType(), ctx.typeReference());

            if (typeNode != null) {
                return typeNode;
            }

            var applicableTypeNode = visitNullable(ctx.applicableType());

            if (applicableTypeNode != null) {
                var args = ctx.typeApplication().typeReference().stream()
                        .<TypeNode<Void>>map(ref -> visitTypeReference(ref))
                        .collect(Collectors2.toImmutableList());

                return typeApplyNode(contextRange(ctx), applicableTypeNode, args);
            }

            return null;
        }

        @Override
        public TypeReferenceNode<Void> visitTypeReference(TypeReferenceContext ctx) {
            var qualifiedIdNode = Optional.ofNullable(qualifiedIdVisitor.visitNullable(ctx.qualifiedId()));

            var varNode = Optional.ofNullable(ctx.typeVar()).flatMap(tv -> {
                return Optional.ofNullable(visitTypeVar(tv))
                        .map(tvNode -> idNode(contextRange(ctx), tvNode.name()));
            });

            var idNode = qualifiedIdNode.or(() -> varNode).orElse(null);

            return typeReferenceNode(contextRange(ctx), idNode);
        }

        @Override
        public TypeVarNode<Void> visitTypeVar(TypeVarContext ctx) {
            return ctx.QUESTION() == null ? forAllVarNode(contextRange(ctx), ctx.getText())
                    : existsVarNode(contextRange(ctx), ctx.getText());
        }

    }

    class ExprVisitor extends Visitor<ExprContext, ExprNode<Void>> {

        @Override
        public ExprNode<Void> visitExpr(ExprContext ctx) {
            return visitAlternatives(ctx.ifExpr(), ctx.lambdaExpr(), ctx.matchExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public IfExprNode<Void> visitIfExpr(IfExprContext ctx) {
            var conditionNode = visitNullable(ctx.expr(0));
            var consequentNode = visitNullable(ctx.expr(1));
            var alternativeNode = visitNullable(ctx.expr(2));
            return ifExprNode(contextRange(ctx), conditionNode, consequentNode, alternativeNode);
        }

        @Override
        public LambdaExprNode<Void> visitLambdaExpr(LambdaExprContext ctx) {
            var params = ctx.lambdaParams().lambdaParam().stream()
                    .map(param -> {
                        var token = param.ID().getSymbol();
                        return paramNode(tokenRange(token), param.getText());
                    })
                    .collect(Collectors2.toImmutableList());

            var bodyNode = visitNullable(ctx.expr());

            return lambdaExprNode(contextRange(ctx), params, bodyNode);
        }

        @Override
        public MatchNode<Void> visitMatchExpr(MatchExprContext ctx) {
            var scrutineeNode = visitNullable(ctx.expr());
            var cases = visitRepeated(ctx.matchCase(), matchCaseVisitor);
            return matchNode(contextRange(ctx), scrutineeNode, cases);
        }

        @Override
        public ExprNode<Void> visitApplicableExpr(ApplicableExprContext ctx) {
            var exprNode = visitAlternatives(ctx.parenExpr(), ctx.qualifiedId());

            if (exprNode != null) {
                return exprNode;
            }

            var applicableExprNode = visitNullable(ctx.applicableExpr());

            if (applicableExprNode != null) {
                var args = visitNullableRepeated(ctx.application(), ApplicationContext::expr, this);
                return applyNode(contextRange(ctx), applicableExprNode, args);
            }

            return null;
        }

        @Override
        public ExprNode<Void> visitParenExpr(ParenExprContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public ReferenceNode<Void> visitQualifiedId(QualifiedIdContext ctx) {
            return refNode(contextRange(ctx), qualifiedIdVisitor.visitNullable(ctx));
        }

        @Override
        public LiteralNode<Void> visitLiteral(LiteralContext ctx) {
            return literalVisitor.visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(),
                    ctx.literalInt(), ctx.literalFloat());
        }
    }

    class LiteralVisitor extends Visitor<LiteralContext, LiteralNode<Void>> {

        private LiteralIntNode<Void> safeIntNode(Range range, Token token, String withoutSuffix) {
            try {
                var decimalValue = new BigInteger(withoutSuffix);
                var intValue = decimalValue.intValueExact();
                return intNode(range, intValue);
            } catch (ArithmeticException exc) {
                errorListener.syntaxError(null, token, token.getLine(), token.getCharPositionInLine(),
                        "Integer overflow detected", null);
                return null;
            }
        }

        private LiteralLongNode<Void> safeLongNode(Range range, Token token, String withoutSuffix) {
            try {
                var decimalValue = new BigInteger(withoutSuffix);
                var longValue = decimalValue.longValueExact();
                return longNode(range, longValue);
            } catch (ArithmeticException exc) {
                errorListener.syntaxError(null, token, token.getLine(), token.getCharPositionInLine(),
                        "Long overflow detected", null);
                return null;
            }
        }

        private LiteralFloatNode<Void> safeFloatNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var floatValue = decimalValue.floatValue();

            if (!(Float.isNaN(floatValue) || Float.isInfinite(floatValue))) {
                if (new BigDecimal(String.valueOf(floatValue)).compareTo(decimalValue) == 0) {
                    return floatNode(range, floatValue);
                }
            }

            errorListener.syntaxError(null, token, token.getLine(), token.getCharPositionInLine(),
                    "Float precision loss detected", null);

            return null;
        }

        private LiteralDoubleNode<Void> safeDoubleNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var doubleValue = decimalValue.doubleValue();

            if (!(Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))) {
                if (new BigDecimal(String.valueOf(doubleValue)).compareTo(decimalValue) == 0) {
                    return doubleNode(range, doubleValue);
                }
            }

            errorListener.syntaxError(null, token, token.getLine(), token.getCharPositionInLine(),
                    "Double precision loss detected", null);

            return null;
        }

        @Override
        public LiteralNode<Void> visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(), ctx.literalInt(),
                    ctx.literalFloat());
        }

        @Override
        public LiteralBooleanNode<Void> visitLiteralBoolean(LiteralBooleanContext ctx) {
            if (ctx.TRUE() != null) {
                return boolNode(contextRange(ctx), true);
            }

            if (ctx.FALSE() != null) {
                return boolNode(contextRange(ctx), false);
            }

            return null;
        }

        @Override
        public LiteralCharNode<Void> visitLiteralChar(LiteralCharContext ctx) {
            var charExpr = ctx.LITERAL_CHAR();
            var charValue = StringEscapeUtils.unescapeJava(charExpr.getText()).charAt(1);
            return charNode(contextRange(ctx), charValue);
        }

        @Override
        public LiteralStringNode<Void> visitLiteralString(LiteralStringContext ctx) {
            var stringExpr = ctx.LITERAL_STRING();
            var unescapedText = StringEscapeUtils.unescapeJava(stringExpr.getText());
            var stringValue = unescapedText.substring(1, unescapedText.length() - 1);
            return stringNode(contextRange(ctx), stringValue);
        }

        @Override
        public LiteralNode<Void> visitLiteralInt(LiteralIntContext ctx) {
            var intExpr = ctx.LITERAL_INT();
            var intText = intExpr.getText().replace("_", "");
            if (intText.endsWith("l") || intText.endsWith("L")) {
                var withoutSuffix = intText.substring(0, intText.length() - 1);
                return safeLongNode(contextRange(ctx), intExpr.getSymbol(), withoutSuffix);
            } else if (intText.endsWith("i") || intText.endsWith("I")) {
                var withoutSuffix = intText.substring(0, intText.length() - 1);
                return safeIntNode(contextRange(ctx), intExpr.getSymbol(), withoutSuffix);
            } else {
                return safeIntNode(contextRange(ctx), intExpr.getSymbol(), intText);
            }
        }

        @Override
        public LiteralNode<Void> visitLiteralFloat(LiteralFloatContext ctx) {
            var floatExpr = ctx.LITERAL_FLOAT();
            var floatText = floatExpr.getText().replace("_", "");
            if (floatText.endsWith("d") || floatText.endsWith("D")) {
                var withoutSuffix = floatText.substring(0, floatText.length() - 1);
                return safeDoubleNode(contextRange(ctx), floatExpr.getSymbol(), withoutSuffix);
            } else if (floatText.endsWith("f") || floatText.endsWith("F")) {
                var withoutSuffix = floatText.substring(0, floatText.length() - 1);
                return safeFloatNode(contextRange(ctx), floatExpr.getSymbol(), withoutSuffix);
            } else {
                return safeDoubleNode(contextRange(ctx), floatExpr.getSymbol(), floatText);
            }
        }
    }

    class MatchCaseVisitor extends Visitor<MatchCaseContext, CaseNode<Void>> {

        @Override
        public CaseNode<Void> visitMatchCase(MatchCaseContext ctx) {
            var patternNode = patternVisitor.visitNullable(ctx.pattern());
            var consequentNode = exprVisitor.visitNullable(ctx.expr());
            return caseNode(contextRange(ctx), patternNode, consequentNode);
        }
    }

    class PatternVisitor extends Visitor<PatternContext, PatternNode<Void>> {

        @Override
        public PatternNode<Void> visitPattern(PatternContext ctx) {
            return visitAlternatives(ctx.idPattern(), ctx.literalPattern(), ctx.constructorPattern());
        }

        @Override
        public IdPatternNode<Void> visitIdPattern(IdPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var id = Optional.ofNullable(ctx.ID())
                    .map(TerminalNode::getText)
                    .orElse(null);

            return idPatternNode(contextRange(ctx), alias, id);
        }

        @Override
        public LiteralPatternNode<Void> visitLiteralPattern(LiteralPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var literal = literalVisitor.visit(ctx.literal());

            return literalPatternNode(contextRange(ctx), alias, literal);
        }

        @Override
        public ConstructorPatternNode<Void> visitConstructorPattern(ConstructorPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var id = qualifiedIdVisitor.visitNullable(ctx.qualifiedId());

            var fields = visitNullableRepeated(ctx.fieldPatterns(), FieldPatternsContext::fieldPattern,
                    fieldPatternVisitor);

            return constructorPatternNode(contextRange(ctx), alias, id, fields);
        }
    }

    class FieldPatternVisitor extends Visitor<FieldPatternContext, FieldPatternNode<Void>> {

        @Override
        public FieldPatternNode<Void> visitFieldPattern(FieldPatternContext ctx) {
            var id = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var pattern = Optional.ofNullable(patternVisitor.visitNullable(ctx.pattern()));
            return fieldPatternNode(contextRange(ctx), id, pattern);
        }
    }

    class QualifiedIdVisitor extends Visitor<QualifiedIdContext, QualifiedIdNode<Void>> {

        @Override
        public QualifiedIdNode<Void> visitQualifiedId(QualifiedIdContext ctx) {
            var pkg = Optional.ofNullable(ctx.moduleId())
                    .map(ModuleIdContext::ID)
                    .map(ids -> {
                        return ids.stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    })
                    .orElse(Lists.immutable.empty());

            var id = Optional.ofNullable(ctx.ID())
                    .map(TerminalNode::getText)
                    .orElse(null);

            return idNode(contextRange(ctx), pkg, id);
        }

    }
}
