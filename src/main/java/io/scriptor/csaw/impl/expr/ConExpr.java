package io.scriptor.csaw.impl.expr;

public class ConExpr extends Expr {

    public Expr condition;
    public Expr thenExpr;
    public Expr elseExpr;

    @Override
    public String toString() {
        return String.format("%s ? %s : %s", condition, thenExpr, elseExpr);
    }

    @Override
    public boolean isConstant() {
        return condition.isConstant();
    }
}
