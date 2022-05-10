package org.mina_lang.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.lsp4j.TextDocumentItem;
import org.mina_lang.parser.MinaParser.CompilationUnitContext;
import org.mina_lang.parser.MinaParser.DataDeclarationContext;
import org.mina_lang.parser.MinaParser.DeclarationContext;
import org.mina_lang.parser.MinaParser.LetDeclarationContext;
import org.mina_lang.parser.MinaParser.ModuleContext;
import org.mina_lang.syntax.CompilationUnitNode;
import org.mina_lang.syntax.DataDeclarationNode;
import org.mina_lang.syntax.DeclarationNode;
import org.mina_lang.syntax.LetDeclarationNode;
import org.mina_lang.syntax.ModuleNode;

public class CompilationUnitParser {

    public static CompilationUnitNode parse(TextDocumentItem document, ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(document.getText(), document.getUri());
        return parse(charStream, errorListener);
    }

    public static CompilationUnitNode parse(String source, ANTLRErrorListener errorListener) {
        var charStream = CharStreams.fromString(source);
        return parse(charStream, errorListener);
    }

    public static CompilationUnitNode parse(CharStream charStream, ANTLRErrorListener errorListener) {
        var lexer = new MinaLexer(charStream);
        lexer.addErrorListener(errorListener);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new MinaParser(tokenStream);
        parser.addErrorListener(errorListener);
        var visitor = new CompilationUnitVisitor();
        return visitor.visit(parser.compilationUnit());
    }

    private static class CompilationUnitVisitor extends MinaBaseVisitor<CompilationUnitNode> {
        @Override
        public CompilationUnitNode visitCompilationUnit(CompilationUnitContext ctx) {
            var moduleVisitor = new ModuleVisitor();

            var modules = ctx.module().stream()
                    .map(mod -> moduleVisitor.visit(mod))
                    .collect(Collectors2.toImmutableList());

            return new CompilationUnitNode(modules);
        }
    }

    private static class ModuleVisitor extends MinaBaseVisitor<ModuleNode> {
        @Override
        public ModuleNode visitModule(ModuleContext ctx) {
            var modId = ctx.moduleId();
            var pkgId = modId.packageId();

            var pkg = pkgId.ID().stream()
                    .map(node -> node.getText())
                    .collect(Collectors2.toImmutableList());

            var name = modId.ID().getText();

            var declarationVisitor = new DeclarationVisitor();

            var declarations = ctx.declaration().stream()
                    .map(decl -> declarationVisitor.visit(decl))
                    .collect(Collectors2.toImmutableList());

            return new ModuleNode(pkg, name, declarations);
        }
    }

    private static class DeclarationVisitor extends MinaBaseVisitor<DeclarationNode> {

        @Override
        public DeclarationNode visitDataDeclaration(DataDeclarationContext ctx) {
            var name = ctx.ID().getText();
            return new DataDeclarationNode(name);
        }

        @Override
        public DeclarationNode visitLetDeclaration(LetDeclarationContext ctx) {
            var name = ctx.ID().getText();
            return new LetDeclarationNode(name);
        }

        @Override
        public DeclarationNode visitDeclaration(DeclarationContext ctx) {
            var data = ctx.dataDeclaration();
            if (data != null) {
                return visit(data);
            }
            var let = ctx.letDeclaration();
            if (let != null) {
                return visit(let);
            }
            return null;
        }
    }
}
