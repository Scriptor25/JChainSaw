package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.Parameter;

public class FunStmt extends Stmt {

    public boolean constructor;
    public String name;
    public String type;
    public Parameter[] parameters;
    public boolean vararg;
    public String member;
    public Stmt[] body;

    @Override
    public String toString() {
        final var paramBuilder = new StringBuilder();
        for (int i = 0; i < (parameters == null ? 0 : parameters.length); i++) {
            if (i > 0)
                paramBuilder.append(", ");
            paramBuilder.append(parameters[i]);
        }

        final var bodyBuilder = new StringBuilder().append("{\n");
        for (final var stmt : body)
            bodyBuilder.append("\t").append(stmt).append("\n");
        bodyBuilder.append("}");

        return String.format("%s%s: %s (%s)%s%s %s", constructor ? "$" : "@", name, type, paramBuilder,
                vararg ? " $" : "", member != null ? " -> " + member : "", bodyBuilder);
    }
}
