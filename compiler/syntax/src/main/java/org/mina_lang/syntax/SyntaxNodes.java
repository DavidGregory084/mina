package org.mina_lang.syntax;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Range;

import java.util.Optional;

public class SyntaxNodes {
    // Namespace
    public static NamespaceNode<Void> namespaceNode(
            Range range,
            NamespaceIdNode<Void> id,
            ImmutableList<ImportNode<Void>> imports,
            ImmutableList<DeclarationNode<Void>> declarations) {
        return new NamespaceNode<>(Meta.empty(range), id, imports, declarations);
    }

    public static <A> NamespaceNode<A> namespaceNode(
            Meta<A> meta,
            NamespaceIdNode<Void> id,
            ImmutableList<ImportNode<Void>> imports,
            ImmutableList<DeclarationNode<A>> declarations) {
        return new NamespaceNode<>(meta, id, imports, declarations);
    }

    public static ImportNode<Void> importNode(
            Range range,
            NamespaceIdNode<Void> mod) {
        return new ImportNode<>(Meta.empty(range), mod, Lists.immutable.empty());
    }

    public static ImportNode<Void> importNode(
            Range range,
            NamespaceIdNode<Void> mod,
            String symbol) {
        return new ImportNode<>(Meta.empty(range), mod, Lists.immutable.of(symbol));
    }

    public static ImportNode<Void> importNode(
            Range range,
            NamespaceIdNode<Void> mod,
            ImmutableList<String> symbols) {
        return new ImportNode<>(Meta.empty(range), mod, symbols);
    }

    // Top level declarations
    public static DataNode<Void> dataNode(
            Range range,
            String name,
            ImmutableList<TypeVarNode<Void>> typeParams,
            ImmutableList<ConstructorNode<Void>> constructors) {
        return new DataNode<>(Meta.empty(range), name, typeParams, constructors);
    }

    public static <A> DataNode<A> dataNode(
            Meta<A> meta,
            String name,
            ImmutableList<TypeVarNode<A>> typeParams,
            ImmutableList<ConstructorNode<A>> constructors) {
        return new DataNode<>(meta, name, typeParams, constructors);
    }

    public static LetFnNode<Void> letFnNode(
            Range range,
            String name,
            ImmutableList<TypeVarNode<Void>> typeParams,
            ImmutableList<ParamNode<Void>> valueParams,
            TypeNode<Void> returnType,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.empty(range), name, typeParams, valueParams, Optional.ofNullable(returnType), expr);
    }

    public static <A> LetFnNode<A> letFnNode(
            Meta<A> meta,
            String name,
            ImmutableList<TypeVarNode<A>> typeParams,
            ImmutableList<ParamNode<A>> valueParams,
            TypeNode<A> returnType,
            ExprNode<A> expr) {
        return new LetFnNode<>(meta, name, typeParams, valueParams, Optional.ofNullable(returnType), expr);
    }

    public static LetFnNode<Void> letFnNode(
            Range range,
            String name,
            ImmutableList<ParamNode<Void>> valueParams,
            TypeNode<Void> returnType,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.empty(range), name, Lists.immutable.empty(), valueParams, Optional.ofNullable(returnType), expr);
    }

    public static <A> LetFnNode<A> letFnNode(
            Meta<A> meta,
            String name,
            ImmutableList<ParamNode<A>> valueParams,
            TypeNode<A> returnType,
            ExprNode<A> expr) {
        return new LetFnNode<>(meta, name, Lists.immutable.empty(), valueParams, Optional.ofNullable(returnType), expr);
    }

    public static LetFnNode<Void> letFnNode(
            Range range,
            String name,
            ImmutableList<TypeVarNode<Void>> typeParams,
            ImmutableList<ParamNode<Void>> valueParams,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.empty(range), name, typeParams, valueParams, Optional.empty(), expr);
    }

    public static <A> LetFnNode<A> letFnNode(
            Meta<A> meta,
            String name,
            ImmutableList<TypeVarNode<A>> typeParams,
            ImmutableList<ParamNode<A>> valueParams,
            ExprNode<A> expr) {
        return new LetFnNode<>(meta, name, typeParams, valueParams, Optional.empty(), expr);
    }

    public static LetFnNode<Void> letFnNode(
            Range range,
            String name,
            ImmutableList<ParamNode<Void>> valueParams,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.empty(range), name, Lists.immutable.empty(), valueParams, Optional.empty(), expr);
    }

    public static <A> LetFnNode<A> letFnNode(
            Meta<A> meta,
            String name,
            ImmutableList<ParamNode<A>> valueParams,
            ExprNode<A> expr) {
        return new LetFnNode<>(meta, name, Lists.immutable.empty(), valueParams, Optional.empty(), expr);
    }

    public static LetNode<Void> letNode(
            Range range,
            String name,
            TypeNode<Void> type,
            ExprNode<Void> expr) {
        return new LetNode<>(Meta.empty(range), name, Optional.ofNullable(type), expr);
    }

    public static <A> LetNode<A> letNode(
            Meta<A> meta,
            String name,
            TypeNode<Void> type,
            ExprNode<A> expr) {
        return new LetNode<>(meta, name, Optional.ofNullable(type), expr);
    }

    public static LetNode<Void> letNode(
            Range range,
            String name,
            ExprNode<Void> expr) {
        return new LetNode<>(Meta.empty(range), name, Optional.empty(), expr);
    }

    public static <A> LetNode<A> letNode(
            Meta<A> meta,
            String name,
            ExprNode<A> expr) {
        return new LetNode<>(meta, name, Optional.empty(), expr);
    }

    // Data constructors
    public static ConstructorNode<Void> constructorNode(
            Range range,
            String name,
            ImmutableList<ConstructorParamNode<Void>> params,
            Optional<TypeNode<Void>> type) {
        return new ConstructorNode<>(Meta.empty(range), name, params, type);
    }

    public static <A> ConstructorNode<A> constructorNode(
            Meta<A> meta,
            String name,
            ImmutableList<ConstructorParamNode<A>> params,
            Optional<TypeNode<A>> type) {
        return new ConstructorNode<>(meta, name, params, type);
    }

    public static ConstructorParamNode<Void> constructorParamNode(
            Range range,
            String name,
            TypeNode<Void> type) {
        return new ConstructorParamNode<>(Meta.empty(range), name, type);
    }

    public static <A> ConstructorParamNode<A> constructorParamNode(
            Meta<A> meta,
            String name,
            TypeNode<Void> type) {
        return new ConstructorParamNode<>(meta, name, type);
    }

    // Types
    public static TypeLambdaNode<Void> typeLambdaNode(
            Range range,
            ImmutableList<TypeVarNode<Void>> args,
            TypeNode<Void> body) {
        return new TypeLambdaNode<>(Meta.empty(range), args, body);
    }

    public static <A> TypeLambdaNode<A> typeLambdaNode(
            Meta<A> meta,
            ImmutableList<TypeVarNode<A>> args,
            TypeNode<A> body) {
        return new TypeLambdaNode<>(meta, args, body);
    }

    public static FunTypeNode<Void> funTypeNode(
            Range range,
            ImmutableList<TypeNode<Void>> argTypes,
            TypeNode<Void> returnType) {
        return new FunTypeNode<>(Meta.empty(range), argTypes, returnType);
    }

    public static <A> FunTypeNode<A> funTypeNode(
            Meta<A> meta,
            ImmutableList<TypeNode<A>> argTypes,
            TypeNode<A> returnType) {
        return new FunTypeNode<>(meta, argTypes, returnType);
    }

    public static TypeApplyNode<Void> typeApplyNode(
            Range range,
            TypeNode<Void> type,
            ImmutableList<TypeNode<Void>> args) {
        return new TypeApplyNode<>(Meta.empty(range), type, args);
    }

    public static <A> TypeApplyNode<A> typeApplyNode(
            Meta<A> meta,
            TypeNode<A> type,
            ImmutableList<TypeNode<A>> args) {
        return new TypeApplyNode<>(meta, type, args);
    }

    public static TypeReferenceNode<Void> typeRefNode(
            Range range,
            QualifiedIdNode<Void> id) {
        return new TypeReferenceNode<>(Meta.empty(range), id);
    }

    public static <A> TypeReferenceNode<A> typeRefNode(
            Meta<A> meta,
            QualifiedIdNode<A> id) {
        return new TypeReferenceNode<>(meta, id);
    }

    public static TypeReferenceNode<Void> typeRefNode(
            Range range,
            String name) {
        var meta = Meta.empty(range);
        return new TypeReferenceNode<>(meta, idNode(meta, name));
    }

    public static <A> TypeReferenceNode<A> typeRefNode(
            Meta<A> meta,
            String name) {
        return new TypeReferenceNode<>(meta, idNode(meta, name));
    }

    public static ForAllVarNode<Void> forAllVarNode(
            Range range,
            String name) {
        return new ForAllVarNode<>(Meta.empty(range), name);
    }

    public static <A> ForAllVarNode<A> forAllVarNode(
            Meta<A> meta,
            String name) {
        return new ForAllVarNode<>(meta, name);
    }

    public static ExistsVarNode<Void> existsVarNode(
            Range range,
            String name) {
        return new ExistsVarNode<>(Meta.empty(range), name);
    }

    public static <A> ExistsVarNode<A> existsVarNode(
            Meta<A> meta,
            String name) {
        return new ExistsVarNode<>(meta, name);
    }

    // Control structures
    public static BlockNode<Void> blockNode(
        Range range,
        ExprNode<Void> result
    ) {
        return new BlockNode<>(Meta.empty(range), Lists.immutable.empty(), result);
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        ExprNode<A> result
    ) {
        return new BlockNode<>(meta, Lists.immutable.empty(), result);
    }

    public static BlockNode<Void> blockNode(
        Range range,
        ImmutableList<LetNode<Void>> declarations,
        ExprNode<Void> result
    ) {
        return new BlockNode<>(Meta.empty(range), declarations, result);
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        ImmutableList<LetNode<A>> declarations,
        ExprNode<A> result
    ) {
        return new BlockNode<>(meta, declarations, result);
    }

    public static IfNode<Void> ifNode(
            Range range,
            ExprNode<Void> condition,
            ExprNode<Void> consequent,
            ExprNode<Void> alternative) {
        return new IfNode<>(Meta.empty(range), condition, consequent, alternative);
    }

    public static <A> IfNode<A> ifNode(
            Meta<A> meta,
            ExprNode<A> condition,
            ExprNode<A> consequent,
            ExprNode<A> alternative) {
        return new IfNode<>(meta, condition, consequent, alternative);
    }

    public static LambdaNode<Void> lambdaNode(
            Range range,
            ImmutableList<ParamNode<Void>> params,
            ExprNode<Void> body) {
        return new LambdaNode<>(Meta.empty(range), params, body);
    }

    public static <A> LambdaNode<A> lambdaNode(
            Meta<A> meta,
            ImmutableList<ParamNode<A>> params,
            ExprNode<A> body) {
        return new LambdaNode<>(meta, params, body);
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
    public static BooleanNode<Void> boolNode(
            Range range,
            boolean value) {
        return new BooleanNode<>(Meta.empty(range), value);
    }

    public static <A> BooleanNode<A> boolNode(
            Meta<A> meta,
            boolean value) {
        return new BooleanNode<>(meta, value);
    }

    public static CharNode<Void> charNode(
            Range range,
            char value) {
        return new CharNode<>(Meta.empty(range), value);
    }

    public static <A> CharNode<A> charNode(
            Meta<A> meta,
            char value) {
        return new CharNode<>(meta, value);
    }

    public static StringNode<Void> stringNode(
            Range range,
            String value) {
        return new StringNode<>(Meta.empty(range), value);
    }

    public static <A> StringNode<A> stringNode(
            Meta<A> meta,
            String value) {
        return new StringNode<>(meta, value);
    }

    public static IntNode<Void> intNode(
            Range range,
            int value) {
        return new IntNode<>(Meta.empty(range), value);
    }

    public static <A> IntNode<A> intNode(
            Meta<A> meta,
            int value) {
        return new IntNode<>(meta, value);
    }

    public static LongNode<Void> longNode(
            Range range,
            long value) {
        return new LongNode<>(Meta.empty(range), value);
    }

    public static <A> LongNode<A> longNode(
            Meta<A> meta,
            long value) {
        return new LongNode<>(meta, value);
    }

    public static FloatNode<Void> floatNode(
            Range range,
            float value) {
        return new FloatNode<>(Meta.empty(range), value);
    }

    public static <A> FloatNode<A> floatNode(
            Meta<A> meta,
            float value) {
        return new FloatNode<>(meta, value);
    }

    public static DoubleNode<Void> doubleNode(
            Range range,
            double value) {
        return new DoubleNode<>(Meta.empty(range), value);
    }

    public static <A> DoubleNode<A> doubleNode(
            Meta<A> meta,
            double value) {
        return new DoubleNode<>(meta, value);
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            NamespaceIdNode<Void> mod,
            String name) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, idNode(meta, mod, name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            String name) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, idNode(meta, name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            QualifiedIdNode<Void> id) {
        var meta = Meta.empty(range);
        return new ReferenceNode<>(meta, id);
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            NamespaceIdNode<Void> mod,
            String name) {
        return new ReferenceNode<>(meta, idNode(meta, mod, name));
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            String name) {
        return new ReferenceNode<>(meta, idNode(meta, name));
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
        return new ParamNode<>(Meta.empty(range), name, Optional.empty());
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name) {
        return new ParamNode<>(meta, name, Optional.empty());
    }

    public static ParamNode<Void> paramNode(
            Range range,
            String name,
            TypeNode<Void> type) {
        return new ParamNode<>(Meta.empty(range), name, Optional.ofNullable(type));
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name,
            TypeNode<Void> type) {
        return new ParamNode<>(meta, name, Optional.ofNullable(type));
    }

    public static NamespaceIdNode<Void> modIdNode(
            Range range,
            ImmutableList<String> pkg,
            String mod) {
        return new NamespaceIdNode<>(Meta.empty(range), pkg, mod);
    }

    public static NamespaceIdNode<Void> modIdNode(
            Range range,
            String mod) {
        return new NamespaceIdNode<>(Meta.empty(range), Lists.immutable.empty(), mod);
    }

    public static QualifiedIdNode<Void> idNode(
            Range range,
            NamespaceIdNode<Void> mod,
            String name) {
        return new QualifiedIdNode<>(Meta.empty(range), Optional.ofNullable(mod), name);
    }

    public static QualifiedIdNode<Void> idNode(
            Range range,
            String name) {
        return new QualifiedIdNode<>(Meta.empty(range), Optional.empty(), name);
    }

    public static <A> QualifiedIdNode<A> idNode(
            Meta<A> meta,
            NamespaceIdNode<Void> mod,
            String name) {
        return new QualifiedIdNode<>(meta, Optional.ofNullable(mod), name);
    }

    public static <A> QualifiedIdNode<A> idNode(
            Meta<A> meta,
            String name) {
        return new QualifiedIdNode<>(meta, Optional.empty(), name);
    }
}
