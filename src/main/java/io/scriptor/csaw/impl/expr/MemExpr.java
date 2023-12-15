package io.scriptor.csaw.impl.expr;

public class MemExpr extends Expr {

    public final Expr object;
    public final String member;

    public MemExpr(Expr object, String member) {
        this.object = object;
        this.member = member;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return new MemExpr(object.makeConstant(), member);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", object, member);
    }

}
