package io.scriptor.expr;

public class AssignExpr extends Expr {

    public Expr object;
    public Expr value;

    public AssignExpr(Expr object, Expr value) {
        this.object = object;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s = %s", object, value);
    }
}
