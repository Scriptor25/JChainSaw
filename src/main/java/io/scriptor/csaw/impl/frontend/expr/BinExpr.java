package io.scriptor.csaw.impl.frontend.expr;

public class BinExpr extends Expr {

    private final Expr mLeft;
    private final String mOperator;
    private final Expr mRight;

    public BinExpr(Expr left, Expr right, String operator) {
        this.mLeft = left;
        this.mRight = right;
        this.mOperator = operator;
    }

    public synchronized Expr left() {
        return mLeft;
    }

    public synchronized String operator() {
        return mOperator;
    }

    public synchronized Expr right() {
        return mRight;
    }

    @Override
    public boolean isConstant() {
        return mLeft.isConstant() && mRight.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr.make(new BinExpr(mLeft.makeConstant(), mRight.makeConstant(), mOperator));
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", mLeft, mOperator, mRight);
    }
}
