package org.mina_lang.common.types;

import com.opencastsoftware.prettier4j.Doc;

public class KindPrinter implements KindFolder<Doc> {

    @Override
    public Doc visitTypeKind(TypeKind typ) {
        return Doc.text("*");
    }

    @Override
    public Doc visitUnsolvedKind(UnsolvedKind unsolved) {
        return Doc.text(unsolved.name());
    }

    @Override
    public Doc visitHigherKind(HigherKind higher) {
        Doc argKinds = higher.argKinds().size() == 1
                ? (higher.argKinds().get(0) instanceof HigherKind h)
                        ? visitKind(h).bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")"))
                        : visitKind(higher.argKinds().get(0))
                : Doc.intersperse(
                        Doc.text(",").append(Doc.lineOrSpace()),
                        higher.argKinds().stream().map(this::visitKind))
                        .bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")"));

        return Doc.group(
                argKinds
                        .appendSpace(Doc.text("->"))
                        .append(Doc.lineOrSpace().append(visitKind(higher.resultKind())).indent(2)));
    }

}
