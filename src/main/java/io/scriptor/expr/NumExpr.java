package io.scriptor.expr;

public class NumExpr extends Expr {

    public double value;

    public NumExpr(String value) {
        this.value = Double.parseDouble(value);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}