package io.scriptor.csaw.impl.expr;

public class IdExpr extends Expr {

    public final String value;

    public IdExpr(String name) {
        this.value = name;
    }

    @Override
    public String toString() {
        return value;
    }

}
