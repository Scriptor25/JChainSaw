package io.scriptor.csaw.impl.expr;

public class CallExpr extends Expr {

    public final Expr function;
    public final Expr[] arguments;

    public CallExpr(Expr function, Expr[] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

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
