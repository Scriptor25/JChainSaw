package io.scriptor.csaw.impl.frontend.expr;

public class StrExpr extends Expr {

    private final String mValue;

    public StrExpr(String value) {
        this.mValue = value;
    }

    public synchronized String value() {
        return mValue;
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
        return String.format("\"%s\"", mValue);
    }
}
