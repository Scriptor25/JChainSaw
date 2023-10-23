package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class IfStmt extends Stmt {

    public boolean constant = false;

    public Expr condition;
    public Stmt[] thenBody;
    public Stmt[] elseBody;

    @Override
    public String toString() {
        final var thenBuilder = new StringBuilder().append("{\n");
        for (final var stmt : thenBody)
            thenBuilder.append("\t").append(stmt).append("\n");
        thenBuilder.append("}");

        final var elseBuilder = new StringBuilder().append("{\n");
        if (elseBody != null)
            for (final var stmt : elseBody)
                elseBuilder.append("\t").append(stmt).append("\n");
        elseBuilder.append("}");

        return String.format("if (%s) %s%s", condition, thenBuilder,
                elseBody != null ? (" else " + elseBuilder.toString()) : "");
    }
}
