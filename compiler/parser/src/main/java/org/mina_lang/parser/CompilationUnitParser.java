package org.mina_lang.parser;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.mina_lang.common.Position;
import org.mina_lang.common.Range;
import org.mina_lang.parser.MinaParser.ApplicableExprContext;
import org.mina_lang.parser.MinaParser.CompilationUnitContext;
import org.mina_lang.parser.MinaParser.ConstructorPatternContext;
import org.mina_lang.parser.MinaParser.DataDeclarationContext;
import org.mina_lang.parser.MinaParser.DeclarationContext;
import org.mina_lang.parser.MinaParser.ExprContext;
import org.mina_lang.parser.MinaParser.FieldPatternContext;
import org.mina_lang.parser.MinaParser.IfExprContext;
import org.mina_lang.parser.MinaParser.ImportDeclarationContext;
import org.mina_lang.parser.MinaParser.ImportSelectorContext;
import org.mina_lang.parser.MinaParser.LambdaExprContext;
import org.mina_lang.parser.MinaParser.LetDeclarationContext;
import org.mina_lang.parser.MinaParser.LiteralBooleanContext;
import org.mina_lang.parser.MinaParser.LiteralCharContext;
import org.mina_lang.parser.MinaParser.LiteralContext;
import org.mina_lang.parser.MinaParser.LiteralIntContext;
import org.mina_lang.parser.MinaParser.MatchCaseContext;
import org.mina_lang.parser.MinaParser.MatchExprContext;
import org.mina_lang.parser.MinaParser.ModuleContext;
import org.mina_lang.parser.MinaParser.ModuleIdContext;
import org.mina_lang.parser.MinaParser.ParenExprContext;
import org.mina_lang.parser.MinaParser.PatternAliasContext;
import org.mina_lang.parser.MinaParser.PatternContext;
import org.mina_lang.parser.MinaParser.QualifiedIdContext;
import org.mina_lang.syntax.ApplyNode;
import org.mina_lang.syntax.CaseNode;
import org.mina_lang.syntax.CompilationUnitNode;
import org.mina_lang.syntax.ConstructorPatternNode;
import org.mina_lang.syntax.DataDeclarationNode;
import org.mina_lang.syntax.DeclarationNode;
import org.mina_lang.syntax.ExprNode;
import org.mina_lang.syntax.FieldPatternNode;
import org.mina_lang.syntax.IdPatternNode;
import org.mina_lang.syntax.IfExprNode;
import org.mina_lang.syntax.ImportNode;
import org.mina_lang.syntax.LambdaExprNode;
import org.mina_lang.syntax.LetDeclarationNode;
import org.mina_lang.syntax.LiteralBooleanNode;
import org.mina_lang.syntax.LiteralCharNode;
import org.mina_lang.syntax.LiteralIntNode;
import org.mina_lang.syntax.MatchNode;
import org.mina_lang.syntax.Meta;
import org.mina_lang.syntax.ModuleNode;
import org.mina_lang.syntax.ParamNode;
import org.mina_lang.syntax.PatternNode;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.ReferenceNode;
import org.mina_lang.syntax.SyntaxNode;

public class CompilationUnitParser {

    public static CompilationUnitNode<Void> parse(String source, ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, errorListener);
    }

    public static CompilationUnitNode<Void> parse(CharStream charStream, ANTLRErrorListener errorListener) {
        return parse(charStream, errorListener, CompilationUnitVisitor.INSTANCE, MinaParser::compilationUnit);
    }

    static <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
            String source,
            ANTLRErrorListener errorListener,
            C visitor,
            Function<MinaParser, A> startRule) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, errorListener, visitor, startRule);
    }

    static <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> B parse(
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

        public B visitAlternatives(ParseTree... tree) {
            return Stream.of(tree)
                    .filter(t -> t != null)
                    .findFirst()
                    .map(t -> visit(t))
                    .orElse(null);
        }
    }

    static class CompilationUnitVisitor extends Visitor<CompilationUnitContext, CompilationUnitNode<Void>> {

        public static CompilationUnitVisitor INSTANCE = new CompilationUnitVisitor(ModuleVisitor.INSTANCE);

        private ModuleVisitor moduleVisitor;

        public CompilationUnitVisitor(ModuleVisitor moduleVisitor) {
            this.moduleVisitor = moduleVisitor;
        }

        @Override
        public CompilationUnitNode<Void> visitCompilationUnit(CompilationUnitContext ctx) {
            var modules = visitRepeated(ctx.module(), moduleVisitor);
            var meta = Meta.empty(contextRange(ctx));
            var node = new CompilationUnitNode<Void>(meta, modules);
            return node;
        }
    }

    static class ModuleVisitor extends Visitor<ModuleContext, ModuleNode<Void>> {

        public static ModuleVisitor INSTANCE = new ModuleVisitor(ImportVisitor.INSTANCE, DeclarationVisitor.INSTANCE);

        private ImportVisitor importVisitor;
        private DeclarationVisitor declarationVisitor;

        public ModuleVisitor(ImportVisitor importVisitor, DeclarationVisitor declarationVisitor) {
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

            var imports = visitRepeated(ctx.importDeclaration(), importVisitor);

            var declarations = visitRepeated(ctx.declaration(), declarationVisitor);

            var meta = Meta.empty(contextRange(ctx));
            var node = new ModuleNode<Void>(meta, pkg, mod, imports, declarations);

            return node;
        }
    }

    static class ImportVisitor extends Visitor<ImportDeclarationContext, ImportNode<Void>> {

        public static ImportVisitor INSTANCE = new ImportVisitor();

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

            var meta = Meta.empty(contextRange(ctx));
            var node = new ImportNode<Void>(meta, pkg, mod, symbols);

            return node;
        }
    }

    static class DeclarationVisitor extends Visitor<DeclarationContext, DeclarationNode<Void>> {

        public static DeclarationVisitor INSTANCE = new DeclarationVisitor(ExprVisitor.INSTANCE);

        private ExprVisitor exprVisitor;

        public DeclarationVisitor(ExprVisitor exprVisitor) {
            this.exprVisitor = exprVisitor;
        }

        @Override
        public DeclarationNode<Void> visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var meta = Meta.empty(contextRange(ctx));
            var node = new DataDeclarationNode<Void>(meta, name);
            return node;
        }

        @Override
        public DeclarationNode<Void> visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var expr = exprVisitor.visitNullable(ctx.expr());
            var meta = Meta.empty(contextRange(ctx));
            var node = new LetDeclarationNode<Void>(meta, name, expr);
            return node;
        }

        @Override
        public DeclarationNode<Void> visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.dataDeclaration(), ctx.letDeclaration());
        }
    }

    static class ExprVisitor extends Visitor<ExprContext, ExprNode<Void>> {

        public static ExprVisitor INSTANCE = new ExprVisitor(MatchCaseVisitor.INSTANCE, QualifiedIdVisitor.INSTANCE);

        private MatchCaseVisitor matchCaseVisitor;

        private QualifiedIdVisitor qualifiedIdVisitor;

        public ExprVisitor(MatchCaseVisitor matchCaseVisitor, QualifiedIdVisitor qualifiedIdVisitor) {
            this.matchCaseVisitor = matchCaseVisitor;
            this.qualifiedIdVisitor = qualifiedIdVisitor;
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
            var meta = Meta.empty(contextRange(ctx));
            var node = new IfExprNode<Void>(meta, conditionNode, consequentNode, alternativeNode);
            return node;
        }

        @Override
        public ExprNode<Void> visitLambdaExpr(LambdaExprContext ctx) {
            var params = ctx.lambdaParams().ID().stream()
                    .map(param -> {
                        var token = param.getSymbol();
                        var meta = Meta.empty(tokenRange(token));
                        return new ParamNode<Void>(meta, param.getText());
                    })
                    .collect(Collectors2.toImmutableList());

            var bodyNode = visitNullable(ctx.expr());

            var meta = Meta.empty(contextRange(ctx));
            var node = new LambdaExprNode<Void>(meta, params, bodyNode);
            return node;
        }

        @Override
        public ExprNode<Void> visitMatchExpr(MatchExprContext ctx) {
            var scrutineeNode = visitNullable(ctx.expr());
            var cases = visitRepeated(ctx.matchCase(), matchCaseVisitor);
            var meta = Meta.empty(contextRange(ctx));
            var node = new MatchNode<Void>(meta, scrutineeNode, cases);
            return node;
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
                var meta = Meta.empty(contextRange(ctx));
                var node = new ApplyNode<Void>(meta, applicableExprNode, args);
                return node;
            }

            return null;
        }

        @Override
        public ExprNode<Void> visitParenExpr(ParenExprContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public ExprNode<Void> visitQualifiedId(QualifiedIdContext ctx) {
            var meta = Meta.empty(contextRange(ctx));
            var node = new ReferenceNode<Void>(meta, qualifiedIdVisitor.visitNullable(ctx));
            return node;
        }

        @Override
        public ExprNode<Void> visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalInt());
        }

        @Override
        public ExprNode<Void> visitLiteralBoolean(LiteralBooleanContext ctx) {
            var trueExpr = ctx.TRUE();
            var meta = Meta.empty(contextRange(ctx));

            if (trueExpr != null) {
                var node = new LiteralBooleanNode<Void>(meta, true);
                return node;
            }

            var falseExpr = ctx.FALSE();
            if (falseExpr != null) {
                var node = new LiteralBooleanNode<Void>(meta, false);
                return node;
            }

            return null;
        }

        @Override
        public ExprNode<Void> visitLiteralChar(LiteralCharContext ctx) {
            var charExpr = ctx.LITERAL_CHAR();
            var charValue = charExpr.getText().charAt(1);
            var meta = Meta.empty(contextRange(ctx));
            var node = new LiteralCharNode<Void>(meta, charValue);
            return node;
        }

        @Override
        public ExprNode<Void> visitLiteralInt(LiteralIntContext ctx) {
            var intExpr = ctx.LITERAL_INT();
            var meta = Meta.empty(contextRange(ctx));
            var node = new LiteralIntNode<Void>(meta, Integer.parseInt(intExpr.getText()));
            return node;
        }

    }

    static class MatchCaseVisitor extends Visitor<MatchCaseContext, CaseNode<Void>> {

        public static MatchCaseVisitor INSTANCE = new MatchCaseVisitor(ExprVisitor.INSTANCE, PatternVisitor.INSTANCE);

        private ExprVisitor exprVisitor;
        private PatternVisitor patternVisitor;

        public MatchCaseVisitor(ExprVisitor exprVisitor, PatternVisitor patternVisitor) {
            this.exprVisitor = exprVisitor;
            this.patternVisitor = patternVisitor;
        }

        @Override
        public CaseNode<Void> visitMatchCase(MatchCaseContext ctx) {
            var patternNode = patternVisitor.visitNullable(ctx.pattern());
            var consequentNode = exprVisitor.visitNullable(ctx.expr());
            var meta = Meta.empty(contextRange(ctx));
            var node = new CaseNode<Void>(meta, patternNode, consequentNode);
            return node;
        }
    }

    static class PatternVisitor extends Visitor<PatternContext, PatternNode<Void>> {

        public static PatternVisitor INSTANCE = new PatternVisitor(QualifiedIdVisitor.INSTANCE,
                FieldPatternVisitor.INSTANCE);

        private QualifiedIdVisitor qualifiedIdVisitor;
        private FieldPatternVisitor fieldPatternVisitor;

        public PatternVisitor(QualifiedIdVisitor qualifiedIdVisitor, FieldPatternVisitor fieldPatternVisitor) {
            this.qualifiedIdVisitor = qualifiedIdVisitor;
            this.fieldPatternVisitor = fieldPatternVisitor;
        }

        @Override
        public PatternNode<Void> visitPattern(PatternContext ctx) {
            var id = ctx.ID();
            if (id != null) {
                var meta = Meta.empty(contextRange(ctx));
                var node = new IdPatternNode<Void>(meta, id.getText());
                return node;
            }

            return visitNullable(ctx.constructorPattern());
        }

        @Override
        public PatternNode<Void> visitConstructorPattern(ConstructorPatternContext ctx) {
            var id = qualifiedIdVisitor.visitNullable(ctx.qualifiedId());

            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var fieldPatterns = ctx.fieldPatterns();
            var fields = Lists.immutable.<FieldPatternNode<Void>>empty();
            if (fieldPatterns != null) {
                fields = visitRepeated(fieldPatterns.fieldPattern(), fieldPatternVisitor);
            }

            var meta = Meta.empty(contextRange(ctx));
            var node = new ConstructorPatternNode<Void>(meta, id, alias, fields);

            return node;
        }

    }

    static class FieldPatternVisitor extends Visitor<FieldPatternContext, FieldPatternNode<Void>> {

        public static FieldPatternVisitor INSTANCE = new FieldPatternVisitor(PatternVisitor.INSTANCE);

        private PatternVisitor patternVisitor;

        public FieldPatternVisitor(PatternVisitor patternVisitor) {
            this.patternVisitor = patternVisitor;
        }

        @Override
        public FieldPatternNode<Void> visitFieldPattern(FieldPatternContext ctx) {
            var id = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var pattern = patternVisitor.visitNullable(ctx.pattern());
            var meta = Meta.empty(contextRange(ctx));
            var node = new FieldPatternNode<Void>(meta, id, pattern);
            return node;
        }
    }

    static class QualifiedIdVisitor extends Visitor<QualifiedIdContext, QualifiedIdNode<Void>> {

        public static QualifiedIdVisitor INSTANCE = new QualifiedIdVisitor();

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

            var meta = Meta.empty(contextRange(ctx));
            var node = new QualifiedIdNode<Void>(meta, pkg, id);

            return node;
        }

    }
}
