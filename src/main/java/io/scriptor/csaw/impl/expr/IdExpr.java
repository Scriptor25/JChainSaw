package io.scriptor.csaw.impl.expr;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;

public class IdExpr extends Expr {

    public final String value;

    public IdExpr(String name) {
        this.value = name;
    }

    @Override
    public boolean isConstant() {
        return getGlobal().hasVariable(value);
    }

    @Override
    public Expr makeConstant() {
        return ConstExpr.make(this);
    }

    @Override
    public String toString() {
        return value;
    }

}
