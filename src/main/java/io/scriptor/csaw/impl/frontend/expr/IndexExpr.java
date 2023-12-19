package io.scriptor.csaw.impl.frontend.expr;

public class IndexExpr extends Expr {

    private final Expr mExpr;
    private final Expr mIndex;

    public IndexExpr(Expr expr, Expr index) {
        this.mExpr = expr;
        this.mIndex = index;
    }

    public synchronized Expr expr() {
        return mExpr;
    }

    public synchronized Expr index() {
        return mIndex;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new IndexExpr(mExpr.makeConstant(), mIndex.makeConstant());
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", mExpr, mIndex);
    }

}
