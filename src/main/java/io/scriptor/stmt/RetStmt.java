package io.scriptor.stmt;

import io.scriptor.expr.Expr;

public class RetStmt extends Stmt {

    public Expr value;

    @Override
    public String toString() {
        return String.format("ret %s", value);
    }
}
