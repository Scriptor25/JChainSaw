package io.scriptor.csaw.impl.frontend.expr;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;

public class IdExpr extends Expr {

    private final String mValue;

    public IdExpr(String name) {
        this.mValue = name;
    }

    public synchronized String value() {
        return mValue;
    }

    @Override
    public boolean isConstant() {
        return getGlobal().hasVariable(mValue);
    }

    @Override
    public Expr makeConstant() {
        return ConstExpr.make(this);
    }

    @Override
    public String toString() {
        return mValue;
    }

}
