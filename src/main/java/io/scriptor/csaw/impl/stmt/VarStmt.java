package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class VarStmt extends Stmt {

    public String type;
    public String name;
    public Expr value;

    @Override
    public String toString() {
        return String.format("%s %s%s;", type, name, value != null ? " = " + value.toString() : "");
    }
}
