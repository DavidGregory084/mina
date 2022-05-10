package org.mina_lang.langserver.semantic.tokens;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.eclipse.lsp4j.TextDocumentItem;
import org.mina_lang.parser.MinaBaseVisitor;
import org.mina_lang.parser.MinaLexer;
import org.mina_lang.parser.MinaParser;
import org.mina_lang.parser.MinaParser.ApplicableExprContext;
import org.mina_lang.parser.MinaParser.ApplicationContext;
import org.mina_lang.parser.MinaParser.ArgumentsContext;
import org.mina_lang.parser.MinaParser.CompilationUnitContext;
import org.mina_lang.parser.MinaParser.DataDeclarationContext;
import org.mina_lang.parser.MinaParser.DeclarationContext;
import org.mina_lang.parser.MinaParser.ExprContext;
import org.mina_lang.parser.MinaParser.IfExprContext;
import org.mina_lang.parser.MinaParser.ImportDeclarationContext;
import org.mina_lang.parser.MinaParser.ImportSelectorContext;
import org.mina_lang.parser.MinaParser.ImportSymbolsContext;
import org.mina_lang.parser.MinaParser.LambdaExprContext;
import org.mina_lang.parser.MinaParser.LambdaParamsContext;
import org.mina_lang.parser.MinaParser.LetDeclarationContext;
import org.mina_lang.parser.MinaParser.LiteralBooleanContext;
import org.mina_lang.parser.MinaParser.LiteralContext;
import org.mina_lang.parser.MinaParser.LiteralIntContext;
import org.mina_lang.parser.MinaParser.ModuleContext;
import org.mina_lang.parser.MinaParser.ModuleIdContext;
import org.mina_lang.parser.MinaParser.PackageIdContext;
import org.mina_lang.parser.MinaParser.ParenExprContext;
import org.mina_lang.parser.MinaParser.QualifiedIdContext;

public class SemanticTokensParser {
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

        @Override
        public IntStream visitCompilationUnit(CompilationUnitContext ctx) {
            return ctx.module().stream().flatMapToInt(mod -> {
                return visitModule(mod);
            });
        }

        @Override
        public IntStream visitModule(ModuleContext ctx) {
            var moduleToken = createToken(ctx.MODULE(), SemanticTokenTypes.Keyword, SemanticTokenModifiers.Declaration);

            var moduleIdTokens = visit(ctx.moduleId());

            var importSelectorTokens = ctx.importDeclaration().stream()
                    .flatMapToInt(imp -> visitImportDeclaration(imp));

            var declarationTokens = ctx.declaration().stream()
                    .flatMapToInt(decl -> visitDeclaration(decl));

            return Stream.of(
                    moduleToken,
                    moduleIdTokens,
                    importSelectorTokens,
                    declarationTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitModuleId(ModuleIdContext ctx) {
            var packageIdTokens = visit(ctx.packageId());

            var moduleNameToken = createToken(ctx.ID(), SemanticTokenTypes.Class, SemanticTokenModifiers.Declaration);

            return Stream.of(
                    packageIdTokens,
                    moduleNameToken).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitPackageId(PackageIdContext ctx) {
            return ctx.ID().stream()
                    .flatMapToInt(pkgSegment -> {
                        return createToken(pkgSegment,
                                SemanticTokenTypes.Namespace,
                                SemanticTokenModifiers.Declaration);
                    });
        }

        @Override
        public IntStream visitImportDeclaration(ImportDeclarationContext ctx) {
            var importToken = createToken(ctx.IMPORT(), SemanticTokenTypes.Keyword);

            var selectorTokens = visit(ctx.importSelector());

            return Stream.of(
                    importToken,
                    selectorTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitImportSelector(ImportSelectorContext ctx) {
            var qualifiedId = ctx.qualifiedId();
            if (qualifiedId != null) {
                return visit(qualifiedId);
            }

            var moduleId = ctx.moduleId();
            var moduleIdTokens = moduleId != null ? visit(moduleId) : IntStream.empty();

            var importSymbols = ctx.importSymbols();
            var importSymbolTokens = importSymbols != null ? visit(importSymbols) : IntStream.empty();

            return Stream.of(
                    moduleIdTokens,
                    importSymbolTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitImportSymbols(ImportSymbolsContext ctx) {
            return ctx.ID().stream()
                    .flatMapToInt(sym -> {
                        return createToken(sym, SemanticTokenTypes.Variable);
                    });
        }

        @Override
        public IntStream visitDeclaration(DeclarationContext ctx) {
            var letDeclaration = ctx.letDeclaration();
            if (letDeclaration != null) {
                return visit(letDeclaration);
            }

            var dataDeclaration = ctx.dataDeclaration();
            if (dataDeclaration != null) {
                return visit(dataDeclaration);
            }

            return IntStream.empty();
        }

        @Override
        public IntStream visitLetDeclaration(LetDeclarationContext ctx) {
            var letToken = createToken(ctx.LET(), SemanticTokenTypes.Keyword, SemanticTokenModifiers.Declaration);

            var nameToken = createToken(ctx.ID(), SemanticTokenTypes.Function, SemanticTokenModifiers.Declaration);

            var exprTokens = visit(ctx.expr());

            return Stream.of(
                    letToken,
                    nameToken,
                    exprTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitExpr(ExprContext ctx) {
            var ifExpr = ctx.ifExpr();
            if (ifExpr != null) {
                return visit(ifExpr);
            }

            var lambdaExpr = ctx.lambdaExpr();
            if (lambdaExpr != null) {
                return visit(lambdaExpr);
            }

            var literal = ctx.literal();
            if (literal != null) {
                return visit(literal);
            }

            var applicableExpr = ctx.applicableExpr();
            if (applicableExpr != null) {
                return visit(applicableExpr);
            }

            return IntStream.empty();
        }

        @Override
        public IntStream visitIfExpr(IfExprContext ctx) {
            var ifToken = createToken(ctx.IF(), SemanticTokenTypes.Keyword);

            var condition = ctx.expr(0);
            var conditionTokens = condition != null ? visit(condition) : IntStream.empty();

            var thenToken = createToken(ctx.THEN(), SemanticTokenTypes.Keyword);

            var consequent = ctx.expr(1);
            var consequentTokens = consequent != null ? visit(consequent) : IntStream.empty();

            var elseToken = createToken(ctx.ELSE(), SemanticTokenTypes.Keyword);

            var alternative = ctx.expr(2);
            var alternativeTokens = alternative != null ? visit(alternative) : IntStream.empty();

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
            var lambdaParams = ctx.lambdaParams();
            var paramTokens = lambdaParams != null ? visit(lambdaParams) : IntStream.empty();

            var arrowToken = createToken(ctx.ARROW(), SemanticTokenTypes.Operator);

            var bodyExpr = ctx.expr();
            var bodyTokens = bodyExpr != null ? visit(bodyExpr) : IntStream.empty();

            return Stream.of(
                    paramTokens,
                    arrowToken,
                    bodyTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitLambdaParams(LambdaParamsContext ctx) {
            return ctx.ID().stream()
                    .flatMapToInt(param -> {
                        return createToken(param, SemanticTokenTypes.Parameter);
                    });
        }

        @Override
        public IntStream visitLiteral(LiteralContext ctx) {
            var literalBoolean = ctx.literalBoolean();
            if (literalBoolean != null) {
                return visit(literalBoolean);
            }

            var literalInt = ctx.literalInt();
            if (literalInt != null) {
                return visit(literalInt);
            }

            return IntStream.empty();
        }

        @Override
        public IntStream visitLiteralBoolean(LiteralBooleanContext ctx) {
            var trueLiteral = ctx.TRUE();
            if (trueLiteral != null) {
                return createToken(ctx.TRUE(), SemanticTokenTypes.EnumMember, SemanticTokenModifiers.DefaultLibrary);
            }

            var falseLiteral = ctx.FALSE();
            if (falseLiteral != null) {
                return createToken(ctx.FALSE(), SemanticTokenTypes.EnumMember, SemanticTokenModifiers.DefaultLibrary);
            }

            return IntStream.empty();
        }

        @Override
        public IntStream visitLiteralInt(LiteralIntContext ctx) {
            return createToken(ctx.LITERAL_INT(), SemanticTokenTypes.Number);
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

            var applicableExpr = ctx.applicableExpr();
            var applicableExprTokens = applicableExpr != null ? visit(applicableExpr) : IntStream.empty();

            var application = ctx.application();
            var applicationTokens = application != null ? visit(application) : IntStream.empty();

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
            var moduleId = ctx.moduleId();
            var moduleIdTokens = moduleId != null ? visit(moduleId) : IntStream.empty();

            var idTokens = createToken(ctx.ID(), SemanticTokenTypes.Variable);

            return Stream.of(
                    moduleIdTokens,
                    idTokens).flatMapToInt(x -> x);
        }

        @Override
        public IntStream visitApplication(ApplicationContext ctx) {
            return visit(ctx.arguments());
        }

        @Override
        public IntStream visitArguments(ArgumentsContext ctx) {
            return ctx.expr().stream()
                    .flatMapToInt(expr -> visit(expr));
        }

        @Override
        public IntStream visitDataDeclaration(DataDeclarationContext ctx) {
            var dataToken = createToken(ctx.DATA(), SemanticTokenTypes.Keyword, SemanticTokenModifiers.Declaration);

            return Stream.of(
                    dataToken).flatMapToInt(x -> x);
        }

    }
}
