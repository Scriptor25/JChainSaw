package io.scriptor.csaw.impl.expr;

public class UnExpr extends Expr {

    public String operator;
    public Expr value;

    public UnExpr(String op, Expr val) {
        operator = op;
        value = val;
    }

    @Override
    public String toString() {
        return String.format("%s%s", operator, value);
    }

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }
}
