/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.mina_lang.common.Meta;

public sealed interface MetaNode<A> extends SyntaxNode permits NamespaceNode, DeclarationNode, ConstructorNode, ConstructorParamNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, TypeNode {
    public Meta<A> meta();

    @Override
    default Range range() {
        return meta().range();
    }

    <B> B accept(MetaNodeFolder<A, B> visitor);

    <B> MetaNode<B> accept(MetaNodeTransformer<A, B> transformer);
}
