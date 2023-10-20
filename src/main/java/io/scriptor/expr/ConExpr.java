package io.scriptor.expr;

public class ConExpr extends Expr {

    public Expr condition;
    public Expr thenExpr;
    public Expr elseExpr;

    @Override
    public String toString() {
        return String.format("%s ? %s : %s", condition, thenExpr, elseExpr);
    }

}
