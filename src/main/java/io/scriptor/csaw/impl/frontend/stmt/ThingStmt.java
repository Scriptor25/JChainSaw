package io.scriptor.csaw.impl.frontend.stmt;

import io.scriptor.csaw.impl.Parameter;

public class ThingStmt extends Stmt {

    public final String name;
    public final String group;
    public final Parameter[] fields;

    public ThingStmt(String name, String group, Parameter[] fields) {
        this.name = name;
        this.group = group;
        this.fields = fields;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        if (fields != null) {
            builder.append(" {");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0)
                    builder.append(',');
                builder.append("\n\t").append(fields[i]);
            }
            builder.append("\n}");
        }

        return String.format("thing: %s%s%s", name, group != null ? " : " + group : "",
                fields == null ? ";" : builder.toString());
    }
}
