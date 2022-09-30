package org.mina_lang.syntax;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.mina_lang.common.Range;

public class TracingSyntaxNodeVisitor implements SyntaxNodeVisitor {
    private MutableList<Entry> entries = Lists.mutable.empty();

    public List<Entry> getEntries() {
        return entries;
    }

    record Entry(Class<?> nodeClass, Range range) {

    }

    @Override
    public void visit(SyntaxNode node) {
        // do nothing - we're testing that the type-specific visitor methods are called
    }

    // Namespaces
    @Override
    public void visitNamespace(NamespaceNode<?> ns) {
        entries.add(new Entry(ns.getClass(), ns.range()));
    }

    // Imports
    @Override
    public void visitImport(ImportNode imp) {
        entries.add(new Entry(imp.getClass(), imp.range()));
    }

    // Types
    @Override
    public void visitTypeLambda(TypeLambdaNode<?> lam) {
        entries.add(new Entry(lam.getClass(), lam.range()));
    }

    @Override
    public void visitFunType(FunTypeNode<?> funTyp) {
        entries.add(new Entry(funTyp.getClass(), funTyp.range()));
    }

    @Override
    public void visitTypeApply(TypeApplyNode<?> tyApp) {
        entries.add(new Entry(tyApp.getClass(), tyApp.range()));
    }

    @Override
    public void visitForAllVar(ForAllVarNode<?> forAll) {
        entries.add(new Entry(forAll.getClass(), forAll.range()));
    }

    @Override
    public void visitExistsVar(ExistsVarNode<?> exists) {
        entries.add(new Entry(exists.getClass(), exists.range()));
    }

    @Override
    public void visitTypeReference(TypeReferenceNode<?> tyRef) {
        entries.add(new Entry(tyRef.getClass(), tyRef.range()));
    }

    // Declarations
    @Override
    public void visitData(DataNode<?> data) {
        entries.add(new Entry(data.getClass(), data.range()));
    }

    @Override
    public void visitConstructor(ConstructorNode<?> constr) {
        entries.add(new Entry(constr.getClass(), constr.range()));
    }

    @Override
    public void visitConstructorParam(ConstructorParamNode<?> constrParam) {
        entries.add(new Entry(constrParam.getClass(), constrParam.range()));
    }

    @Override
    public void visitLet(LetNode<?> let) {
        entries.add(new Entry(let.getClass(), let.range()));
    }

    @Override
    public void visitLetFn(LetFnNode<?> letFn) {
        entries.add(new Entry(letFn.getClass(), letFn.range()));
    }

    // Expressions
    @Override
    public void visitBlock(BlockNode<?> block) {
        entries.add(new Entry(block.getClass(), block.range()));
    }

    @Override
    public void visitIf(IfNode<?> ifExpr) {
        entries.add(new Entry(ifExpr.getClass(), ifExpr.range()));
    }

    @Override
    public void visitLambda(LambdaNode<?> lambda) {
        entries.add(new Entry(lambda.getClass(), lambda.range()));
    }

    @Override
    public void visitParam(ParamNode<?> param) {
        entries.add(new Entry(param.getClass(), param.range()));
    }

    @Override
    public void visitMatch(MatchNode<?> match) {
        entries.add(new Entry(match.getClass(), match.range()));
    }

    @Override
    public void visitApply(ApplyNode<?> apply) {
        entries.add(new Entry(apply.getClass(), apply.range()));
    }

    @Override
    public void visitBoolean(BooleanNode<?> bool) {
        entries.add(new Entry(bool.getClass(), bool.range()));
    }

    @Override
    public void visitInt(IntNode<?> intgr) {
        entries.add(new Entry(intgr.getClass(), intgr.range()));
    }

    @Override
    public void visitLong(LongNode<?> lng) {
        entries.add(new Entry(lng.getClass(), lng.range()));
    }

    @Override
    public void visitFloat(FloatNode<?> flt) {
        entries.add(new Entry(flt.getClass(), flt.range()));
    }

    @Override
    public void visitDouble(DoubleNode<?> dbl) {
        entries.add(new Entry(dbl.getClass(), dbl.range()));
    }

    @Override
    public void visitChar(CharNode<?> chr) {
        entries.add(new Entry(chr.getClass(), chr.range()));
    }

    @Override
    public void visitString(StringNode<?> str) {
        entries.add(new Entry(str.getClass(), str.range()));
    }

    @Override
    public void visitReference(ReferenceNode<?> ref) {
        entries.add(new Entry(ref.getClass(), ref.range()));
    }

    // Cases and patterns
    @Override
    public void visitCase(CaseNode<?> cse) {
        entries.add(new Entry(cse.getClass(), cse.range()));
    }

    @Override
    public void visitConstructorPattern(ConstructorPatternNode<?> constrPat) {
        entries.add(new Entry(constrPat.getClass(), constrPat.range()));
    }

    @Override
    public void visitFieldPattern(FieldPatternNode<?> fieldPat) {
        entries.add(new Entry(fieldPat.getClass(), fieldPat.range()));
    }

    @Override
    public void visitIdPattern(IdPatternNode<?> idPat) {
        entries.add(new Entry(idPat.getClass(), idPat.range()));
    }

    @Override
    public void visitLiteralPattern(LiteralPatternNode<?> litPat) {
        entries.add(new Entry(litPat.getClass(), litPat.range()));
    }

    // Identifiers
    @Override
    public void visitNamespaceId(NamespaceIdNode id) {
        entries.add(new Entry(id.getClass(), id.range()));
    }

    @Override
    public void visitQualifiedId(QualifiedIdNode id) {
        entries.add(new Entry(id.getClass(), id.range()));
    }
}
