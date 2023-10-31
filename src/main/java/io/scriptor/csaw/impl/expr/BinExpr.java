package io.scriptor.csaw.impl.expr;

public class BinExpr extends Expr {

    public Expr left;
    public String operator;
    public Expr right;

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator, right);
    }
}
