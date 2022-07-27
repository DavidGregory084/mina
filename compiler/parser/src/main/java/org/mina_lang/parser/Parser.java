package org.mina_lang.parser;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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

public class Parser {

    private ANTLRDiagnosticCollector errorListener;

    private CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor();
    private ModuleIdVisitor moduleIdVisitor = new ModuleIdVisitor();
    private ModuleVisitor moduleVisitor = new ModuleVisitor();
    private ImportVisitor importVisitor = new ImportVisitor();
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

    public Parser(ANTLRDiagnosticCollector errorListener) {
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

    public ConstructorVisitor getConstructorVisitor() {
        return constructorVisitor;
    }

    public ConstructorParamVisitor getConstructorParamVisitor() {
        return constructorParamVisitor;
    }

    public TypeVisitor getTypeVisitor() {
        return typeVisitor;
    }

    public ExprVisitor getExprVisitor() {
        return exprVisitor;
    }

    public ParamVisitor getParamVisitor() {
        return paramVisitor;
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
        return parse(charStream, Parser::getCompilationUnitVisitor, MinaParser::compilationUnit);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, visitor, startRule);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            CharStream charStream,
            Function<Parser, C> visitor,
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
            var id = moduleIdVisitor.visitNullable(ctx.moduleId());

            var imports = visitRepeated(ctx.importDeclaration(), importVisitor);

            var declarations = visitRepeated(ctx.declaration(), declarationVisitor);

            return moduleNode(contextRange(ctx), id, imports, declarations);
        }
    }

    class ImportVisitor extends Visitor<ImportDeclarationContext, ImportNode<Void>> {

        @Override
        public ImportNode<Void> visitImportDeclaration(ImportDeclarationContext ctx) {
            var selector = Optional.ofNullable(ctx.importSelector());

            var mod = selector
                    .map(ImportSelectorContext::moduleId)
                    .map(moduleIdVisitor::visitNullable)
                    .orElse(null);

            var symbols = selector
                    .map(selectorCtx -> {
                        return selectorCtx.symbols.stream()
                                .map(Token::getText)
                                .collect(Collectors2.toImmutableList());
                    })
                    .orElse(Lists.immutable.empty());

            return importNode(contextRange(ctx), mod, symbols);
        }
    }

    class DeclarationVisitor extends Visitor<DeclarationContext, DeclarationNode<Void>> {

        @Override
        public DataDeclarationNode<Void> visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var typeParams = visitNullableRepeated(ctx.typeParams(), TypeParamsContext::typeVar,
                    typeVisitor::visitTypeVar);
            var constructors = visitRepeated(ctx.dataConstructor(), constructorVisitor);
            return dataDeclarationNode(contextRange(ctx), name, typeParams, constructors);
        }

        @Override
        public LetFnDeclarationNode<Void> visitLetFnDeclaration(LetFnDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var typeParams = visitNullableRepeated(ctx.typeParams(), TypeParamsContext::typeVar,
                    typeVisitor::visitTypeVar);
            var valueParams = visitNullableRepeated(ctx.lambdaParams(), LambdaParamsContext::lambdaParam,
                    paramVisitor::visitLambdaParam);
            var expr = exprVisitor.visitNullable(ctx.expr());
            var type = typeVisitor.visitNullable(ctx.typeAnnotation());
            return letFnDeclarationNode(contextRange(ctx), name, typeParams, valueParams, type, expr);
        }

        @Override
        public LetDeclarationNode<Void> visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var expr = exprVisitor.visitNullable(ctx.expr());
            var type = typeVisitor.visitNullable(ctx.typeAnnotation());
            return letDeclarationNode(contextRange(ctx), name, type, expr);
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
                var args = ctx.typeApplication().typeReference().stream()
                        .<TypeNode<Void>>map(ref -> visitTypeReference(ref))
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
            return visitAlternatives(ctx.blockExpr(), ctx.ifExpr(), ctx.lambdaExpr(), ctx.matchExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public BlockExprNode<Void> visitBlockExpr(BlockExprContext ctx) {
            var declarationNodes = visitRepeated(ctx.letDeclaration(), declarationVisitor::visitLetDeclaration);
            var resultNode = visitNullable(ctx.expr());
            return blockExprNode(contextRange(ctx), declarationNodes, resultNode);
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

        private LiteralIntNode<Void> safeIntNode(Range range, String withoutSuffix) {
            var decimalValue = new BigInteger(withoutSuffix);
            try {
                var intValue = decimalValue.intValueExact();
                return intNode(range, intValue);
            } catch (ArithmeticException exc) {
                errorListener.reportWarning(range, "Integer overflow detected");
                return intNode(range, decimalValue.intValue());
            }
        }

        private LiteralLongNode<Void> safeLongNode(Range range, String withoutSuffix) {
            var decimalValue = new BigInteger(withoutSuffix);
            try {
                var longValue = decimalValue.longValueExact();
                return longNode(range, longValue);
            } catch (ArithmeticException exc) {
                errorListener.reportWarning(range, "Long overflow detected");
                return longNode(range, decimalValue.longValue());
            }
        }

        private LiteralFloatNode<Void> safeFloatNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var floatValue = decimalValue.floatValue();

            var outOfRange = Float.isNaN(floatValue) || Float.isInfinite(floatValue);
            var notExact = new BigDecimal(String.valueOf(floatValue)).compareTo(decimalValue) != 0;
            if (outOfRange || notExact) {
                errorListener.reportWarning(range, "Float precision loss detected");
            }

            return floatNode(range, floatValue);
        }

        private LiteralDoubleNode<Void> safeDoubleNode(Range range, Token token, String withoutSuffix) {
            var decimalValue = new BigDecimal(withoutSuffix);
            var doubleValue = decimalValue.doubleValue();

            var outOfRange = Double.isNaN(doubleValue) || Double.isInfinite(doubleValue);
            var notExact = new BigDecimal(String.valueOf(doubleValue)).compareTo(decimalValue) != 0;
            if (outOfRange || notExact) {
                errorListener.reportWarning(range, "Double precision loss detected");
            }

            return doubleNode(range, doubleValue);
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

    class ModuleIdVisitor extends Visitor<ModuleIdContext, ModuleIdNode<Void>> {

        @Override
        public ModuleIdNode<Void> visitModuleId(ModuleIdContext ctx) {
            var pkg = ctx.pkg.stream()
                    .map(Token::getText)
                    .collect(Collectors2.toImmutableList());

            var mod = Optional.ofNullable(ctx.mod).map(Token::getText).orElse(null);

            return modIdNode(contextRange(ctx), pkg, mod);
        }
    }

    class QualifiedIdVisitor extends Visitor<QualifiedIdContext, QualifiedIdNode<Void>> {

        @Override
        public QualifiedIdNode<Void> visitQualifiedId(QualifiedIdContext ctx) {
            var mod = moduleIdVisitor.visitNullable(ctx.moduleId());
            var id = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            return idNode(contextRange(ctx), mod, id);
        }
    }
}