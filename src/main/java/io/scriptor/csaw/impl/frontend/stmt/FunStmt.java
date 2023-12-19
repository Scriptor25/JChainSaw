package io.scriptor.csaw.impl.frontend.stmt;

import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.interpreter.Type;

public class FunStmt extends Stmt {

    public final boolean constructor;
    public final String name;
    public final Type type;
    public final Parameter[] parameters;
    public final String vararg;
    public final Type member;
    public final EnclosedStmt body;

    public FunStmt(
            boolean constructor,
            String name,
            Type type,
            Parameter[] parameters,
            String vararg,
            Type member,
            EnclosedStmt body) {
        this.constructor = constructor;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
        this.vararg = vararg;
        this.member = member;
        this.body = body;
    }

    @Override
    public String toString() {
        final var paramBuilder = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0)
                paramBuilder.append(", ");
            paramBuilder.append(parameters[i]);
        }

        return String.format("%s%s: %s (%s)%s -> %s %s",
                constructor ? "$" : "@",
                name,
                type,
                paramBuilder,
                vararg != null ? " $" + vararg : "",
                member,
                body);
    }
}
