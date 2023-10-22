package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class RetStmt extends Stmt {

    public Expr value;

    @Override
    public String toString() {
        return String.format("ret %s", value);
    }
}
