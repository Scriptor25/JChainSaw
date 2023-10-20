package io.scriptor.stmt;

import io.scriptor.Parameter;

public class ThingStmt extends Stmt {

    public String name;
    public String group;
    public Parameter[] fields;

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
