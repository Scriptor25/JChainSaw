package io.scriptor.csaw.impl.expr;

import io.scriptor.csaw.impl.stmt.Stmt;

public abstract class Expr extends Stmt {

    public boolean isConstant() {
        return false;
    }
}
