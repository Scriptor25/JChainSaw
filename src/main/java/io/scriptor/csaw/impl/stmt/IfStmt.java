package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class IfStmt extends Stmt {

    public Expr condition;
    public Stmt thenBody;
    public Stmt elseBody;

    @Override
    public String toString() {
        return String.format("if (%s) %s%s", condition, thenBody, elseBody != null ? (" else " + elseBody) : "");
    }
}
