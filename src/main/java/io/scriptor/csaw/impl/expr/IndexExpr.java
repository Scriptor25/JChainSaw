package io.scriptor.csaw.impl.expr;

public class IndexExpr extends Expr {

    public final Expr expr;
    public final Expr index;

    public IndexExpr(Expr expr, Expr index) {
        this.expr = expr;
        this.index = index;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new IndexExpr(expr.makeConstant(), index.makeConstant());
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", expr, index);
    }

}
