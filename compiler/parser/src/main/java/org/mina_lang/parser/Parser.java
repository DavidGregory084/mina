/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.parser;

import com.opencastsoftware.yvette.Position;
import com.opencastsoftware.yvette.Range;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.parser.MinaParser.*;
import org.mina_lang.syntax.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class Parser {

    private ANTLRDiagnosticReporter diagnostics;

    private NamespaceIdVisitor namespaceIdVisitor = new NamespaceIdVisitor();
    private NamespaceVisitor namespaceVisitor = new NamespaceVisitor();
    private ImportVisitor importVisitor = new ImportVisitor();
    private ImporteeVisitor importeeVisitor = new ImporteeVisitor();
    private DeclarationVisitor declarationVisitor = new DeclarationVisitor();
    private ConstructorVisitor constructorVisitor = new ConstructorVisitor();
    private ConstructorParamVisitor constructorParamVisitor = new ConstructorParamVisitor();
    private TypeVisitor typeVisitor = new TypeVisitor();
    private ExprVisitor exprVisitor = new ExprVisitor();
    private ParamVisitor paramVisitor = new ParamVisitor();
    private LiteralVisitor literalVisitor = new LiteralVisitor();
    private MatchCaseVisitor matchCaseVisitor = new MatchCaseVisitor();
    private PatternVisitor patternVisitor = new PatternVisitor();
    private FieldPatternVisitor fieldPatternVisitor = new FieldPatternVisitor();
    private QualifiedIdVisitor qualifiedIdVisitor = new QualifiedIdVisitor();

    private static final ThreadLocal<DFA[]> lexerDFA = ThreadLocal.withInitial(() -> {
        var atn = MinaLexer._ATN;
        return IntStream.range(0, atn.getNumberOfDecisions())
            .mapToObj(i -> new DFA(atn.getDecisionState(i), i))
            .toArray(DFA[]::new);
    });

    private static final ThreadLocal<PredictionContextCache> lexerCache =
        ThreadLocal.withInitial(PredictionContextCache::new);

    private static final ThreadLocal<DFA[]> parserDFA = ThreadLocal.withInitial(() -> {
        var atn = MinaParser._ATN;
        return IntStream.range(0, atn.getNumberOfDecisions())
            .mapToObj(i -> new DFA(atn.getDecisionState(i), i))
            .toArray(DFA[]::new);
    });

    private static final ThreadLocal<PredictionContextCache> parserCache =
        ThreadLocal.withInitial(PredictionContextCache::new);

    public Parser(ANTLRDiagnosticReporter diagnostics) {
        this.diagnostics = diagnostics;
    }

    NamespaceVisitor getNamespaceVisitor() {
        return namespaceVisitor;
    }

    ImportVisitor getImportVisitor() {
        return importVisitor;
    }

    ImporteeVisitor getImporteeVisitor() {
        return importeeVisitor;
    }

    DeclarationVisitor getDeclarationVisitor() {
        return declarationVisitor;
    }

    ConstructorVisitor getConstructorVisitor() {
        return constructorVisitor;
    }

    ConstructorParamVisitor getConstructorParamVisitor() {
        return constructorParamVisitor;
    }

    TypeVisitor getTypeVisitor() {
        return typeVisitor;
    }

    ExprVisitor getExprVisitor() {
        return exprVisitor;
    }

    ParamVisitor getParamVisitor() {
        return paramVisitor;
    }

    LiteralVisitor getLiteralVisitor() {
        return literalVisitor;
    }

    MatchCaseVisitor getMatchCaseVisitor() {
        return matchCaseVisitor;
    }

    PatternVisitor getPatternVisitor() {
        return patternVisitor;
    }

    FieldPatternVisitor getFieldPatternVisitor() {
        return fieldPatternVisitor;
    }

    QualifiedIdVisitor getQualifiedIdVisitor() {
        return qualifiedIdVisitor;
    }

    public NamespaceNode<Void> parse(String source) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream);
    }

    public NamespaceNode<Void> parse(CharStream charStream) {
        return parse(charStream, Parser::getNamespaceVisitor, MinaParser::namespace);
    }

    <A extends ParserRuleContext, B extends SyntaxNode, C extends Visitor<A, B>> B parse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, visitor, startRule);
    }

    <A extends ParserRuleContext, B extends SyntaxNode, C extends Visitor<A, B>> B parse(
            CharStream charStream,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule) {
        var lexer = new MinaLexer(charStream, lexerDFA, lexerCache);
        lexer.removeErrorListeners();
        lexer.addErrorListener(diagnostics);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream, parserDFA, parserCache);

        // Try parsing using SLL(*) first, as it's faster
        var interpreter = parser.getInterpreter();
        interpreter.setPredictionMode(PredictionMode.SLL);

        // Don't report diagnostics on the first pass
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());

        A syntaxNode;

        try {
            syntaxNode = startRule.apply(parser);
        } catch (ParseCancellationException e) {
            // Try again with LL(*) if it fails
            interpreter.setPredictionMode(PredictionMode.LL);
            tokenStream.seek(0);
            parser.reset();

            // Collect diagnostics on the second pass
            parser.addErrorListener(diagnostics);
            parser.setErrorHandler(new DefaultErrorStrategy());

            syntaxNode = startRule.apply(parser);
        }

        return visitor.apply(this).visitNullable(syntaxNode);
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

    abstract class Visitor<A extends ParserRuleContext, B> extends MinaParserBaseVisitor<B> {
        public B visitNullable(ParseTree tree) {
            return tree != null ? visit(tree) : null;
        }

        public <C extends ParserRuleContext, D> ImmutableList<D> visitRepeated(List<C> contexts,
                Visitor<C, D> visitor) {
            return contexts.stream()
                    .map(ctx -> visitor.visit(ctx))
                    .collect(Collectors2.toImmutableList());
        }

        public <C extends ParserRuleContext, D> ImmutableList<D> visitRepeated(List<C> contexts,
                Function<C, D> visitorMethod) {
            return contexts.stream()
                    .map(ctx -> visitorMethod.apply(ctx))
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

        public <C extends ParserRuleContext, D extends ParserRuleContext, E> ImmutableList<E> visitNullableRepeated(
                C context, Function<C, List<D>> rule,
                Function<D, E> visitorMethod) {
            return context == null ? Lists.immutable.<E>empty()
                    : rule.apply(context).stream()
                            .map(ctx -> visitorMethod.apply(ctx))
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

    class NamespaceVisitor extends Visitor<NamespaceContext, NamespaceNode<Void>> {

        @Override
        public NamespaceNode<Void> visitNamespace(NamespaceContext ctx) {
            var id = namespaceIdVisitor.visitNullable(ctx.namespaceId());

            var imports = visitRepeated(ctx.importDeclaration(), importVisitor);

            var declarations = visitRepeated(ctx.declaration(), declarationVisitor);

            return namespaceNode(contextRange(ctx), id, imports, declarations);
        }
    }

    class ImportVisitor extends Visitor<ImportDeclarationContext, ImportNode> {

        @Override
        public ImportNode visitImportDeclaration(ImportDeclarationContext ctx) {
            return visitAlternatives(ctx.importQualified(), ctx.importSymbols());
        }

        @Override
        public ImportNode visitImportQualified(ImportQualifiedContext ctx) {
            var ns = namespaceIdVisitor.visitNullable(ctx.namespaceId());
            var alias = Optional.ofNullable(ctx.alias).map(Token::getText);
            return importQualifiedNode(contextRange(ctx), ns, alias);
        }

        @Override
        public ImportNode visitImportSymbols(ImportSymbolsContext ctx) {
            var ns = namespaceIdVisitor.visitNullable(ctx.namespaceId());

            var singleImportee = Optional.ofNullable(ctx.id)
                .map(id -> importeeNode(tokenRange(id), id.getText()))
                .map(Lists.immutable::of);

            var multiImportees = Optional.ofNullable(ctx.importee())
                .map(importees -> visitRepeated(importees, importeeVisitor));

            return importSymbolsNode(
                contextRange(ctx), ns,
                singleImportee
                    .or(() -> multiImportees)
                    .orElseGet(Lists.immutable::empty));
        }
    }

    class ImporteeVisitor extends Visitor<ImporteeContext, ImporteeNode> {
        @Override
        public ImporteeNode visitImportee(ImporteeContext ctx) {
            var id = Optional.ofNullable(ctx.id).map(Token::getText).orElse(null);
            var alias = Optional.ofNullable(ctx.alias).map(Token::getText);
            return importeeNode(contextRange(ctx), id, alias);
        }
    }

    class DeclarationVisitor extends Visitor<DeclarationContext, DeclarationNode<Void>> {

        @Override
        public DataNode<Void> visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var typeParams = visitNullableRepeated(ctx.typeParams(), TypeParamsContext::typeVar,
                    typeVisitor::visitTypeVar);
            var constructors = visitRepeated(ctx.dataConstructor(), constructorVisitor);
            return dataNode(contextRange(ctx), name, typeParams, constructors);
        }

        @Override
        public LetFnNode<Void> visitLetFnDeclaration(LetFnDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var typeParams = visitNullableRepeated(ctx.typeParams(), TypeParamsContext::typeVar,
                    typeVisitor::visitTypeVar);
            var valueParams = visitNullableRepeated(ctx.lambdaParams(), LambdaParamsContext::lambdaParam,
                    paramVisitor::visitLambdaParam);
            var expr = exprVisitor.visitNullable(ctx.expr());
            var type = typeVisitor.visitNullable(ctx.typeAnnotation());
            return letFnNode(contextRange(ctx), name, typeParams, valueParams, type, expr);
        }

        @Override
        public LetNode<Void> visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var expr = exprVisitor.visitNullable(ctx.expr());
            var type = typeVisitor.visitNullable(ctx.typeAnnotation());
            return letNode(contextRange(ctx), name, type, expr);
        }

        @Override
        public DeclarationNode<Void> visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.dataDeclaration(), ctx.letFnDeclaration(), ctx.letDeclaration());
        }
    }

    class ConstructorVisitor extends Visitor<DataConstructorContext, ConstructorNode<Void>> {

        @Override
        public ConstructorNode<Void> visitDataConstructor(DataConstructorContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var params = visitNullableRepeated(ctx.constructorParams(), ConstructorParamsContext::constructorParam,
                    constructorParamVisitor::visitConstructorParam);
            var type = Optional.ofNullable(ctx.typeAnnotation())
                    .map(TypeAnnotationContext::type)
                    .map(typeVisitor::visitNullable);
            return constructorNode(contextRange(ctx), name, params, type);
        }
    }

    class ConstructorParamVisitor extends Visitor<ConstructorParamContext, ConstructorParamNode<Void>> {

        @Override
        public ConstructorParamNode<Void> visitConstructorParam(ConstructorParamContext ctx) {
            var id = Optional.ofNullable(ctx.ID());
            var name = id.map(TerminalNode::getText).orElse(null);
            var type = Optional.ofNullable(ctx.typeAnnotation())
                    .map(TypeAnnotationContext::type)
                    .map(typeVisitor::visitNullable)
                    .orElse(null);
            return constructorParamNode(contextRange(ctx), name, type);
        }
    }

    class TypeVisitor extends Visitor<TypeContext, TypeNode<Void>> {

        @Override
        public TypeNode<Void> visitType(TypeContext ctx) {
            return visitAlternatives(ctx.typeLambda(), ctx.funType(), ctx.applicableType());
        }

        @Override
        public TypeLambdaNode<Void> visitTypeLambda(TypeLambdaContext ctx) {
            var singleParam = Optional.ofNullable(ctx.typeVar())
                    .map(this::visitTypeVar)
                    .map(Lists.immutable::of);

            var typeParams = singleParam.orElse(
                    visitNullableRepeated(ctx.typeParams(), TypeParamsContext::typeVar, this::visitTypeVar));

            var bodyNode = visitNullable(ctx.type());

            return typeLambdaNode(contextRange(ctx), typeParams, bodyNode);
        }

        @Override
        public FunTypeNode<Void> visitFunType(FunTypeContext ctx) {
            var paramTypeNode = Optional.ofNullable(ctx.applicableType())
                    .map(this::visitApplicableType)
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
                var args = ctx.typeApplication().type().stream()
                        .<TypeNode<Void>>map(this::visitType)
                        .collect(Collectors2.toImmutableList());

                return typeApplyNode(contextRange(ctx), applicableTypeNode, args);
            }

            return null;
        }

        @Override
        public TypeNode<Void> visitParenType(ParenTypeContext ctx) {
            return visitNullable(ctx.type());
        }

        @Override
        public TypeReferenceNode<Void> visitTypeReference(TypeReferenceContext ctx) {
            var qualifiedIdNode = Optional.ofNullable(qualifiedIdVisitor.visitNullable(ctx.qualifiedId()));

            var varNode = Optional.ofNullable(ctx.typeVar()).flatMap(tv -> {
                return Optional.ofNullable(visitTypeVar(tv))
                        .map(tvNode -> idNode(contextRange(tv), tvNode.name()));
            });

            var idNode = qualifiedIdNode.or(() -> varNode).orElse(null);

            return typeRefNode(contextRange(ctx), idNode);
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
            return visitAlternatives(ctx.blockExpr(), ctx.ifExpr(), ctx.lambdaExpr(), ctx.matchExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public BlockNode<Void> visitBlockExpr(BlockExprContext ctx) {
            var declarationNodes = visitRepeated(ctx.letDeclaration(), declarationVisitor::visitLetDeclaration);
            var resultNode = Optional.ofNullable(visitNullable(ctx.expr()));
            return blockNode(contextRange(ctx), declarationNodes, resultNode);
        }

        @Override
        public IfNode<Void> visitIfExpr(IfExprContext ctx) {
            var conditionNode = visitNullable(ctx.expr(0));
            var consequentNode = visitNullable(ctx.expr(1));
            var alternativeNode = visitNullable(ctx.expr(2));
            return ifNode(contextRange(ctx), conditionNode, consequentNode, alternativeNode);
        }

        @Override
        public LambdaNode<Void> visitLambdaExpr(LambdaExprContext ctx) {
            var singleParam = Optional.ofNullable(ctx.ID())
                    .map(id -> {
                        var token = id.getSymbol();
                        return paramNode(tokenRange(token), id.getText());
                    })
                    .map(Lists.immutable::of);

            var params = singleParam.orElse(
                    visitNullableRepeated(ctx.lambdaParams(), LambdaParamsContext::lambdaParam,
                            paramVisitor::visitLambdaParam));

            var bodyNode = visitNullable(ctx.expr());

            return lambdaNode(contextRange(ctx), params, bodyNode);
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
            return visitNullable(ctx.expr());
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

    class ParamVisitor extends Visitor<LambdaParamContext, ParamNode<Void>> {

        @Override
        public ParamNode<Void> visitLambdaParam(LambdaParamContext ctx) {
            var id = Optional.ofNullable(ctx.ID());
            var name = id.map(TerminalNode::getText).orElse(null);
            var type = typeVisitor.visitNullable(ctx.typeAnnotation());
            return paramNode(contextRange(ctx), name, type);
        }
    }

    class LiteralVisitor extends Visitor<LiteralContext, LiteralNode<Void>> {

        private IntNode<Void> safeIntNode(Range range, String withoutSuffix) {
            var decimalValue = new BigInteger(withoutSuffix);
            try {
                var intValue = decimalValue.intValueExact();
                return intNode(range, intValue);
            } catch (ArithmeticException exc) {
                diagnostics.reportWarning(range, "Integer overflow detected");
                return intNode(range, decimalValue.intValue());
            }
        }

        private LongNode<Void> safeLongNode(Range range, String withoutSuffix) {
            var decimalValue = new BigInteger(withoutSuffix);
            try {
                var longValue = decimalValue.longValueExact();
                return longNode(range, longValue);
            } catch (ArithmeticException exc) {
                diagnostics.reportWarning(range, "Long overflow detected");
                return longNode(range, decimalValue.longValue());
            }
        }

        private FloatNode<Void> safeFloatNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var floatValue = decimalValue.floatValue();

            var outOfRange = Float.isNaN(floatValue) || Float.isInfinite(floatValue);
            var notExact = new BigDecimal(String.valueOf(floatValue)).compareTo(decimalValue) != 0;
            if (outOfRange || notExact) {
                diagnostics.reportWarning(range, "Float precision loss detected");
            }

            return floatNode(range, floatValue);
        }

        private DoubleNode<Void> safeDoubleNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var doubleValue = decimalValue.doubleValue();

            var outOfRange = Double.isNaN(doubleValue) || Double.isInfinite(doubleValue);
            var notExact = new BigDecimal(String.valueOf(doubleValue)).compareTo(decimalValue) != 0;
            if (outOfRange || notExact) {
                diagnostics.reportWarning(range, "Double precision loss detected");
            }

            return doubleNode(range, doubleValue);
        }

        @Override
        public LiteralNode<Void> visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(), ctx.literalInt(),
                    ctx.literalFloat());
        }

        @Override
        public BooleanNode<Void> visitLiteralBoolean(LiteralBooleanContext ctx) {
            if (ctx.TRUE() != null) {
                return boolNode(contextRange(ctx), true);
            }

            if (ctx.FALSE() != null) {
                return boolNode(contextRange(ctx), false);
            }

            return null;
        }

        @Override
        public CharNode<Void> visitLiteralChar(LiteralCharContext ctx) {
            var charExpr = ctx.LITERAL_CHAR();
            var charValue = StringEscapeUtils.unescapeJava(charExpr.getText()).charAt(1);
            return charNode(contextRange(ctx), charValue);
        }

        @Override
        public StringNode<Void> visitLiteralString(LiteralStringContext ctx) {
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
                return safeLongNode(contextRange(ctx), withoutSuffix);
            } else if (intText.endsWith("i") || intText.endsWith("I")) {
                var withoutSuffix = intText.substring(0, intText.length() - 1);
                return safeIntNode(contextRange(ctx), withoutSuffix);
            } else {
                return safeIntNode(contextRange(ctx), intText);
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
            return visitAlternatives(
                    ctx.aliasPattern(), ctx.idPattern(),
                    ctx.literalPattern(), ctx.constructorPattern());
        }

        @Override
        public AliasPatternNode<Void> visitAliasPattern(AliasPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.ID())
                    .map(TerminalNode::getText)
                    .orElse(null);

            var pattern = visitNullable(ctx.pattern());

            return aliasPatternNode(contextRange(ctx), alias, pattern);
        }

        @Override
        public IdPatternNode<Void> visitIdPattern(IdPatternContext ctx) {
            var id = Optional.ofNullable(ctx.ID())
                    .map(TerminalNode::getText)
                    .orElse(null);

            return idPatternNode(contextRange(ctx), id);
        }

        @Override
        public LiteralPatternNode<Void> visitLiteralPattern(LiteralPatternContext ctx) {
            var literal = literalVisitor.visit(ctx.literal());
            return literalPatternNode(contextRange(ctx), literal);
        }

        @Override
        public ConstructorPatternNode<Void> visitConstructorPattern(ConstructorPatternContext ctx) {
            var id = qualifiedIdVisitor.visitNullable(ctx.qualifiedId());

            var fields = visitNullableRepeated(ctx.fieldPatterns(), FieldPatternsContext::fieldPattern,
                    fieldPatternVisitor);

            return constructorPatternNode(contextRange(ctx), id, fields);
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

    class NamespaceIdVisitor extends Visitor<NamespaceIdContext, NamespaceIdNode> {

        @Override
        public NamespaceIdNode visitNamespaceId(NamespaceIdContext ctx) {
            var pkg = Lists.immutable.ofAll(ctx.pkg).collect(Token::getText);
            var ns = Optional.ofNullable(ctx.ns).map(Token::getText).orElse(null);
            return nsIdNode(contextRange(ctx), pkg, ns);
        }
    }

    class QualifiedIdVisitor extends Visitor<QualifiedIdContext, QualifiedIdNode> {

        @Override
        public QualifiedIdNode visitQualifiedId(QualifiedIdContext ctx) {
            var ns = Optional.ofNullable(ctx.ns).map(token -> {
                return nsIdNode(tokenRange(token), Lists.immutable.empty(), token.getText());
            });
            var id = Optional.ofNullable(ctx.id).map(Token::getText).orElse(null);
            return idNode(contextRange(ctx), ns, id);
        }
    }
}
