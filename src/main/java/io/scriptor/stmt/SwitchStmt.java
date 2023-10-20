package io.scriptor.stmt;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.expr.Expr;
import io.scriptor.value.Value;

public class SwitchStmt extends Stmt {

    public Expr switcher;
    public final Map<Value, Stmt[]> cases = new HashMap<>();
    public Stmt[] defaultCase;

    @Override
    public String toString() {
        return String.format("switch (%s) { ... }", switcher);
    }

}
