package io.scriptor.csaw.impl.frontend.expr;

public class UnExpr extends Expr {

    public final String operator;
    public final Expr value;

    public UnExpr(String operator, Expr value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr.make(new UnExpr(operator, value.makeConstant()));
    }

    @Override
    public String toString() {
        return String.format("%s%s", operator, value);
    }
}
