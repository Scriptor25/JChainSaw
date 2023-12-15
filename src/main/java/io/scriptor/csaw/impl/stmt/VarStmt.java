package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.expr.Expr;

public class VarStmt extends Stmt {

    public final Type type;
    public final String name;
    public final Expr value;

    public VarStmt(Type type, String name, Expr value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s %s%s", type, name,
                value != null ? " = " + value.toString() : "");
    }
}
