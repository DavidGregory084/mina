package org.mina_lang.syntax;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;

import java.util.Optional;

public class SyntaxNodes {
    // Namespace
    public static NamespaceNode<Void> namespaceNode(
            Range range,
            NamespaceIdNode id,
            ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<Void>> declarations) {
        return new NamespaceNode<>(Meta.of(range), id, imports, Lists.immutable.of(declarations));
    }

    public static <A> NamespaceNode<A> namespaceNode(
            Meta<A> meta,
            NamespaceIdNode id,
            ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<A>> declarations) {
        return new NamespaceNode<>(meta, id, imports, Lists.immutable.of(declarations));
    }

    public static ImportNode importNode(
            Range range,
            NamespaceIdNode namespace) {
        return new ImportNode(range, namespace, Lists.immutable.empty());
    }

    public static ImportNode importNode(
            Range range,
            NamespaceIdNode namespace,
            ImportSymbolNode symbol) {
        return new ImportNode(range, namespace, Lists.immutable.of(symbol));
    }

    public static ImportNode importNode(
            Range range,
            NamespaceIdNode namespace,
            ImmutableList<ImportSymbolNode> symbols) {
        return new ImportNode(range, namespace, symbols);
    }

    public static ImportSymbolNode importSymbolNode(Range range, String symbol) {
        return new ImportSymbolNode(range, symbol);
    }

    // Top level declarations
    public static DataNode<Void> dataNode(
            Range range,
            String name,
            ImmutableList<TypeVarNode<Void>> typeParams,
            ImmutableList<ConstructorNode<Void>> constructors) {
        return new DataNode<>(Meta.of(range), name, typeParams, constructors);
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
            Optional<TypeNode<Void>> returnType,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.of(range), name, typeParams, valueParams, returnType, expr);
    }

    public static <A> LetFnNode<A> letFnNode(
            Meta<A> meta,
            String name,
            ImmutableList<TypeVarNode<A>> typeParams,
            ImmutableList<ParamNode<A>> valueParams,
            Optional<TypeNode<A>> returnType,
            ExprNode<A> expr) {
        return new LetFnNode<>(meta, name, typeParams, valueParams, returnType, expr);
    }

    public static LetFnNode<Void> letFnNode(
            Range range,
            String name,
            ImmutableList<TypeVarNode<Void>> typeParams,
            ImmutableList<ParamNode<Void>> valueParams,
            TypeNode<Void> returnType,
            ExprNode<Void> expr) {
        return new LetFnNode<>(Meta.of(range), name, typeParams, valueParams, Optional.ofNullable(returnType), expr);
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
        return new LetFnNode<>(Meta.of(range), name, Lists.immutable.empty(), valueParams, Optional.ofNullable(returnType), expr);
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
        return new LetFnNode<>(Meta.of(range), name, typeParams, valueParams, Optional.empty(), expr);
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
        return new LetFnNode<>(Meta.of(range), name, Lists.immutable.empty(), valueParams, Optional.empty(), expr);
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
            Optional<TypeNode<Void>> type,
            ExprNode<Void> expr) {
        return new LetNode<>(Meta.of(range), name, type, expr);
    }

    public static <A> LetNode<A> letNode(
            Meta<A> meta,
            String name,
            Optional<TypeNode<A>> type,
            ExprNode<A> expr) {
        return new LetNode<>(meta, name, type, expr);
    }

    public static LetNode<Void> letNode(
            Range range,
            String name,
            TypeNode<Void> type,
            ExprNode<Void> expr) {
        return new LetNode<>(Meta.of(range), name, Optional.ofNullable(type), expr);
    }

    public static <A> LetNode<A> letNode(
            Meta<A> meta,
            String name,
            TypeNode<A> type,
            ExprNode<A> expr) {
        return new LetNode<>(meta, name, Optional.ofNullable(type), expr);
    }

    public static LetNode<Void> letNode(
            Range range,
            String name,
            ExprNode<Void> expr) {
        return new LetNode<>(Meta.of(range), name, Optional.empty(), expr);
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
        return new ConstructorNode<>(Meta.of(range), name, params, type);
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
        return new ConstructorParamNode<>(Meta.of(range), name, type);
    }

    public static <A> ConstructorParamNode<A> constructorParamNode(
            Meta<A> meta,
            String name,
            TypeNode<A> type) {
        return new ConstructorParamNode<>(meta, name, type);
    }

    // Types
    public static TypeLambdaNode<Void> typeLambdaNode(
            Range range,
            ImmutableList<TypeVarNode<Void>> args,
            TypeNode<Void> body) {
        return new TypeLambdaNode<>(Meta.of(range), args, body);
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
        return new FunTypeNode<>(Meta.of(range), argTypes, returnType);
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
        return new TypeApplyNode<>(Meta.of(range), type, args);
    }

    public static <A> TypeApplyNode<A> typeApplyNode(
            Meta<A> meta,
            TypeNode<A> type,
            ImmutableList<TypeNode<A>> args) {
        return new TypeApplyNode<>(meta, type, args);
    }

    public static TypeReferenceNode<Void> typeRefNode(
            Range range,
            QualifiedIdNode id) {
        return new TypeReferenceNode<>(Meta.of(range), id);
    }

    public static <A> TypeReferenceNode<A> typeRefNode(
            Meta<A> meta,
            QualifiedIdNode id) {
        return new TypeReferenceNode<>(meta, id);
    }

    public static TypeReferenceNode<Void> typeRefNode(
            Range range,
            String name) {
        var meta = Meta.of(range);
        return new TypeReferenceNode<>(meta, idNode(range, name));
    }

    public static <A> TypeReferenceNode<A> typeRefNode(
            Meta<A> meta,
            String name) {
        return new TypeReferenceNode<>(meta, idNode(meta.range(), name));
    }

    public static ForAllVarNode<Void> forAllVarNode(
            Range range,
            String name) {
        return new ForAllVarNode<>(Meta.of(range), name);
    }

    public static <A> ForAllVarNode<A> forAllVarNode(
            Meta<A> meta,
            String name) {
        return new ForAllVarNode<>(meta, name);
    }

    public static ExistsVarNode<Void> existsVarNode(
            Range range,
            String name) {
        return new ExistsVarNode<>(Meta.of(range), name);
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
        return new BlockNode<>(Meta.of(range), Lists.immutable.empty(), Optional.of(result));
    }

    public static BlockNode<Void> blockNode(
        Range range,
        Optional<ExprNode<Void>> result
    ) {
        return new BlockNode<>(Meta.of(range), Lists.immutable.empty(), result);
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        ExprNode<A> result
    ) {
        return new BlockNode<>(meta, Lists.immutable.empty(), Optional.of(result));
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        Optional<ExprNode<A>> result
    ) {
        return new BlockNode<>(meta, Lists.immutable.empty(), result);
    }

    public static BlockNode<Void> blockNode(
        Range range,
        ImmutableList<LetNode<Void>> declarations,
        ExprNode<Void> result
    ) {
        return new BlockNode<>(Meta.of(range), declarations, Optional.of(result));
    }

    public static BlockNode<Void> blockNode(
        Range range,
        ImmutableList<LetNode<Void>> declarations,
        Optional<ExprNode<Void>> result
    ) {
        return new BlockNode<>(Meta.of(range), declarations, result);
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        ImmutableList<LetNode<A>> declarations,
        ExprNode<A> result
    ) {
        return new BlockNode<>(meta, declarations, Optional.of(result));
    }

    public static <A> BlockNode<A> blockNode(
        Meta<A> meta,
        ImmutableList<LetNode<A>> declarations,
        Optional<ExprNode<A>> result
    ) {
        return new BlockNode<>(meta, declarations, result);
    }

    public static IfNode<Void> ifNode(
            Range range,
            ExprNode<Void> condition,
            ExprNode<Void> consequent,
            ExprNode<Void> alternative) {
        return new IfNode<>(Meta.of(range), condition, consequent, alternative);
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
        return new LambdaNode<>(Meta.of(range), params, body);
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
        return new MatchNode<>(Meta.of(range), scrutinee, cases);
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
        return new ApplyNode<>(Meta.of(range), expr, args);
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
        return new BooleanNode<>(Meta.of(range), value);
    }

    public static <A> BooleanNode<A> boolNode(
            Meta<A> meta,
            boolean value) {
        return new BooleanNode<>(meta, value);
    }

    public static CharNode<Void> charNode(
            Range range,
            char value) {
        return new CharNode<>(Meta.of(range), value);
    }

    public static <A> CharNode<A> charNode(
            Meta<A> meta,
            char value) {
        return new CharNode<>(meta, value);
    }

    public static StringNode<Void> stringNode(
            Range range,
            String value) {
        return new StringNode<>(Meta.of(range), value);
    }

    public static <A> StringNode<A> stringNode(
            Meta<A> meta,
            String value) {
        return new StringNode<>(meta, value);
    }

    public static IntNode<Void> intNode(
            Range range,
            int value) {
        return new IntNode<>(Meta.of(range), value);
    }

    public static <A> IntNode<A> intNode(
            Meta<A> meta,
            int value) {
        return new IntNode<>(meta, value);
    }

    public static LongNode<Void> longNode(
            Range range,
            long value) {
        return new LongNode<>(Meta.of(range), value);
    }

    public static <A> LongNode<A> longNode(
            Meta<A> meta,
            long value) {
        return new LongNode<>(meta, value);
    }

    public static FloatNode<Void> floatNode(
            Range range,
            float value) {
        return new FloatNode<>(Meta.of(range), value);
    }

    public static <A> FloatNode<A> floatNode(
            Meta<A> meta,
            float value) {
        return new FloatNode<>(meta, value);
    }

    public static DoubleNode<Void> doubleNode(
            Range range,
            double value) {
        return new DoubleNode<>(Meta.of(range), value);
    }

    public static <A> DoubleNode<A> doubleNode(
            Meta<A> meta,
            double value) {
        return new DoubleNode<>(meta, value);
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            NamespaceIdNode namespace,
            String name) {
        var meta = Meta.of(range);
        return new ReferenceNode<>(meta, idNode(range, namespace, name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            String name) {
        var meta = Meta.of(range);
        return new ReferenceNode<>(meta, idNode(range, name));
    }

    public static ReferenceNode<Void> refNode(
            Range range,
            QualifiedIdNode id) {
        var meta = Meta.of(range);
        return new ReferenceNode<>(meta, id);
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            NamespaceIdNode namespace,
            String name) {
        return new ReferenceNode<>(meta, idNode(meta.range(), namespace, name));
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            String name) {
        return new ReferenceNode<>(meta, idNode(meta.range(), name));
    }

    public static <A> ReferenceNode<A> refNode(
            Meta<A> meta,
            QualifiedIdNode id) {
        return new ReferenceNode<>(meta, id);
    }

    // Patterns
    public static CaseNode<Void> caseNode(
            Range range,
            PatternNode<Void> pattern,
            ExprNode<Void> consequent) {
        return new CaseNode<>(Meta.of(range), pattern, consequent);
    }

    public static <A> CaseNode<A> caseNode(
            Meta<A> meta,
            PatternNode<A> pattern,
            ExprNode<A> consequent) {
        return new CaseNode<>(meta, pattern, consequent);
    }

    public static AliasPatternNode<Void> aliasPatternNode(
            Range range,
            String alias,
            PatternNode<Void> pattern) {
        return new AliasPatternNode<>(Meta.of(range), alias, pattern);
    }

    public static <A> AliasPatternNode<A> aliasPatternNode(
            Meta<A> meta,
            String alias,
            PatternNode<A> pattern) {
        return new AliasPatternNode<>(meta, alias, pattern);
    }

    public static IdPatternNode<Void> idPatternNode(
            Range range,
            String name) {
        return new IdPatternNode<>(Meta.of(range), name);
    }

    public static <A> IdPatternNode<A> idPatternNode(
            Meta<A> meta,
            String name) {
        return new IdPatternNode<>(meta, name);
    }

    public static LiteralPatternNode<Void> literalPatternNode(
            Range range,
            LiteralNode<Void> literal) {
        return new LiteralPatternNode<>(Meta.of(range), literal);
    }

    public static <A> LiteralPatternNode<A> literalPatternNode(
            Meta<A> meta,
            LiteralNode<A> literal) {
        return new LiteralPatternNode<>(meta, literal);
    }

    public static ConstructorPatternNode<Void> constructorPatternNode(
            Range range,
            QualifiedIdNode id,
            ImmutableList<FieldPatternNode<Void>> fields) {
        return new ConstructorPatternNode<>(Meta.of(range), id, fields);
    }

    public static <A> ConstructorPatternNode<A> constructorPatternNode(
            Meta<A> meta,
            QualifiedIdNode id,
            ImmutableList<FieldPatternNode<A>> fields) {
        return new ConstructorPatternNode<>(meta, id, fields);
    }

    public static FieldPatternNode<Void> fieldPatternNode(
            Range range,
            String field,
            Optional<PatternNode<Void>> pattern) {
        return new FieldPatternNode<>(Meta.of(range), field, pattern);
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
        return new ParamNode<>(Meta.of(range), name, Optional.empty());
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name) {
        return new ParamNode<>(meta, name, Optional.empty());
    }

    public static ParamNode<Void> paramNode(
            Range range,
            String name,
            Optional<TypeNode<Void>> type) {
        return new ParamNode<>(Meta.of(range), name, type);
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name,
            Optional<TypeNode<A>> type) {
        return new ParamNode<>(meta, name, type);
    }

    public static ParamNode<Void> paramNode(
            Range range,
            String name,
            TypeNode<Void> type) {
        return new ParamNode<>(Meta.of(range), name, Optional.ofNullable(type));
    }

    public static <A> ParamNode<A> paramNode(
            Meta<A> meta,
            String name,
            TypeNode<A> type) {
        return new ParamNode<>(meta, name, Optional.ofNullable(type));
    }

    public static NamespaceIdNode nsIdNode(
            Range range,
            ImmutableList<String> pkg,
            String ns) {
        return new NamespaceIdNode(range, pkg, ns);
    }

    public static NamespaceIdNode nsIdNode(
            Range range,
            String ns) {
        return new NamespaceIdNode(range, Lists.immutable.empty(), ns);
    }

    public static QualifiedIdNode idNode(
            Range range,
            Optional<NamespaceIdNode> namespace,
            String name) {
        return new QualifiedIdNode(range, namespace, name);
    }

    public static QualifiedIdNode idNode(
            Range range,
            NamespaceIdNode namespace,
            String name) {
        return new QualifiedIdNode(range, Optional.ofNullable(namespace), name);
    }

    public static QualifiedIdNode idNode(
            Range range,
            String name) {
        return new QualifiedIdNode(range, Optional.empty(), name);
    }
}
