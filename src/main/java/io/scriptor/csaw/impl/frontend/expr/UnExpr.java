package io.scriptor.csaw.impl.frontend.expr;

public class UnExpr extends Expr {

    private final String mOperator;
    private final Expr mValue;

    public UnExpr(String operator, Expr value) {
        this.mOperator = operator;
        this.mValue = value;
    }

    public synchronized String operator() {
        return mOperator;
    }

    public synchronized Expr value() {
        return mValue;
    }

    @Override
    public boolean isConstant() {
        return mValue.isConstant();
    }

    @Override
    public Expr makeConstant() {
        if (isConstant())
            return new ConstExpr(this);
        return ConstExpr.make(new UnExpr(mOperator, mValue.makeConstant()));
    }

    @Override
    public String toString() {
        return String.format("%s%s", mOperator, mValue);
    }
}
