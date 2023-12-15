package io.scriptor.csaw.impl.expr;

public class AssignExpr extends Expr {

    public final Expr object;
    public final Expr value;

    public AssignExpr(Expr object, Expr value) {
        this.object = object;
        this.value = value;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new AssignExpr(object.makeConstant(), value.makeConstant());
    }

    @Override
    public String toString() {
        return String.format("%s = %s", object, value);
    }

}
