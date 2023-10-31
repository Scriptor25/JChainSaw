package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class ForStmt extends Stmt {

    public Stmt begin;
    public Expr condition;
    public Stmt loop;
    public Stmt body;

    @Override
    public String toString() {
        return String.format("for (%s; %s; %s) %s", begin, condition, loop, body);
    }

}
