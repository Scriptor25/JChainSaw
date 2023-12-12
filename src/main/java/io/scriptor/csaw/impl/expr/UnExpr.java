package io.scriptor.csaw.impl.expr;

public class UnExpr extends Expr {

    public final String operator;
    public final Expr value;

    public UnExpr(String operator, Expr value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s%s", operator, value);
    }
}
