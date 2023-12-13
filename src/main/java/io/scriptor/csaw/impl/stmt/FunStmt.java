package io.scriptor.csaw.impl.stmt;

import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.Type;

public class FunStmt extends Stmt {

    public boolean constructor;
    public String name;
    public Type type;
    public Parameter[] parameters;
    public String vararg;
    public Type member;
    public EnclosedStmt body;

    @Override
    public String toString() {
        final var paramBuilder = new StringBuilder();
        for (int i = 0; i < (parameters == null ? 0 : parameters.length); i++) {
            if (i > 0)
                paramBuilder.append(", ");
            paramBuilder.append(parameters[i]);
        }

        return String.format("%s%s: %s (%s)%s%s %s", constructor ? "$" : "@", name, type, paramBuilder,
                vararg != null ? " $" + vararg : "", member != null ? " -> " + member : "", body);
    }
}
