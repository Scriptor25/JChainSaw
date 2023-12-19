package io.scriptor.csaw.impl.frontend.expr;

import io.scriptor.csaw.impl.frontend.stmt.Stmt;

public abstract class Expr extends Stmt {

    public abstract boolean isConstant();

    public abstract Expr makeConstant();
}
