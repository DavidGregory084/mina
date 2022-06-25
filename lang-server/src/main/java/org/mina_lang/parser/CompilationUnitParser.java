package org.mina_lang.parser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.mina_lang.langserver.MinaSyntaxNodeCollector;
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
import org.mina_lang.parser.MinaParser.PackageIdContext;
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
import org.mina_lang.syntax.ModuleNode;
import org.mina_lang.syntax.ParamNode;
import org.mina_lang.syntax.PatternNode;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.ReferenceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilationUnitParser {
    private static Logger logger = LoggerFactory.getLogger(CompilationUnitParser.class);

    public static CompilationUnitNode parse(TextDocumentItem document, MinaSyntaxNodeCollector collector,
            ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(document.getText(), document.getUri());
        return parse(charStream, collector, errorListener);
    }

    public static CompilationUnitNode parse(String source, MinaSyntaxNodeCollector collector,
            ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, collector, errorListener);
    }

    public static CompilationUnitNode parse(CharStream charStream, MinaSyntaxNodeCollector collector,
            ANTLRErrorListener errorListener) {
        var lexer = new MinaLexer(charStream);
        lexer.addErrorListener(errorListener);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream);
        parser.addErrorListener(errorListener);
        var visitor = new CompilationUnitVisitor(collector);
        return visitor.visit(parser.compilationUnit());
    }

    public static Range contextRange(ParserRuleContext ctx) {
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

    private static abstract class Visitor<A> extends MinaBaseVisitor<A> {
        public A visitNullable(ParseTree tree) {
            return tree != null ? visit(tree) : null;
        }

        public <B> ImmutableList<B> visitRepeated(List<? extends ParserRuleContext> contexts, Visitor<B> visitor) {
            return contexts.stream()
                    .map(ctx -> visitor.visit(ctx))
                    .collect(Collectors2.toImmutableList());
        }

        public A visitAlternatives(ParseTree... tree) {
            return Stream.of(tree)
                    .filter(t -> t != null)
                    .findFirst()
                    .map(t -> visit(t))
                    .orElse(null);
        }
    }

    private static class CompilationUnitVisitor extends Visitor<CompilationUnitNode> {
        private MinaSyntaxNodeCollector collector;

        public CompilationUnitVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public CompilationUnitNode visitCompilationUnit(CompilationUnitContext ctx) {
            var moduleVisitor = new ModuleVisitor(collector);

            var modules = visitRepeated(ctx.module(), moduleVisitor);
            var node = new CompilationUnitNode(modules);

            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }
    }

    private static class ModuleVisitor extends Visitor<ModuleNode> {
        private MinaSyntaxNodeCollector collector;

        public ModuleVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public ModuleNode visitModule(ModuleContext ctx) {
            var modId = Optional.ofNullable(ctx.moduleId());
            var pkgId = modId.map(ModuleIdContext::packageId);

            var pkgSegments = pkgId
                    .map(PackageIdContext::ID)
                    .map(pkgName -> {
                        return pkgName.stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    }).orElse(Lists.immutable.empty());

            var modName = modId.map(ModuleIdContext::ID)
                    .map(TerminalNode::getText)
                    .orElse(null);

            var importVisitor = new ImportVisitor(collector);
            var imports = visitRepeated(ctx.importDeclaration(), importVisitor);

            var declarationVisitor = new DeclarationVisitor(collector);
            var declarations = visitRepeated(ctx.declaration(), declarationVisitor);

            var node = new ModuleNode(pkgSegments, modName, imports, declarations);

            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }
    }

    private static class ImportVisitor extends Visitor<ImportNode> {

        private MinaSyntaxNodeCollector collector;

        public ImportVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public ImportNode visitImportDeclaration(ImportDeclarationContext ctx) {
            var selector = Optional.ofNullable(ctx.importSelector());

            var modId = selector.map(ImportSelectorContext::moduleId);

            var modName = modId.map(ModuleIdContext::ID)
                    .map(TerminalNode::getText)
                    .orElse(null);

            var pkgId = modId.map(ModuleIdContext::packageId);

            var pkgSegments = pkgId
                    .map(PackageIdContext::ID)
                    .map(pkgName -> {
                        return pkgName.stream()
                                .map(TerminalNode::getText)
                                .collect(Collectors2.toImmutableList());
                    }).orElse(Lists.immutable.empty());

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

            var node = new ImportNode(pkgSegments, modName, symbols);

            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }
    }

    private static class DeclarationVisitor extends Visitor<DeclarationNode> {

        private MinaSyntaxNodeCollector collector;

        public DeclarationVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public DeclarationNode visitDataDeclaration(DataDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var node = new DataDeclarationNode(name);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public DeclarationNode visitLetDeclaration(LetDeclarationContext ctx) {
            var name = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);

            var exprVisitor = new ExprVisitor(collector);
            var expr = exprVisitor.visitNullable(ctx.expr());

            var node = new LetDeclarationNode(name, expr);

            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }

        @Override
        public DeclarationNode visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.dataDeclaration(), ctx.letDeclaration());
        }
    }

    private static class ExprVisitor extends Visitor<ExprNode> {

        private MinaSyntaxNodeCollector collector;

        public ExprVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public ExprNode visitExpr(ExprContext ctx) {
            return visitAlternatives(ctx.ifExpr(), ctx.lambdaExpr(), ctx.matchExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public ExprNode visitIfExpr(IfExprContext ctx) {
            var conditionNode = visitNullable(ctx.expr(0));
            var consequentNode = visitNullable(ctx.expr(1));
            var alternativeNode = visitNullable(ctx.expr(2));
            var node = new IfExprNode(conditionNode, consequentNode, alternativeNode);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public ExprNode visitLambdaExpr(LambdaExprContext ctx) {
            var params = ctx.lambdaParams().ID().stream()
                    .map(param -> new ParamNode(param.getText()))
                    .collect(Collectors2.toImmutableList());
            var bodyNode = visitNullable(ctx.expr());
            var node = new LambdaExprNode(params, bodyNode);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public ExprNode visitMatchExpr(MatchExprContext ctx) {
            var scrutineeNode = visitNullable(ctx.expr());
            var matchCaseVisitor = new MatchCaseVisitor(this, collector);
            var cases = visitRepeated(ctx.matchCase(), matchCaseVisitor);
            var node = new MatchNode(scrutineeNode, cases);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public ExprNode visitApplicableExpr(ApplicableExprContext ctx) {
            var exprNode = visitAlternatives(ctx.parenExpr(), ctx.qualifiedId());

            if (exprNode != null) {
                return exprNode;
            }

            var applicableExprNode = visitNullable(ctx.applicableExpr());

            if (applicableExprNode != null) {
                var args = visitRepeated(ctx.application().expr(), this);
                var node = new ApplyNode(applicableExprNode, args);
                collector.add(Tuples.pair(contextRange(ctx), node));
                return node;
            }

            return null;
        }

        @Override
        public ExprNode visitParenExpr(ParenExprContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public ExprNode visitQualifiedId(QualifiedIdContext ctx) {
            var idVisitor = new QualifiedIdVisitor(collector);
            var node = new ReferenceNode(idVisitor.visitNullable(ctx));
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public ExprNode visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalInt());
        }

        @Override
        public ExprNode visitLiteralBoolean(LiteralBooleanContext ctx) {
            var trueExpr = ctx.TRUE();
            if (trueExpr != null) {
                var node = new LiteralBooleanNode(true);
                collector.add(Tuples.pair(contextRange(ctx), node));
                return node;
            }

            var falseExpr = ctx.FALSE();
            if (falseExpr != null) {
                var node = new LiteralBooleanNode(false);
                collector.add(Tuples.pair(contextRange(ctx), node));
                return node;
            }

            return null;
        }

        @Override
        public ExprNode visitLiteralChar(LiteralCharContext ctx) {
            var charExpr = ctx.LITERAL_CHAR();
            var charValue = charExpr.getText().charAt(1);
            var node = new LiteralCharNode(charValue);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

        @Override
        public ExprNode visitLiteralInt(LiteralIntContext ctx) {
            var intExpr = ctx.LITERAL_INT();
            var node = new LiteralIntNode(Integer.parseInt(intExpr.getText()));
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }

    }

    private static class MatchCaseVisitor extends Visitor<CaseNode> {

        private ExprVisitor exprVisitor;
        private MinaSyntaxNodeCollector collector;

        public MatchCaseVisitor(ExprVisitor exprVisitor, MinaSyntaxNodeCollector collector) {
            this.exprVisitor = exprVisitor;
            this.collector = collector;
        }

        @Override
        public CaseNode visitMatchCase(MatchCaseContext ctx) {
            var patternVisitor = new PatternVisitor(collector);
            var patternNode = patternVisitor.visitNullable(ctx.pattern());
            var consequentNode = exprVisitor.visitNullable(ctx.expr());
            var node = new CaseNode(patternNode, consequentNode);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }
    }

    private static class PatternVisitor extends Visitor<PatternNode> {

        private MinaSyntaxNodeCollector collector;

        public PatternVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public PatternNode visitPattern(PatternContext ctx) {
            var id = ctx.ID();
            if (id != null) {
                var node = new IdPatternNode(id.getText());
                collector.add(Tuples.pair(contextRange(ctx), node));
                return node;
            }

            return visitNullable(ctx.constructorPattern());
        }

        @Override
        public PatternNode visitConstructorPattern(ConstructorPatternContext ctx) {
            var idVisitor = new QualifiedIdVisitor(collector);

            var id = idVisitor.visitNullable(ctx.qualifiedId());

            var alias = Optional.ofNullable(ctx.patternAlias())
                    .map(PatternAliasContext::ID)
                    .map(TerminalNode::getText);

            var fieldPatternVisitor = new FieldPatternVisitor(collector, this);

            var fieldPatterns = ctx.fieldPatterns();
            var fields = Lists.immutable.<FieldPatternNode>empty();
            if (fieldPatterns != null) {
                fields = visitRepeated(fieldPatterns.fieldPattern(), fieldPatternVisitor);
            }

            var node = new ConstructorPatternNode(id, alias, fields);
            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }

    }

    private static class FieldPatternVisitor extends Visitor<FieldPatternNode> {

        private MinaSyntaxNodeCollector collector;
        private PatternVisitor patternVisitor;

        public FieldPatternVisitor(MinaSyntaxNodeCollector collector, PatternVisitor patternVisitor) {
            this.collector = collector;
            this.patternVisitor = patternVisitor;
        }

        @Override
        public FieldPatternNode visitFieldPattern(FieldPatternContext ctx) {
            var id = Optional.ofNullable(ctx.ID()).map(TerminalNode::getText).orElse(null);
            var pattern = patternVisitor.visitNullable(ctx.pattern());
            var node = new FieldPatternNode(id, pattern);
            collector.add(Tuples.pair(contextRange(ctx), node));
            return node;
        }
    }

    private static class QualifiedIdVisitor extends Visitor<QualifiedIdNode> {

        private MinaSyntaxNodeCollector collector;

        public QualifiedIdVisitor(MinaSyntaxNodeCollector collector) {
            this.collector = collector;
        }

        @Override
        public QualifiedIdNode visitQualifiedId(QualifiedIdContext ctx) {
            var modId = Optional.ofNullable(ctx.moduleId());
            var packageId = modId.map(ModuleIdContext::packageId);

            var pkg = packageId.map(pkgId -> {
                return pkgId.ID().stream()
                        .map(node -> node.getText())
                        .collect(Collectors2.toImmutableList());
            }).orElse(Lists.immutable.empty());

            var pkgWithModName = modId
                    .map(ModuleIdContext::ID)
                    .map(id -> pkg.newWith(id.getText()));

            var id = Optional.ofNullable(ctx.ID())
                .map(TerminalNode::getText)
                .orElse(null);

            var node = new QualifiedIdNode(pkgWithModName.orElse(pkg), id);

            collector.add(Tuples.pair(contextRange(ctx), node));

            return node;
        }

    }
}
