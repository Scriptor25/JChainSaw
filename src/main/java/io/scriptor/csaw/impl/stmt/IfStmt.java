package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class IfStmt extends Stmt {

    public final Expr condition;
    public final Stmt thenBody;
    public final Stmt elseBody;

    public IfStmt(Expr condition, Stmt thenBody, Stmt elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    @Override
    public String toString() {
        return String.format("if (%s) %s%s", condition, thenBody, elseBody != null ? (" else " + elseBody) : "");
    }
}
