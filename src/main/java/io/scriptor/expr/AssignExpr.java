package io.scriptor.expr;

public class AssignExpr extends Expr {

    public String id;
    public Expr value;

    public AssignExpr(String id, Expr value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s = %s", id, value);
    }
}
