package io.scriptor.csaw.impl.frontend.expr;

public class ConExpr extends Expr {

    private final Expr mCondition;
    private final Expr mThenExpr;
    private final Expr mElseExpr;

    public ConExpr(Expr condition, Expr thenExpr, Expr elseExpr) {
        this.mCondition = condition;
        this.mThenExpr = thenExpr;
        this.mElseExpr = elseExpr;
    }

    public synchronized Expr condition() {
        return mCondition;
    }

    public synchronized Expr thenExpr() {
        return mThenExpr;
    }

    public synchronized Expr elseExpr() {
        return mElseExpr;
    }

    @Override
    public boolean isConstant() {
        if (!mCondition.isConstant())
            return false;
        final var value = new ConstExpr(mCondition);
        if (value.value().asNum().getBool())
            return mThenExpr.isConstant();
        return mElseExpr.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr
                .make(new ConExpr(mCondition.makeConstant(), mThenExpr.makeConstant(), mElseExpr.makeConstant()));
    }

    @Override
    public String toString() {
        return String.format("%s ? %s : %s", mCondition, mThenExpr, mElseExpr);
    }
}
