package io.scriptor.csaw.impl.frontend.expr;

public class BinExpr extends Expr {

    public final Expr left;
    public final String operator;
    public final Expr right;

    public BinExpr(Expr left, Expr right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr.make(new BinExpr(left.makeConstant(), right.makeConstant(), operator));
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator, right);
    }
}
