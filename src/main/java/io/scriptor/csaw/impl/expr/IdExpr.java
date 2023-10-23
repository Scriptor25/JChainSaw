package io.scriptor.csaw.impl.expr;

public class IdExpr extends Expr {

    public String name;

    public IdExpr(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
