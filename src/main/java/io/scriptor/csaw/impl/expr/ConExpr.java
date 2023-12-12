package io.scriptor.csaw.impl.expr;

public class ConExpr extends Expr {

    public final Expr condition;
    public final Expr thenExpr;
    public final Expr elseExpr;

    public ConExpr(Expr condition, Expr thenExpr, Expr elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public String toString() {
        return String.format("%s ? %s : %s", condition, thenExpr, elseExpr);
    }
}
