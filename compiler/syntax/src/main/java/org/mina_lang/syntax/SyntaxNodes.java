package org.mina_lang.syntax;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Range;

import java.util.Optional;

public class SyntaxNodes {
    public static CompilationUnitNode<Void> compilationUnitNode(
            Range range,
            ImmutableList<ModuleNode<Void>> modules) {
        return new CompilationUnitNode<>(Meta.empty(range), modules);
    }

    public static <A> CompilationUnitNode<A> compilationUnitNode(
            Meta<A> meta,
            ImmutableList<ModuleNode<A>> modules) {
        return new CompilationUnitNode<>(meta, modules);
    }

    // Module header
    public static ModuleNode<Void> moduleNode(
            Range range,
            ImmutableList<String> pkg,
            String name,
            ImmutableList<ImportNode<Void>> imports,
            ImmutableList<DeclarationNode<Void>> declarations) {
        return new ModuleNode<>(Meta.empty(range), pkg, name, imports, declarations);
    }

    public static <A> ModuleNode<A> moduleNode(
            Meta<A> meta,
            ImmutableList<String> pkg,
            String name,
            ImmutableList<ImportNode<Void>> imports,
            ImmutableList<DeclarationNode<A>> declarations) {
        return new ModuleNode<>(meta, pkg, name, imports, declarations);
    }

    public static ImportNode<Void> importNode(
            Range range,
            ImmutableList<String> pkg,
            String mod) {
        return new ImportNode<>(Meta.empty(range), pkg, mod, Lists.immutable.empty());
    }

    public static ImportNode<Void> importNode(
            Range range,
            ImmutableList<String> pkg,
            String mod,
            String symbol) {
        return new ImportNode<>(Meta.empty(range), pkg, mod, Lists.immutable.of(symbol));
    }

    public static ImportNode<Void> importNode(
            Range range,
            ImmutableList<String> pkg,
            String mod,
            ImmutableList<String> symbols) {
        return new ImportNode<>(Meta.empty(range), pkg, mod, symbols);
    }

    // Top level declarations
    public static DataDeclarationNode<Void> dataDeclarationNode(
            Range range,
            String name) {
        return new DataDeclarationNode<>(Meta.empty(range), name);
    }

    public static LetDeclarationNode<Void> letDeclarationNode(
            Range range,
            String name,
            ExprNode<Void> expr) {
        return new LetDeclarationNode<>(Meta.empty(range), name, expr);
    }

    public static <A> LetDeclarationNode<A> letDeclarationNode(
            Meta<A> meta,
            String name,
            ExprNode<A> expr) {
        return new LetDeclarationNode<>(meta, name, expr);
    }

    // Types
    public static TypeLambdaNode<Void> typeLambdaNode(
        Range range,
        ImmutableList<TypeVarNode<Void>> args,
        TypeNode<Void> body
    ) {
        return new TypeLambdaNode<>(Meta.empty(range), args, body);
    }

    public static <A> TypeLambdaNode<A> typeLambdaNode(
        Meta<A> meta,
        ImmutableList<TypeVarNode<A>> args,
        TypeNode<A> body
    ) {
        return new TypeLambdaNode<>(meta, args, body);
    }

    public static FunTypeNode<Void> funTypeNode(
        Range range,
        ImmutableList<TypeNode<Void>> argTypes,
        TypeNode<Void> returnType
    ) {
        return new FunTypeNode<>(Meta.empty(range), argTypes, returnType);
    }

    public static <A> FunTypeNode<A> funTypeNode(
        Meta<A> meta,
        ImmutableList<TypeNode<A>> argTypes,
        TypeNode<A> returnType
    ) {
        return new FunTypeNode<>(meta, argTypes, returnType);
    }

    public static TypeApplyNode<Void> typeApplyNode(
       Range range,
       TypeNode<Void> type,
       ImmutableList<TypeNode<Void>> args
    ) {
        return new TypeApplyNode<>(Meta.empty(range), type, args);
    }

    public static <A> TypeApplyNode<A> typeApplyNode(
       Meta<A> meta,
       TypeNode<A> type,
       ImmutableList<TypeNode<A>> args
    ) {
        return new TypeApplyNode<>(meta, type, args);
    }

    public static TypeReferenceNode<Void> typeReferenceNode(
        Range range,
        QualifiedIdNode<Void> id
    ) {
        return new TypeReferenceNode<>(Meta.empty(range), id);
    }

    public static <A> TypeReferenceNode<A> typeReferenceNode(
        Meta<A> meta,
        QualifiedIdNode<A> id
    ) {
        return new TypeReferenceNode<>(meta, id);
    }

    public static TypeReferenceNode<Void> typeReferenceNode(
        Range range,
        String name
    ) {
        var meta = Meta.empty(range);
        return new TypeReferenceNode<>(meta, idNode(meta, name));
    }

    public static <A> TypeReferenceNode<A> typeReferenceNode(
        Meta<A> meta,
        String name
    ) {
        return new TypeReferenceNode<>(meta, idNode(meta, name));
    }

    public static UniversalTypeVarNode<Void> forAllVarNode(
        Range range,
        String name
    ) {
        return new UniversalTypeVarNode<>(Meta.empty(range), name);
    }

    public static <A> UniversalTypeVarNode<A> forAllVarNode(
        Meta<A> meta,
        String name
    ) {
        return new UniversalTypeVarNode<>(meta, name);
    }

    public static ExistentialTypeVarNode<Void> existsVarNode(
        Range range,
        String name
    ) {
        return new ExistentialTypeVarNode<>(Meta.empty(range), name);
    }

    public static <A> ExistentialTypeVarNode<A> existsVarNode(
        Meta<A> meta,
        String name
    ) {
        return new ExistentialTypeVarNode<>(meta, name);
    }

    // Control structures
    public static IfExprNode<Void> ifExprNode(
            Range range,
            ExprNode<Void> condition,
            ExprNode<Void> consequent,
            ExprNode<Void> alternative) {
        return new IfExprNode<>(Meta.empty(range), condition, consequent, alternative);
    }

    public static <A> IfExprNode<A> ifExprNode(
            Meta<A> meta,
            ExprNode<A> condition,
            ExprNode<A> consequent,
            ExprNode<A> alternative) {
        return new IfExprNode<>(meta, condition, consequent, alternative);
    }

    public static LambdaExprNode<Void> lambdaExprNode(
            Range range,
            ImmutableList<ParamNode<Void>> params,
            ExprNode<Void> body) {
        return new LambdaExprNode<>(Meta.empty(range), params, body);
    }

    public static <A> LambdaExprNode<A> lambdaExprNode(
            Meta<A> meta,
            ImmutableList<ParamNode<A>> params,
            ExprNode<A> body) {
        return new LambdaExprNode<>(meta, params, body);
    }

    public static MatchNode<Void> matchNode(
            Range range,
            ExprNode<Void> scrutinee,
            ImmutableList<CaseNode<Void>> cases) {
        return new MatchNode<>(Meta.empty(range), scrutinee, cases);
    }

    public static <A> MatchNode<A> matchNode(
            Meta<A> meta,
            ExprNode<A> scrutinee,
            ImmutableList<CaseNode<A>> cases) {
        return new MatchNode<>(meta, scrutinee, cases);
    }

    public static ApplyNode<Void> applyNode(
            Range range,
            ExprNode<Void> expr,
            ImmutableList<ExprNode<Void>> args) {
        return new ApplyNode<>(Meta.empty(range), expr, args);
    }

    public static <A> ApplyNode<A> applyNode(
            Meta<A> meta,
            ExprNode<A> expr,
            ImmutableList<ExprNode<A>> args) {
        return new ApplyNode<>(meta, expr, args);
    }

    // Atomic expressions
    public static LiteralBooleanNode<Void> boolNode(
            Range range,
            boolean value) {
        return new LiteralBooleanNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralBooleanNode<A> boolNode(
            Meta<A> meta,
            boolean value) {
        return new LiteralBooleanNode<>(meta, value);
    }

    public static LiteralCharNode<Void> charNode(
            Range range,
            char value) {
        return new LiteralCharNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralCharNode<A> charNode(
            Meta<A> meta,
            char value) {
        return new LiteralCharNode<>(meta, value);
    }

    public static LiteralStringNode<Void> stringNode(
            Range range,
            String value) {
        return new LiteralStringNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralStringNode<A> stringNode(
            Meta<A> meta,
            String value) {
        return new LiteralStringNode<>(meta, value);
    }

    public static LiteralIntNode<Void> intNode(
            Range range,
            int value) {
        return new LiteralIntNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralIntNode<A> intNode(
            Meta<A> meta,
            int value) {
        return new LiteralIntNode<>(meta, value);
    }

    public static LiteralLongNode<Void> longNode(
            Range range,
            long value) {
        return new LiteralLongNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralLongNode<A> longNode(
            Meta<A> meta,
            long value) {
        return new LiteralLongNode<>(meta, value);
    }

    public static LiteralFloatNode<Void> floatNode(
            Range range,
            float value) {
        return new LiteralFloatNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralFloatNode<A> floatNode(
            Meta<A> meta,
            float value) {
        return new LiteralFloatNode<>(meta, value);
    }

    public static LiteralDoubleNode<Void> doubleNode(
            Range range,
            double value) {
        return new LiteralDoubleNode<>(Meta.empty(range), value);
    }

    public static <A> LiteralDoubleNode<A> doubleNode(
            Meta<A> meta,
            double value) {
        return new LiteralDoubleNode<>(meta, value);
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            ImmutableList<String> pkg,
            String name) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, new QualifiedIdNode<>(meta, pkg, name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            String name) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, new QualifiedIdNode<>(meta, Lists.immutable.empty(), name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            QualifiedIdNode<Void> id) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, id);
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            ImmutableList<String> pkg,
            String name) {
        return new ReferenceNode<>(meta, new QualifiedIdNode<>(meta, pkg, name));
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            String name) {
        return new ReferenceNode<>(meta, new QualifiedIdNode<>(meta, Lists.immutable.empty(), name));
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            QualifiedIdNode<A> id) {
        return new ReferenceNode<>(meta, id);
    }

    // Patterns
    public static CaseNode<Void> caseNode(
            Range range,
            PatternNode<Void> pattern,
            ExprNode<Void> consequent) {
        return new CaseNode<>(Meta.empty(range), pattern, consequent);
    }

    public static <A> CaseNode<A> caseNode(
            Meta<A> meta,
            PatternNode<A> pattern,
            ExprNode<A> consequent) {
        return new CaseNode<>(meta, pattern, consequent);
    }

    public static IdPatternNode<Void> idPatternNode(
            Range range,
            Optional<String> alias,
            String name) {
        return new IdPatternNode<>(Meta.empty(range), alias, name);
    }

    public static <A> IdPatternNode<A> idPatternNode(
            Meta<A> meta,
            Optional<String> alias,
            String name) {
        return new IdPatternNode<>(meta, alias, name);
    }

    public static LiteralPatternNode<Void> literalPatternNode(
            Range range,
            Optional<String> alias,
            LiteralNode<Void> literal) {
        return new LiteralPatternNode<>(Meta.empty(range), alias, literal);
    }

    public static <A> LiteralPatternNode<A> literalPatternNode(
            Meta<A> meta,
            Optional<String> alias,
            LiteralNode<A> literal) {
        return new LiteralPatternNode<>(meta, alias, literal);
    }

    public static ConstructorPatternNode<Void> constructorPatternNode(
            Range range,
            Optional<String> alias,
            QualifiedIdNode<Void> id,
            ImmutableList<FieldPatternNode<Void>> fields) {
        return new ConstructorPatternNode<>(Meta.empty(range), alias, id, fields);
    }

    public static <A> ConstructorPatternNode<A> constructorPatternNode(
            Meta<A> meta,
            Optional<String> alias,
            QualifiedIdNode<A> id,
            ImmutableList<FieldPatternNode<A>> fields) {
        return new ConstructorPatternNode<>(meta, alias, id, fields);
    }

    public static FieldPatternNode<Void> fieldPatternNode(
            Range range,
            String field,
            Optional<PatternNode<Void>> pattern) {
        return new FieldPatternNode<>(Meta.empty(range), field, pattern);
    }

    public static <A> FieldPatternNode<A> fieldPatternNode(
            Meta<A> meta,
            String field,
            Optional<PatternNode<A>> pattern) {
        return new FieldPatternNode<>(meta, field, pattern);
    }

    // Syntax elements
    public static ParamNode<Void> paramNode(
            Range range,
            String name) {
        return new ParamNode<>(Meta.empty(range), name);
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name) {
        return new ParamNode<>(meta, name);
    }

    public static QualifiedIdNode<Void> idNode(
            Range range,
            ImmutableList<String> pkg,
            String name) {
        return new QualifiedIdNode<>(Meta.empty(range), pkg, name);
    }

    public static QualifiedIdNode<Void> idNode(
            Range range,
            String name) {
        return new QualifiedIdNode<>(Meta.empty(range), Lists.immutable.empty(), name);
    }

    public static <A> QualifiedIdNode<A> idNode(
            Meta<A> meta,
            ImmutableList<String> pkg,
            String name) {
        return new QualifiedIdNode<>(meta, pkg, name);
    }

    public static <A> QualifiedIdNode<A> idNode(
            Meta<A> meta,
            String name) {
        return new QualifiedIdNode<>(meta, Lists.immutable.empty(), name);
    }
}
