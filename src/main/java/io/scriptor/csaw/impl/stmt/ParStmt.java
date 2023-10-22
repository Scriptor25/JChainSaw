package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class ParStmt extends Stmt {

    public Expr from;
    public Expr length;
    public String variable;
    public Stmt[] body;

    @Override
    public String toString() {
        final var builder = new StringBuilder().append("{\n");
        for (final var stmt : body)
            builder.append("\t").append(stmt).append("\n");
        builder.append("}");

        return String.format("par (%s; %s; %s) %s", from, length, variable, builder);
    }

}
