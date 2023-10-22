package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.expr.Expr;

public class WhileStmt extends Stmt {

    public Expr condition;
    public Stmt[] body;

    @Override
    public String toString() {
        final var bodyBuilder = new StringBuilder().append("{\n");
        for (final var stmt : body)
            bodyBuilder.append("\t").append(stmt).append("\n");
        bodyBuilder.append("}");

        return String.format("while (%s) %s", condition, bodyBuilder);
    }
}
