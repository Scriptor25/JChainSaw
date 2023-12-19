package io.scriptor.csaw.impl.frontend.expr;

public class CallExpr extends Expr {

    private final Expr mFunction;
    private final Expr[] mArguments;

    public CallExpr(Expr function, Expr[] arguments) {
        this.mFunction = function;
        this.mArguments = arguments;
    }

    public synchronized Expr function() {
        return mFunction;
    }

    public synchronized Expr[] arguments() {
        return mArguments;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        final var args = new Expr[mArguments.length];
        for (int i = 0; i < args.length; i++)
            args[i] = mArguments[i].makeConstant();
        return new CallExpr(mFunction.makeConstant(), args);
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        for (int i = 0; i < mArguments.length; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(mArguments[i]);
        }

        return String.format("%s(%s)", mFunction, builder);
    }

}
