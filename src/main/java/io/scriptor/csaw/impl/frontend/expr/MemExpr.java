package io.scriptor.csaw.impl.frontend.expr;

public class MemExpr extends Expr {

    private final Expr mObject;
    private final String mMember;

    public MemExpr(Expr object, String member) {
        this.mObject = object;
        this.mMember = member;
    }

    public synchronized Expr object() {
        return mObject;
    }

    public synchronized String member() {
        return mMember;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new MemExpr(mObject.makeConstant(), mMember);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", mObject, mMember);
    }

}
