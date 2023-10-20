package io.scriptor.expr;

public class MemExpr extends Expr {

    public Expr object;
    public String member;

    @Override
    public String toString() {
        return String.format("%s.%s", object, member);
    }

}
