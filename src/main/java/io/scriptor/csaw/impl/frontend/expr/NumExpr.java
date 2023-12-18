package io.scriptor.csaw.impl.frontend.expr;

public class NumExpr extends Expr {

    public final double value;

    public NumExpr(String value, int radix) {
        if (radix == 10)
            this.value = Double.parseDouble(value);
        else
            this.value = Long.parseLong(value, radix);
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