package io.scriptor.csaw.impl.frontend.expr;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;

import io.scriptor.csaw.impl.interpreter.Interpreter;
import io.scriptor.csaw.impl.interpreter.value.Value;

public class ConstExpr extends Expr {

    private final Value mValue;
    private final String mStringRep;

    public ConstExpr(Expr expr) {
        this.mValue = Interpreter.evaluate(getGlobal(), expr);
        this.mStringRep = expr.toString();
    }

    public synchronized Value value() {
        return mValue;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Expr makeConstant() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s[=%s]", mStringRep, mValue);
    }

    public static Expr make(Expr expr) {
        if (expr.isConstant())
            return new ConstExpr(expr);
        return expr;
    }

}
