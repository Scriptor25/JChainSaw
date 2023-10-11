package io.scriptor.expr;

public class StrExpr extends Expr {

    public String value;

    public StrExpr(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", value);
    }
}
