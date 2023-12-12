package io.scriptor.csaw.impl.expr;

public class ChrExpr extends Expr {

    public final char value;

    public ChrExpr(char value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("'%c'", value);
    }
}
