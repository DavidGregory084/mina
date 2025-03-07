/*
 * SPDX-FileCopyrightText:  © 2023-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.syntax.*;

import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.refNode;

public class FreeLocalVariablesFolder implements MetaNodeFolder<Attributes, ImmutableList<ReferenceNode<Attributes>>> {
    MutableSet<Name> boundVariables = Sets.mutable.empty();

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitData(Meta<Attributes> meta, String name,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> typeParams,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> constructors) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitConstructor(Meta<Attributes> meta, String name,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> params,
            Optional<ImmutableList<ReferenceNode<Attributes>>> type) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitConstructorParam(Meta<Attributes> meta, String name,
            ImmutableList<ReferenceNode<Attributes>> typeAnnotation) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitQuantifiedType(Meta<Attributes> meta,
                                                                        ImmutableList<ImmutableList<ReferenceNode<Attributes>>> args,
                                                                        ImmutableList<ReferenceNode<Attributes>> body) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitFunType(Meta<Attributes> meta,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> argTypes,
            ImmutableList<ReferenceNode<Attributes>> returnType) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitTypeApply(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> type,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> args) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitTypeReference(Meta<Attributes> meta, QualifiedIdNode id) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitForAllVar(Meta<Attributes> meta, String name) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitExistsVar(Meta<Attributes> meta, String name) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitNamespace(Meta<Attributes> meta, NamespaceIdNode id,
            ImmutableList<ImportNode> imports, ImmutableList<ImmutableList<ImmutableList<ReferenceNode<Attributes>>>> declarationGroups) {
        return Lists.immutable.empty();
    }

    @Override
    public void preVisitLet(LetNode<Attributes> let) {
        // Local let bindings are scoped to a block and can shadow outer variables.
        // They are only visible to later bindings.
        if (let.meta().meta().name() instanceof LocalName localName) {
            boundVariables.add(localName);
        }
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitLet(Meta<Attributes> meta, String name,
            Optional<ImmutableList<ReferenceNode<Attributes>>> type, ImmutableList<ReferenceNode<Attributes>> expr) {
        return expr;
    }

    @Override
    public void preVisitLetFn(LetFnNode<Attributes> letFn) {
        var paramNames = letFn.valueParams().collect(param -> param.meta().meta().name());
        boundVariables.addAllIterable(paramNames);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitLetFn(Meta<Attributes> meta, String name,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> typeParams,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> valueParams,
            Optional<ImmutableList<ReferenceNode<Attributes>>> returnType,
            ImmutableList<ReferenceNode<Attributes>> expr) {
        return expr.reject(ref -> boundVariables.contains(ref.meta().meta().name()));
    }

    @Override
    public void postVisitLetFn(LetFnNode<Attributes> letFn) {
        var paramNames = letFn.valueParams().collect(param -> param.meta().meta().name());
        boundVariables.removeAllIterable(paramNames);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitParam(Meta<Attributes> param, String name,
            Optional<ImmutableList<ReferenceNode<Attributes>>> typeAnnotation) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitBlock(Meta<Attributes> meta,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> declarations,
            Optional<ImmutableList<ReferenceNode<Attributes>>> result) {
        var freeInDecls = declarations
            .flatCollect(decls -> decls)
            .reject(decl -> boundVariables.contains(decl.meta().meta().name()));

        var freeInResult = result.map(res -> {
            return res.reject(freeVar -> boundVariables.contains(freeVar.meta().meta().name()));
        }).orElseGet(Lists.immutable::empty);

        return freeInDecls.newWithAll(freeInResult);
    }

    @Override
    public void postVisitBlock(BlockNode<Attributes> block) {
        var declNames = block.declarations().collect(let -> let.meta().meta().name());
        boundVariables.removeAllIterable(declNames);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitIf(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> condition, ImmutableList<ReferenceNode<Attributes>> consequent,
            ImmutableList<ReferenceNode<Attributes>> alternative) {
        return condition
                .newWithAll(consequent)
                .newWithAll(alternative);
    }

    @Override
    public void preVisitLambda(LambdaNode<Attributes> lambda) {
        var paramNames = lambda.params().collect(param -> param.meta().meta().name());
        boundVariables.addAllIterable(paramNames);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitLambda(Meta<Attributes> meta,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> params,
            ImmutableList<ReferenceNode<Attributes>> body) {
        return body.reject(ref -> boundVariables.contains(ref.meta().meta().name()));
    }

    @Override
    public void postVisitLambda(LambdaNode<Attributes> lambda) {
        var paramNames = lambda.params().collect(param -> param.meta().meta().name());
        boundVariables.removeAllIterable(paramNames);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitMatch(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> scrutinee,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> cases) {
        return scrutinee.newWithAll(cases.flatCollect(cse -> cse));
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitApply(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> expr,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> args) {
        return expr.newWithAll(args.flatCollect(arg -> arg));
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitSelect(Meta<Attributes> meta,
           ImmutableList<ReferenceNode<Attributes>> receiver,
           ImmutableList<ReferenceNode<Attributes>> selection) {
        return receiver.newWithAll(selection);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitUnaryOp(Meta<Attributes> meta, UnaryOp operator, ImmutableList<ReferenceNode<Attributes>> operand) {
        return operand;
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitBinaryOp(Meta<Attributes> meta, ImmutableList<ReferenceNode<Attributes>> leftOperand, BinaryOp operator, ImmutableList<ReferenceNode<Attributes>> rightOperand) {
        return leftOperand.newWithAll(rightOperand);
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitReference(Meta<Attributes> meta, QualifiedIdNode id) {
        // Top-level definitions can remain free in lambdas
        if (meta.meta().name() instanceof LocalName) {
            return Lists.immutable.of(refNode(meta, id));
        } else {
            return Lists.immutable.empty();
        }
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitBoolean(Meta<Attributes> meta, boolean value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitChar(Meta<Attributes> meta, char value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitString(Meta<Attributes> meta, String value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitInt(Meta<Attributes> meta, int value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitLong(Meta<Attributes> meta, long value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitFloat(Meta<Attributes> meta, float value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitDouble(Meta<Attributes> meta, double value) {
        return Lists.immutable.empty();
    }

    @Override
    public void preVisitCase(CaseNode<Attributes> cse) {
        boundVariables.addAllIterable(cse.pattern().accept(new PatternVariablesFolder()));
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitCase(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> pattern, ImmutableList<ReferenceNode<Attributes>> consequent) {
        return consequent.reject(freeVar -> boundVariables.contains(freeVar.meta().meta().name()));
    }

    @Override
    public void postVisitCase(CaseNode<Attributes> cse) {
        boundVariables.removeAllIterable(cse.pattern().accept(new PatternVariablesFolder()));
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitAliasPattern(Meta<Attributes> meta, String alias,
            ImmutableList<ReferenceNode<Attributes>> pattern) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitConstructorPattern(Meta<Attributes> meta, QualifiedIdNode id,
            ImmutableList<ImmutableList<ReferenceNode<Attributes>>> fields) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitFieldPattern(Meta<Attributes> meta, String field,
            Optional<ImmutableList<ReferenceNode<Attributes>>> pattern) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitIdPattern(Meta<Attributes> meta, String name) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<ReferenceNode<Attributes>> visitLiteralPattern(Meta<Attributes> meta,
            ImmutableList<ReferenceNode<Attributes>> literal) {
        return Lists.immutable.empty();
    }

}
