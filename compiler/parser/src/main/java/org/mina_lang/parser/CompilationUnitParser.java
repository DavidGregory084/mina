package org.mina_lang.parser;

import com.google.inject.Provider;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class CompilationUnitParser {

    private Provider<CompilationUnitVisitor> compilationUnitVisitor;

    @Inject
    public CompilationUnitParser(Provider<CompilationUnitVisitor> compilationUnitVisitor) {
        this.compilationUnitVisitor = compilationUnitVisitor;
    }

    public CompilationUnitNode<Void> parse(String source, ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, errorListener);
    }

    public CompilationUnitNode<Void> parse(CharStream charStream, ANTLRErrorListener errorListener) {
        return parse(charStream, errorListener, compilationUnitVisitor.get(),
                MinaParser::compilationUnit);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            String source,
            ANTLRErrorListener errorListener,
            C visitor,
            Function<MinaParser, A> startRule) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, errorListener, visitor, startRule);
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            CharStream charStream,
            ANTLRErrorListener errorListener,
            C visitor,
            Function<MinaParser, A> startRule) {
        var lexer = new MinaLexer(charStream);
        lexer.addErrorListener(errorListener);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream);
        parser.addErrorListener(errorListener);
        return visitor.visitNullable(startRule.apply(parser));
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

    static abstract class Visitor<A extends ParserRuleContext, B> extends MinaBaseVisitor<B> {
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

    @Singleton
    static class CompilationUnitVisitor extends Visitor<CompilationUnitContext, CompilationUnitNode<Void>> {

        private Provider<ModuleVisitor> moduleVisitor;

        @Inject
        public CompilationUnitVisitor(Provider<ModuleVisitor> moduleVisitor) {
            this.moduleVisitor = moduleVisitor;
        }

        @Override
        public CompilationUnitNode<Void> visitCompilationUnit(CompilationUnitContext ctx) {
            var modules = visitRepeated(ctx.module(), moduleVisitor.get());
            return compilationUnitNode(contextRange(ctx), modules);
        }
    }

    @Singleton
    static class ModuleVisitor extends Visitor<ModuleContext, ModuleNode<Void>> {

        private Provider<ImportVisitor> importVisitor;
        private Provider<DeclarationVisitor> declarationVisitor;

        @Inject
        public ModuleVisitor(Provider<ImportVisitor> importVisitor, Provider<DeclarationVisitor> declarationVisitor) {
            this.importVisitor = importVisitor;
            this.declarationVisitor = declarationVisitor;
        }

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

            var imports = visitRepeated(ctx.importDeclaration(), importVisitor.get());

            var declarations = visitRepeated(ctx.declaration(), declarationVisitor.get());

            return moduleNode(contextRange(ctx), pkg, mod, imports, declarations);
        }
    }

    @Singleton
    static class ImportVisitor extends Visitor<ImportDeclarationContext, ImportNode<Void>> {

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

    @Singleton
    static class DeclarationVisitor extends Visitor<DeclarationContext, DeclarationNode<Void>> {

        private Provider<ExprVisitor> exprVisitor;

        @Inject
        public DeclarationVisitor(Provider<ExprVisitor> exprVisitor) {
            this.exprVisitor = exprVisitor;
        }

        @Override
        public DeclarationNode<Void> visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            return dataDeclarationNode(contextRange(ctx), name);
        }

        @Override
        public DeclarationNode<Void> visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var expr = exprVisitor.get().visitNullable(ctx.expr());
            return letDeclarationNode(contextRange(ctx), name, expr);
        }

        @Override
        public DeclarationNode<Void> visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.dataDeclaration(), ctx.letDeclaration());
        }
    }

    @Singleton
    static class ExprVisitor extends Visitor<ExprContext, ExprNode<Void>> {

        private Provider<MatchCaseVisitor> matchCaseVisitor;
        private Provider<QualifiedIdVisitor> qualifiedIdVisitor;
        private Provider<LiteralVisitor> literalVisitor;

        @Inject
        public ExprVisitor(Provider<MatchCaseVisitor> matchCaseVisitor, Provider<QualifiedIdVisitor> qualifiedIdVisitor,
                Provider<LiteralVisitor> literalVisitor) {
            this.matchCaseVisitor = matchCaseVisitor;
            this.qualifiedIdVisitor = qualifiedIdVisitor;
            this.literalVisitor = literalVisitor;
        }

        @Override
        public ExprNode<Void> visitExpr(ExprContext ctx) {
            return visitAlternatives(ctx.ifExpr(), ctx.lambdaExpr(), ctx.matchExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public ExprNode<Void> visitIfExpr(IfExprContext ctx) {
            var conditionNode = visitNullable(ctx.expr(0));
            var consequentNode = visitNullable(ctx.expr(1));
            var alternativeNode = visitNullable(ctx.expr(2));
            return ifExprNode(contextRange(ctx), conditionNode, consequentNode, alternativeNode);
        }

        @Override
        public ExprNode<Void> visitLambdaExpr(LambdaExprContext ctx) {
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
        public ExprNode<Void> visitMatchExpr(MatchExprContext ctx) {
            var scrutineeNode = visitNullable(ctx.expr());
            var cases = visitRepeated(ctx.matchCase(), matchCaseVisitor.get());
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
                var args = visitRepeated(ctx.application().expr(), this);
                return applyNode(contextRange(ctx), applicableExprNode, args);
            }

            return null;
        }

        @Override
        public ExprNode<Void> visitParenExpr(ParenExprContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public ExprNode<Void> visitQualifiedId(QualifiedIdContext ctx) {
            return refNode(contextRange(ctx), qualifiedIdVisitor.get().visitNullable(ctx));
        }

        @Override
        public ExprNode<Void> visitLiteral(LiteralContext ctx) {
            return literalVisitor.get().visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(),
                    ctx.literalInt(), ctx.literalFloat());
        }
    }

    @Singleton
    static class LiteralVisitor extends Visitor<LiteralContext, LiteralNode<Void>> {

        @Override
        public LiteralNode<Void> visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(), ctx.literalInt(),
                    ctx.literalFloat());
        }

        @Override
        public LiteralNode<Void> visitLiteralBoolean(LiteralBooleanContext ctx) {
            if (ctx.TRUE() != null) {
                return boolNode(contextRange(ctx), true);
            }

            if (ctx.FALSE() != null) {
                return boolNode(contextRange(ctx), false);
            }

            return null;
        }

        @Override
        public LiteralNode<Void> visitLiteralChar(LiteralCharContext ctx) {
            var charExpr = ctx.LITERAL_CHAR();
            var charValue = StringEscapeUtils.unescapeJava(charExpr.getText()).charAt(1);
            return charNode(contextRange(ctx), charValue);
        }

        @Override
        public LiteralNode<Void> visitLiteralString(LiteralStringContext ctx) {
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
                return longNode(contextRange(ctx), Long.parseLong(withoutSuffix));
            } else if (intText.endsWith("i") || intText.endsWith("I")) {
                var withoutSuffix = intText.substring(0, intText.length() - 1);
                return intNode(contextRange(ctx), Integer.parseInt(withoutSuffix));
            } else {
                return intNode(contextRange(ctx), Integer.parseInt(intText));
            }
        }

        @Override
        public LiteralNode<Void> visitLiteralFloat(LiteralFloatContext ctx) {
            var floatExpr = ctx.LITERAL_FLOAT();
            var floatText = floatExpr.getText().replace("_", "");
            if (floatText.endsWith("d") || floatText.endsWith("D")) {
                var withoutSuffix = floatText.substring(0, floatText.length() - 1);
                return doubleNode(contextRange(ctx), Double.parseDouble(withoutSuffix));
            } else if (floatText.endsWith("f") || floatText.endsWith("F")) {
                var withoutSuffix = floatText.substring(0, floatText.length() - 1);
                return floatNode(contextRange(ctx), Float.parseFloat(withoutSuffix));
            } else {
                return doubleNode(contextRange(ctx), Double.parseDouble(floatText));
            }
        }
    }

    @Singleton
    static class MatchCaseVisitor extends Visitor<MatchCaseContext, CaseNode<Void>> {

        private Provider<ExprVisitor> exprVisitor;
        private Provider<PatternVisitor> patternVisitor;

        @Inject
        public MatchCaseVisitor(Provider<ExprVisitor> exprVisitor, Provider<PatternVisitor> patternVisitor) {
            this.exprVisitor = exprVisitor;
            this.patternVisitor = patternVisitor;
        }

        @Override
        public CaseNode<Void> visitMatchCase(MatchCaseContext ctx) {
            var patternNode = patternVisitor.get().visitNullable(ctx.pattern());
            var consequentNode = exprVisitor.get().visitNullable(ctx.expr());
            return caseNode(contextRange(ctx), patternNode, consequentNode);
        }
    }

    @Singleton
    static class PatternVisitor extends Visitor<PatternContext, PatternNode<Void>> {

        private QualifiedIdVisitor qualifiedIdVisitor;
        private FieldPatternVisitor fieldPatternVisitor;
        private LiteralVisitor literalVisitor;

        @Inject
        public PatternVisitor(FieldPatternVisitor fieldPatternVisitor, QualifiedIdVisitor qualifiedIdVisitor,
                LiteralVisitor literalVisitor) {
            this.fieldPatternVisitor = fieldPatternVisitor;
            this.qualifiedIdVisitor = qualifiedIdVisitor;
            this.literalVisitor = literalVisitor;
        }

        @Override
        public PatternNode<Void> visitPattern(PatternContext ctx) {
            return visitAlternatives(ctx.idPattern(), ctx.literalPattern(), ctx.constructorPattern());
        }

        @Override
        public PatternNode<Void> visitIdPattern(IdPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var id = Optional.ofNullable(ctx.ID())
                    .map(TerminalNode::getText)
                    .orElse(null);

            return idPatternNode(contextRange(ctx), alias, id);
        }

        @Override
        public PatternNode<Void> visitLiteralPattern(LiteralPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var literal = literalVisitor.visit(ctx.literal());

            return literalPatternNode(contextRange(ctx), alias, literal);
        }

        @Override
        public PatternNode<Void> visitConstructorPattern(ConstructorPatternContext ctx) {
            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var id = qualifiedIdVisitor.visitNullable(ctx.qualifiedId());

            var fields = visitNullableRepeated(ctx.fieldPatterns(), FieldPatternsContext::fieldPattern,
                    fieldPatternVisitor);

            return constructorPatternNode(contextRange(ctx), alias, id, fields);
        }
    }

    @Singleton
    static class FieldPatternVisitor extends Visitor<FieldPatternContext, FieldPatternNode<Void>> {

        private Provider<PatternVisitor> patternVisitor;

        @Inject
        public FieldPatternVisitor(Provider<PatternVisitor> patternVisitor) {
            this.patternVisitor = patternVisitor;
        }

        @Override
        public FieldPatternNode<Void> visitFieldPattern(FieldPatternContext ctx) {
            var id = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var pattern = Optional.ofNullable(patternVisitor.get().visitNullable(ctx.pattern()));
            return fieldPatternNode(contextRange(ctx), id, pattern);
        }
    }

    @Singleton
    static class QualifiedIdVisitor extends Visitor<QualifiedIdContext, QualifiedIdNode<Void>> {

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
