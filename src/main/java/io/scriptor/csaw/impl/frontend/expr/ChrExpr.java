package io.scriptor.csaw.impl.frontend.expr;

public class ChrExpr extends Expr {

    public final char value;

    public ChrExpr(char value) {
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
        return String.format("'%c'", value);
    }
}
