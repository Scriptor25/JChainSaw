package io.scriptor.csaw.impl.frontend.expr;

public class AssignExpr extends Expr {

    private final Expr mObject;
    private final Expr mValue;

    public AssignExpr(Expr object, Expr value) {
        this.mObject = object;
        this.mValue = value;
    }

    public synchronized Expr object() {
        return mObject;
    }

    public synchronized Expr value() {
        return mValue;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new AssignExpr(mObject.makeConstant(), mValue.makeConstant());
    }

    @Override
    public String toString() {
        return String.format("%s = %s", mObject, mValue);
    }

}
