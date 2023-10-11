package io.scriptor;

import io.scriptor.expr.AssignExpr;
import io.scriptor.expr.BinExpr;
import io.scriptor.expr.CallExpr;
import io.scriptor.expr.Expr;
import io.scriptor.expr.IdExpr;
import io.scriptor.expr.NumExpr;
import io.scriptor.expr.StrExpr;
import io.scriptor.stmt.FunStmt;
import io.scriptor.stmt.IfStmt;
import io.scriptor.stmt.RetStmt;
import io.scriptor.stmt.Stmt;
import io.scriptor.value.NumValue;
import io.scriptor.value.StrValue;
import io.scriptor.value.Value;

public class Interpreter {

    private Interpreter() {
    }

    public static Value evaluate(Environment env, FunDef<?> fun, Value... args) {
        final var e = new Environment(env);
        for (int i = 0; i < args.length; i++)
            e.createVariable(fun.parameters[i].name, args[i]);

        Value value = null;
        for (final var stmt : fun.implementation) {
            final var v = evaluate(e, stmt);
            if (v != null && v.isReturn()) {
                value = v.isReturn(false);
                break;
            }
        }

        if (fun.type.equals(Value.class))
            return value;

        if (value == null || !fun.type.equals(value.getClass()))
            return null;

        return value;
    }

    public static Value evaluate(Environment env, Stmt stmt) {
        if (stmt instanceof FunStmt)
            return evaluate(env, (FunStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluate(env, (IfStmt) stmt);
        if (stmt instanceof RetStmt)
            return evaluate(env, (RetStmt) stmt);

        if (stmt instanceof Expr)
            return evaluate(env, (Expr) stmt);

        return null;
    }

    public static Value evaluate(Environment env, FunStmt stmt) {
        env.createFunction(stmt.name, stmt.type, stmt.parameters, stmt.implementation);
        return null;
    }

    public static Value evaluate(Environment env, IfStmt stmt) {
        final var condition = evaluate(env, stmt.condition);

        if (condition.asBoolean())
            for (final var s : stmt.thenStmt) {
                final var value = evaluate(env, s);
                if (value.isReturn())
                    return value;
            }

        if (stmt.elseStmt != null)
            for (final var s : stmt.elseStmt) {
                final var value = evaluate(env, s);
                if (value.isReturn())
                    return value;
            }

        return null;
    }

    public static Value evaluate(Environment env, RetStmt stmt) {
        return stmt.value == null ? null : evaluate(env, stmt.value).isReturn(true);
    }

    public static Value evaluate(Environment env, Expr expr) {
        if (expr instanceof AssignExpr)
            return evaluate(env, (AssignExpr) expr);
        if (expr instanceof BinExpr)
            return evaluate(env, (BinExpr) expr);
        if (expr instanceof CallExpr)
            return evaluate(env, (CallExpr) expr);
        if (expr instanceof IdExpr)
            return evaluate(env, (IdExpr) expr);
        if (expr instanceof NumExpr)
            return evaluate(env, (NumExpr) expr);
        if (expr instanceof StrExpr)
            return evaluate(env, (StrExpr) expr);

        return null;
    }

    public static Value evaluate(Environment env, AssignExpr expr) {
        return env.setVariable(expr.id, evaluate(env, expr.value));
    }

    public static Value evaluate(Environment env, BinExpr expr) {
        final var left = evaluate(env, expr.left);
        final var right = evaluate(env, expr.right);

        final var result = switch (expr.operator) {

            case "&" -> Value.binAnd(left, right);
            case "&&" -> Value.and(left, right);
            case "|" -> Value.binOr(left, right);
            case "||" -> Value.or(left, right);
            case "==" -> Value.cmpe(left, right);
            case "<" -> Value.cmpl(left, right);
            case "<=" -> Value.cmple(left, right);
            case ">" -> Value.cmpg(left, right);
            case ">=" -> Value.cmpge(left, right);
            case "+" -> Value.add(left, right);
            case "-" -> Value.sub(left, right);
            case "*" -> Value.mul(left, right);
            case "/" -> Value.div(left, right);
            case "%" -> Value.mod(left, right);

            default -> throw new RuntimeException("undefined operator");
        };

        return result;
    }

    public static Value evaluate(Environment env, CallExpr expr) {
        final var args = new Value[expr.arguments.length];
        final var argsClasses = new Class<?>[expr.arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = evaluate(env, expr.arguments[i]);
            argsClasses[i] = args[i].getClass();
        }

        final var fun = env.getFunction(expr.function, argsClasses);
        if (fun == null)
            return null;

        return evaluate(env, fun, args);
    }

    public static Value evaluate(Environment env, IdExpr expr) {
        return env.getVariable(expr.name);
    }

    public static Value evaluate(Environment env, NumExpr expr) {
        return new NumValue(expr.value);
    }

    public static Value evaluate(Environment env, StrExpr expr) {
        return new StrValue(expr.value);
    }
}
