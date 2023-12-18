package io.scriptor.csaw.impl.frontend.expr;

public class StrExpr extends Expr {

    public final String value;

    public StrExpr(String value) {
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
        return String.format("\"%s\"", value);
    }
}
