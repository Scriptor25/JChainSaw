package io.scriptor.stmt;

import io.scriptor.expr.Expr;

public class IfStmt extends Stmt {

    public Expr condition;
    public Stmt[] thenStmt;
    public Stmt[] elseStmt;

    @Override
    public String toString() {
        final var thenBuilder = new StringBuilder().append("{\n");
        for (final var stmt : thenStmt)
            thenBuilder.append("\t").append(stmt).append("\n");
        thenBuilder.append("}");

        final var elseBuilder = new StringBuilder().append("{\n");
        if (elseStmt != null)
            for (final var stmt : elseStmt)
                elseBuilder.append("\t").append(stmt).append("\n");
        elseBuilder.append("}");

        return String.format("if (%s) %s%s", condition, thenBuilder,
                elseStmt != null ? (" else " + elseBuilder.toString()) : "");
    }
}
