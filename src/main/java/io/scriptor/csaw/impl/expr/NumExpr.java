package io.scriptor.csaw.impl.expr;

public class NumExpr extends Expr {

    public final double value;

    public NumExpr(String value) {
        this.value = Double.parseDouble(value);
    }

    public NumExpr(double value) {
        this.value = value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Expr makeConstant() {
        return new ConstExpr(this);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}