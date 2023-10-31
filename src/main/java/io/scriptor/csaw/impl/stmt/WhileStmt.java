package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class WhileStmt extends Stmt {

    public Expr condition;
    public Stmt body;

    @Override
    public String toString() {
        return String.format("while (%s) %s", condition, body);
    }
}
