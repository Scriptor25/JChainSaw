package io.scriptor.csaw.impl.frontend.expr;

import java.util.Arrays;

import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.frontend.stmt.Stmt;

public class LambdaExpr extends Expr {

    public final IdExpr[] passed;
    public final Parameter[] parameters;
    public final Stmt body;

    public LambdaExpr(IdExpr[] passed, Parameter[] parameters, Stmt body) {
        this.passed = passed;
        this.parameters = parameters;
        this.body = body;
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
        return String.format("%s%s %s", Arrays.toString(passed), Arrays.toString(parameters), body);
    }

}
