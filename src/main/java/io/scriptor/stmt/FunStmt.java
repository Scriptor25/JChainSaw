package io.scriptor.stmt;

import io.scriptor.Parameter;

public class FunStmt extends Stmt {

    public String name;
    public String type;
    public Parameter[] parameters;
    public Stmt[] implementation;

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        for (int i = 0; i < (parameters == null ? 0 : parameters.length); i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(parameters[i]);
        }

        final var implBuilder = new StringBuilder().append("{\n");
        for (final var stmt : implementation)
            implBuilder.append("\t").append(stmt).append("\n");
        implBuilder.append("}");

        return String.format("@%s: %s (%s) %s", name, type, builder, implBuilder);
    }
}
