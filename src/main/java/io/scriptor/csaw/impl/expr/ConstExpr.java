package io.scriptor.csaw.impl.expr;

import io.scriptor.csaw.impl.Interpreter;
import io.scriptor.csaw.impl.value.Value;

public class ConstExpr extends Expr {

    public final Value value;

    public ConstExpr(Expr expr)  {
        if (!expr.isConstant())
            throw new IllegalStateException(
                    String.format("expr %s is not constant, so it cannot be evaluated as a constexpr", expr));

        value = Interpreter.evaluate(null, expr);
    }

    @Override
    public String toString() {
        return String.format("[constexpr %s]", value);
    }

}
