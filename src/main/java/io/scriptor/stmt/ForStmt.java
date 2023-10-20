package io.scriptor.stmt;

import io.scriptor.expr.Expr;

public class ForStmt extends Stmt {

    public Stmt begin;
    public Expr condition;
    public Stmt loop;
    public Stmt[] body;

    @Override
    public String toString() {
        final var builder = new StringBuilder().append("{\n");
        for (final var stmt : body)
            builder.append("\t").append(stmt).append("\n");
        builder.append("}");

        return String.format("for (%s; %s; %s) %s", begin, condition, loop, builder);
    }

}
