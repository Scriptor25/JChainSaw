package io.scriptor.csaw.impl.frontend.expr;

public class ChrExpr extends Expr {

    private final char mValue;

    public ChrExpr(char value) {
        this.mValue = value;
    }

    public synchronized char value() {
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
        return String.format("'%c'", mValue);
    }
}
