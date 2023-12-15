package io.scriptor.csaw.impl.expr;

import io.scriptor.csaw.impl.stmt.Stmt;

public abstract class Expr extends Stmt {

    public abstract boolean isConstant();

    public abstract Expr makeConstant();
}
