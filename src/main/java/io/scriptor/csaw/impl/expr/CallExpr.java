package io.scriptor.csaw.impl.expr;

public class CallExpr extends Expr {

    public Expr function;
    public Expr[] arguments;

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(arguments[i]);
        }

        return String.format("%s(%s)", function, builder);
    }
}
