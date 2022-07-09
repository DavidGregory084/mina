package org.mina_lang.langserver.semantic.tokens;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.TextDocumentItem;
import org.mina_lang.parser.MinaBaseVisitor;
import org.mina_lang.parser.MinaLexer;
import org.mina_lang.parser.MinaParser;
import org.mina_lang.parser.MinaParser.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.eclipse.lsp4j.SemanticTokenModifiers.*;
import static org.eclipse.lsp4j.SemanticTokenTypes.*;

public class MinaSemanticTokensParser {
    public static List<Integer> parseTokens(TextDocumentItem document) {
        var charStream = CharStreams.fromString(document.getText());
        var lexer = new MinaLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream);
        var visitor = new SemanticTokensVisitor();
        var semanticTokenStream = visitor.visit(parser.compilationUnit());
        var semanticTokens = semanticTokenStream.boxed().collect(Collectors.toList());
        return semanticTokens;
    }

    private static class SemanticTokensVisitor extends MinaBaseVisitor<IntStream> {
        int previousLine = -1;
        int previousPos = -1;

        private IntStream createToken(TerminalNode node, String tokenType, String... tokenModifiers) {
            if (node == null) {
                return IntStream.empty();
            } else {
                var token = node.getSymbol();
                var tokenLine = token.getLine() - 1;
                var tokenPos = token.getCharPositionInLine();

                return Stream.generate(() -> {
                    int deltaLine;

                    // The first token sets our starting line
                    if (previousLine < 0) {
                        deltaLine = tokenLine;
                        previousLine = tokenLine;
                    } else {
                        deltaLine = tokenLine - previousLine;
                        previousLine = tokenLine;
                    }

                    // Moved to the next line, so we use the first token to set position
                    if (deltaLine > 0) {
                        previousPos = -1;
                    }

                    int deltaPos;

                    if (previousPos < 0) {
                        deltaPos = tokenPos;
                        previousPos = tokenPos;
                    } else {
                        deltaPos = tokenPos - previousPos;
                        previousPos = tokenPos;
                    }

                    var length = token.getText().length();

                    var tokenTypeIdx = MinaSemanticTokens.typeIndices.get(tokenType);

                    var tokenModBitmap = 0;
                    for (String modifier : tokenModifiers) {
                        tokenModBitmap |= (1 << MinaSemanticTokens.modifierIndices.get(modifier));
                    }

                    return IntStream.of(deltaLine, deltaPos, length, tokenTypeIdx, tokenModBitmap);
                }).limit(1).flatMapToInt(x -> x);
            }
        }

        public IntStream visitNullable(ParseTree tree) {
            return tree != null ? visit(tree) : IntStream.empty();
        }

        public IntStream visitAlternatives(ParseTree... tree) {
            return Stream.of(tree)
                    .filter(t -> t != null)
                    .findFirst()
                    .map(t -> visit(t))
                    .orElse(IntStream.empty());
        }

        public IntStream visitRepeated(List<? extends ParseTree> trees) {
            return trees.stream().map(ctx -> visit(ctx)).flatMapToInt(x -> x);
        }

        public <A extends RuleContext, B extends RuleContext> IntStream visitNullableRepeated(A tree,
                Function<A, List<B>> rule) {
            return tree == null ? IntStream.empty()
                    : rule.apply(tree).stream().map(ctx -> visit(ctx)).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitCompilationUnit(CompilationUnitContext ctx) {
            return visitRepeated(ctx.module());
        }

        @Override
        public IntStream visitModule(ModuleContext ctx) {
            var moduleToken = createToken(ctx.MODULE(), Keyword, Declaration);

            var moduleIdTokens = visitNullable(ctx.moduleId());

            var importSelectorTokens = visitRepeated(ctx.importDeclaration());

            var declarationTokens = visitRepeated(ctx.declaration());

            return Stream.of(
                    moduleToken,
                    moduleIdTokens,
                    importSelectorTokens,
                    declarationTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitModuleId(ModuleIdContext ctx) {
            var idNodes = ctx.ID();
            var separatorsCount = idNodes.size() - 1;

            var packageTokens = IntStream.range(0, separatorsCount)
                    .flatMap(idx -> {
                        var idToken = createToken(ctx.ID(idx), Namespace, Declaration);
                        var rslashToken = idx == separatorsCount ? IntStream.empty()
                                : createToken(ctx.RSLASH(idx), Operator);
                        return Stream.of(idToken, rslashToken).flatMapToInt(x -> x);
                    });

            var moduleNameToken = createToken(ctx.ID(idNodes.size()), Class, Declaration);

            return Stream.of(
                    packageTokens,
                    moduleNameToken).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitImportDeclaration(ImportDeclarationContext ctx) {
            var importToken = createToken(ctx.IMPORT(), Keyword);

            var selectorTokens = visitNullable(ctx.importSelector());

            return Stream.of(
                    importToken,
                    selectorTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitImportSelector(ImportSelectorContext ctx) {
            var moduleIdTokens = visitNullable(ctx.moduleId());
            var symbolToken = createToken(ctx.ID(), Variable);
            var importSymbolsTokens = visitNullable(ctx.importSymbols());

            return Stream.of(
                    moduleIdTokens,
                    symbolToken,
                    importSymbolsTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitImportSymbols(ImportSymbolsContext ctx) {
            return ctx.ID().stream()
                    .flatMapToInt(sym -> {
                        return createToken(sym, Function);
                    });
        }

        @Override
        public IntStream visitDeclaration(DeclarationContext ctx) {
            return visitAlternatives(ctx.letDeclaration(), ctx.dataDeclaration());
        }

        @Override
        public IntStream visitLetDeclaration(LetDeclarationContext ctx) {
            var letToken = createToken(ctx.LET(), Keyword, Declaration);

            var nameToken = createToken(ctx.ID(), Function, Declaration, Static, Readonly);

            var exprTokens = visitNullable(ctx.expr());

            return Stream.of(
                    letToken,
                    nameToken,
                    exprTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitExpr(ExprContext ctx) {
            return visitAlternatives(ctx.ifExpr(), ctx.matchExpr(), ctx.lambdaExpr(), ctx.literal(),
                    ctx.applicableExpr());
        }

        @Override
        public IntStream visitIfExpr(IfExprContext ctx) {
            var ifToken = createToken(ctx.IF(), Keyword);
            var conditionTokens = visitNullable(ctx.expr(0));
            var thenToken = createToken(ctx.THEN(), Keyword);
            var consequentTokens = visitNullable(ctx.expr(1));
            var elseToken = createToken(ctx.ELSE(), Keyword);
            var alternativeTokens = visitNullable(ctx.expr(2));

            return Stream.of(
                    ifToken,
                    conditionTokens,
                    thenToken,
                    consequentTokens,
                    elseToken,
                    alternativeTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitLambdaExpr(LambdaExprContext ctx) {
            var paramTokens = visitNullable(ctx.lambdaParams());
            var arrowToken = createToken(ctx.ARROW(), Operator);
            var bodyTokens = visitNullable(ctx.expr());

            return Stream.of(
                    paramTokens,
                    arrowToken,
                    bodyTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitLambdaParams(LambdaParamsContext ctx) {
            return ctx.ID().stream()
                    .flatMapToInt(param -> {
                        return createToken(param, Parameter);
                    });
        }

        @Override
        public IntStream visitLiteral(LiteralContext ctx) {
            return visitAlternatives(ctx.literalBoolean(), ctx.literalChar(), ctx.literalString(), ctx.literalInt(),
                    ctx.literalFloat());
        }

        @Override
        public IntStream visitLiteralBoolean(LiteralBooleanContext ctx) {
            var trueLiteral = ctx.TRUE();
            if (trueLiteral != null) {
                return createToken(ctx.TRUE(), EnumMember, DefaultLibrary);
            }

            var falseLiteral = ctx.FALSE();
            if (falseLiteral != null) {
                return createToken(ctx.FALSE(), EnumMember, DefaultLibrary);
            }

            return IntStream.empty();
        }

        @Override
        public IntStream visitLiteralInt(LiteralIntContext ctx) {
            return createToken(ctx.LITERAL_INT(), Number);
        }

        @Override
        public IntStream visitLiteralChar(LiteralCharContext ctx) {
            return createToken(ctx.LITERAL_CHAR(), Number);
        }

        @Override
        public IntStream visitLiteralFloat(LiteralFloatContext ctx) {
            return createToken(ctx.LITERAL_FLOAT(), Number);
        }

        @Override
        public IntStream visitLiteralString(LiteralStringContext ctx) {
            return createToken(ctx.LITERAL_STRING(), String);
        }

        @Override
        public IntStream visitApplicableExpr(ApplicableExprContext ctx) {
            var parenExpr = ctx.parenExpr();
            if (parenExpr != null) {
                return visit(parenExpr);
            }

            var qualifiedId = ctx.qualifiedId();
            if (qualifiedId != null) {
                return visit(qualifiedId);
            }

            var applicableExprTokens = visitNullable(ctx.applicableExpr());

            var applicationTokens = visitNullable(ctx.application());

            return Stream.of(
                    applicableExprTokens,
                    applicationTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitParenExpr(ParenExprContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public IntStream visitQualifiedId(QualifiedIdContext ctx) {
            var moduleIdTokens = visitNullable(ctx.moduleId());
            var dotTokens = createToken(ctx.DOT(), Operator);
            var idTokens = createToken(ctx.ID(), Function);
            return Stream.of(moduleIdTokens, dotTokens, idTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitApplication(ApplicationContext ctx) {
            return visitRepeated(ctx.expr());
        }

        @Override
        public IntStream visitDataDeclaration(DataDeclarationContext ctx) {
            var dataToken = createToken(ctx.DATA(), Keyword, Declaration);
            return Stream.of(dataToken).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitConstructorPattern(ConstructorPatternContext ctx) {
            var aliasTokens = visitNullable(ctx.patternAlias());
            var idTokens = visitNullable(ctx.qualifiedId());
            var fieldTokens = visitNullableRepeated(ctx.fieldPatterns(), FieldPatternsContext::fieldPattern);
            return Stream.of(aliasTokens, idTokens, fieldTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitFieldPattern(FieldPatternContext ctx) {
            var idTokens = createToken(ctx.ID(), Variable);
            var patternTokens = visitNullable(ctx.pattern());
            return Stream.of(idTokens, patternTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitIdPattern(IdPatternContext ctx) {
            var aliasTokens = visitNullable(ctx.patternAlias());
            var idTokens = createToken(ctx.ID(), Variable);
            return Stream.of(aliasTokens, idTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitLiteralPattern(LiteralPatternContext ctx) {
            var aliasTokens = visitNullable(ctx.patternAlias());
            var literalTokens = visitNullable(ctx.literal());
            return Stream.of(aliasTokens, literalTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitMatchCase(MatchCaseContext ctx) {
            var patternTokens = visitNullable(ctx.pattern());
            var consequentTokens = visitNullable(ctx.expr());
            return Stream.of(patternTokens, consequentTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitMatchExpr(MatchExprContext ctx) {
            var scrutineeTokens = visitNullable(ctx.expr());
            var casesTokens = visitRepeated(ctx.matchCase());
            return Stream.of(scrutineeTokens, casesTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitPattern(PatternContext ctx) {
            return visitAlternatives(ctx.idPattern(), ctx.literalPattern(), ctx.constructorPattern());
        }

        @Override
        public IntStream visitPatternAlias(PatternAliasContext ctx) {
            var idTokens = createToken(ctx.ID(), Variable);
            var atTokens = createToken(ctx.AT(), Operator);
            return Stream.of(idTokens, atTokens).flatMapToInt(x -> x);
        }
    }
}
