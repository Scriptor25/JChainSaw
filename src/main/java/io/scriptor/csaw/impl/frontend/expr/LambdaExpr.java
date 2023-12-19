package io.scriptor.csaw.impl.frontend.expr;

import java.util.Arrays;

import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.frontend.stmt.Stmt;

public class LambdaExpr extends Expr {

    private final IdExpr[] mPassed;
    private final Parameter[] mParameters;
    private final Stmt mBody;

    public LambdaExpr(IdExpr[] passed, Parameter[] parameters, Stmt body) {
        this.mPassed = passed;
        this.mParameters = parameters;
        this.mBody = body;
    }

    public synchronized IdExpr[] passed() {
        return mPassed;
    }

    public synchronized Parameter[] parameters() {
        return mParameters;
    }

    public synchronized Stmt body() {
        return mBody;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Expr makeConstant() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s", Arrays.toString(mPassed), Arrays.toString(mParameters), mBody);
    }

}
