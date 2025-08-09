/*
 * SPDX-FileCopyrightText:  © 2023-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.syntax.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mina_lang.syntax.SyntaxNodes.refNode;

public class FreeLocalVariablesFolder implements MetaNodeFolder<Attributes, List<ReferenceNode<Attributes>>> {
    private final Set<Name> boundVariables = new HashSet<>();

    @Override
    public List<ReferenceNode<Attributes>> visitData(Meta<Attributes> meta, String name,
            List<List<ReferenceNode<Attributes>>> typeParams,
            List<List<ReferenceNode<Attributes>>> constructors) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitConstructor(Meta<Attributes> meta, String name,
            List<List<ReferenceNode<Attributes>>> params,
            Optional<List<ReferenceNode<Attributes>>> type) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitConstructorParam(Meta<Attributes> meta, String name,
            List<ReferenceNode<Attributes>> typeAnnotation) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitQuantifiedType(Meta<Attributes> meta,
                                                                        List<List<ReferenceNode<Attributes>>> args,
                                                                        List<ReferenceNode<Attributes>> body) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitFunType(Meta<Attributes> meta,
            List<List<ReferenceNode<Attributes>>> argTypes,
            List<ReferenceNode<Attributes>> returnType) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitTypeApply(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> type,
            List<List<ReferenceNode<Attributes>>> args) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitTypeReference(Meta<Attributes> meta, QualifiedIdNode id) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitForAllVar(Meta<Attributes> meta, String name) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitExistsVar(Meta<Attributes> meta, String name) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitNamespace(Meta<Attributes> meta, NamespaceIdNode id,
            List<ImportNode> imports, List<List<List<ReferenceNode<Attributes>>>> declarationGroups) {
        return List.of();
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
    public List<ReferenceNode<Attributes>> visitLet(Meta<Attributes> meta, String name,
            Optional<List<ReferenceNode<Attributes>>> type, List<ReferenceNode<Attributes>> expr) {
        return expr;
    }

    @Override
    public void preVisitLetFn(LetFnNode<Attributes> letFn) {
        letFn.valueParams().forEach(param -> boundVariables.add(param.meta().meta().name()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitLetFn(Meta<Attributes> meta, String name,
            List<List<ReferenceNode<Attributes>>> typeParams,
            List<List<ReferenceNode<Attributes>>> valueParams,
            Optional<List<ReferenceNode<Attributes>>> returnType,
            List<ReferenceNode<Attributes>> expr) {
        return expr.stream()
            .filter(ref -> !boundVariables.contains(ref.meta().meta().name()))
            .toList();
    }

    @Override
    public void postVisitLetFn(LetFnNode<Attributes> letFn) {
        letFn.valueParams().forEach(param -> boundVariables.remove(param.meta().meta().name()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitParam(Meta<Attributes> param, String name,
            Optional<List<ReferenceNode<Attributes>>> typeAnnotation) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitBlock(Meta<Attributes> meta,
            List<List<ReferenceNode<Attributes>>> declarations,
            Optional<List<ReferenceNode<Attributes>>> result) {
        var freeInDecls = declarations.stream()
            .flatMap(List::stream)
            .filter(decl -> !boundVariables.contains(decl.meta().meta().name()));

        var freeInResult = result.stream().flatMap(res -> {
            return res.stream()
                .filter(freeVar -> !boundVariables.contains(freeVar.meta().meta().name()));
        });

        return Stream.concat(freeInDecls, freeInResult).toList();
    }

    @Override
    public void postVisitBlock(BlockNode<Attributes> block) {
        block.declarations().forEach(let -> boundVariables.remove(let.meta().meta().name()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitIf(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> condition, List<ReferenceNode<Attributes>> consequent,
            List<ReferenceNode<Attributes>> alternative) {
        return Stream.of(
            condition.stream(),
            consequent.stream(),
            alternative.stream()
        ).flatMap(Function.identity()).toList();
    }

    @Override
    public void preVisitLambda(LambdaNode<Attributes> lambda) {
        lambda.params().forEach(param -> boundVariables.add(param.meta().meta().name()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitLambda(Meta<Attributes> meta,
            List<List<ReferenceNode<Attributes>>> params,
            List<ReferenceNode<Attributes>> body) {
        return body.stream().filter(ref -> !boundVariables.contains(ref.meta().meta().name())).toList();
    }

    @Override
    public void postVisitLambda(LambdaNode<Attributes> lambda) {
        lambda.params().forEach(param -> boundVariables.remove(param.meta().meta().name()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitMatch(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> scrutinee,
            List<List<ReferenceNode<Attributes>>> cases) {
        return Stream.concat(
            scrutinee.stream(),
            cases.stream().flatMap(List::stream)
        ).toList();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitApply(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> expr,
            List<List<ReferenceNode<Attributes>>> args) {
        return Stream.concat(
            expr.stream(),
            args.stream().flatMap(List::stream)
        ).toList();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitSelect(Meta<Attributes> meta,
           List<ReferenceNode<Attributes>> receiver,
           List<ReferenceNode<Attributes>> selection) {
        return Stream.concat(receiver.stream(), selection.stream()).toList();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitUnaryOp(Meta<Attributes> meta, UnaryOp operator, List<ReferenceNode<Attributes>> operand) {
        return operand;
    }

    @Override
    public List<ReferenceNode<Attributes>> visitBinaryOp(Meta<Attributes> meta, List<ReferenceNode<Attributes>> leftOperand, BinaryOp operator, List<ReferenceNode<Attributes>> rightOperand) {
        return Stream.concat(leftOperand.stream(), rightOperand.stream()).toList();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitReference(Meta<Attributes> meta, QualifiedIdNode id) {
        // Top-level definitions can remain free in lambdas
        if (meta.meta().name() instanceof LocalName) {
            return List.of(refNode(meta, id));
        } else {
            return List.of();
        }
    }

    @Override
    public List<ReferenceNode<Attributes>> visitBoolean(Meta<Attributes> meta, boolean value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitChar(Meta<Attributes> meta, char value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitString(Meta<Attributes> meta, String value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitInt(Meta<Attributes> meta, int value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitLong(Meta<Attributes> meta, long value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitFloat(Meta<Attributes> meta, float value) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitDouble(Meta<Attributes> meta, double value) {
        return List.of();
    }

    @Override
    public void preVisitCase(CaseNode<Attributes> cse) {
        boundVariables.addAll(cse.pattern().accept(new PatternVariablesFolder()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitCase(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> pattern, List<ReferenceNode<Attributes>> consequent) {
        return consequent.stream().filter(freeVar -> !boundVariables.contains(freeVar.meta().meta().name())).toList();
    }

    @Override
    public void postVisitCase(CaseNode<Attributes> cse) {
        boundVariables.removeAll(cse.pattern().accept(new PatternVariablesFolder()));
    }

    @Override
    public List<ReferenceNode<Attributes>> visitAliasPattern(Meta<Attributes> meta, String alias,
            List<ReferenceNode<Attributes>> pattern) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitConstructorPattern(Meta<Attributes> meta, QualifiedIdNode id,
            List<List<ReferenceNode<Attributes>>> fields) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitFieldPattern(Meta<Attributes> meta, String field,
            List<ReferenceNode<Attributes>> pattern) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitIdPattern(Meta<Attributes> meta, String name) {
        return List.of();
    }

    @Override
    public List<ReferenceNode<Attributes>> visitLiteralPattern(Meta<Attributes> meta,
            List<ReferenceNode<Attributes>> literal) {
        return List.of();
    }

}
