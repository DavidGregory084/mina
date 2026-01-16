/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

import com.opencastsoftware.prettier4j.Doc;

public class NamePrinter implements NameFolder<Doc> {
    private static final Doc QUESTION = Doc.text("?");

    private final boolean showIndices;

    public NamePrinter() {
        this(false);
    }

    public NamePrinter(boolean showIndices) {
        this.showIndices = showIndices;
    }

    @Override
    public Doc visitNameless(Nameless nameless) {
        return Doc.empty();
    }

    @Override
    public Doc visitBuiltInName(BuiltInName builtIn) {
        return Doc.text(builtIn.name());
    }

    @Override
    public Doc visitNamespaceName(NamespaceName namespace) {
        return Doc.text(namespace.canonicalName());
    }

    @Override
    public Doc visitLocalName(LocalName local) {
        return Doc.text(
            showIndices
                ? local.name() + "@" + local.index()
                : local.name()
        );
    }

    @Override
    public Doc visitFieldName(FieldName field) {
        return Doc.text(field.name());
    }

    @Override
    public Doc visitSyntheticName(SyntheticName synthetic) {
        return Doc.text(synthetic.localName());
    }

    @Override
    public Doc visitLetName(LetName let) {
        return Doc.text(let.localName());
    }

    @Override
    public Doc visitConstructorName(ConstructorName constructor) {
        return Doc.text(constructor.localName());
    }

    @Override
    public Doc visitDataName(DataName data) {
        return Doc.text(data.localName());
    }

    @Override
    public Doc visitExistsVarName(ExistsVarName existsVar) {
        return QUESTION.append(Doc.text(existsVar.name()));
    }

    @Override
    public Doc visitForAllVarName(ForAllVarName forAllVar) {
        return Doc.text(forAllVar.name());
    }
}
