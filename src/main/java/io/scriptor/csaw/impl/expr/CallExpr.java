package io.scriptor.csaw.impl.expr;

public class CallExpr extends Expr {

    public final Expr function;
    public final Expr[] arguments;

    public CallExpr(Expr function, Expr[] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        final var args = new Expr[arguments.length];
        for (int i = 0; i < args.length; i++)
            args[i] = arguments[i].makeConstant();
        return new CallExpr(function.makeConstant(), args);
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
