package io.scriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import io.scriptor.expr.AssignExpr;
import io.scriptor.expr.BinExpr;
import io.scriptor.expr.CallExpr;
import io.scriptor.expr.ChrExpr;
import io.scriptor.expr.ConExpr;
import io.scriptor.expr.Expr;
import io.scriptor.expr.IdExpr;
import io.scriptor.expr.MemExpr;
import io.scriptor.expr.NumExpr;
import io.scriptor.expr.StrExpr;
import io.scriptor.expr.UnExpr;
import io.scriptor.stmt.AliasStmt;
import io.scriptor.stmt.ForStmt;
import io.scriptor.stmt.FunStmt;
import io.scriptor.stmt.IfStmt;
import io.scriptor.stmt.IncStmt;
import io.scriptor.stmt.ParStmt;
import io.scriptor.stmt.RetStmt;
import io.scriptor.stmt.Stmt;
import io.scriptor.stmt.ThingStmt;
import io.scriptor.stmt.VarStmt;
import io.scriptor.stmt.WhileStmt;
import io.scriptor.value.ChrValue;
import io.scriptor.value.NumValue;
import io.scriptor.value.ObjValue;
import io.scriptor.value.StrValue;
import io.scriptor.value.Value;

public class Interpreter {

    private Interpreter() {
    }

    public static Value evaluate(Environment env, Stmt stmt) {
        if (stmt == null)
            return null;

        if (stmt instanceof AliasStmt)
            return evaluate(env, (AliasStmt) stmt);
        if (stmt instanceof ForStmt)
            return evaluate(env, (ForStmt) stmt);
        if (stmt instanceof FunStmt)
            return evaluate(env, (FunStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluate(env, (IfStmt) stmt);
        if (stmt instanceof IncStmt)
            return evaluate(env, (IncStmt) stmt);
        if (stmt instanceof ParStmt)
            return evaluate(env, (ParStmt) stmt);
        if (stmt instanceof RetStmt)
            return evaluate(env, (RetStmt) stmt);
        if (stmt instanceof ThingStmt)
            return evaluate(env, (ThingStmt) stmt);
        if (stmt instanceof VarStmt)
            return evaluate(env, (VarStmt) stmt);
        if (stmt instanceof WhileStmt)
            return evaluate(env, (WhileStmt) stmt);

        if (stmt instanceof Expr)
            return evaluate(env, (Expr) stmt);

        throw new RuntimeException();
    }

    public static Value evaluate(Environment env, AliasStmt stmt) {
        env.createAlias(stmt.alias, stmt.origin);
        return null;
    }

    public static Value evaluate(Environment env, ForStmt stmt) {
        final var e = new Environment(env);
        for (evaluate(e, stmt.begin); evaluate(e, stmt.condition).asBoolean(); evaluate(e, stmt.loop)) {
            final var environment = new Environment(e);
            for (final var s : stmt.body) {
                final var value = evaluate(environment, s);
                if (value != null && value.isReturn())
                    return value;
            }
        }
        return null;
    }

    public static Value evaluate(Environment env, FunStmt stmt) {
        env.createFunction(
                stmt.constructor,
                stmt.name,
                stmt.type,
                stmt.parameters,
                stmt.vararg,
                stmt.member,
                stmt.body);
        return null;
    }

    public static Value evaluate(Environment env, IfStmt stmt) {
        final var condition = evaluate(env, stmt.condition);
        final var e = new Environment(env);

        if (condition.asBoolean()) {
            for (final var s : stmt.thenBody) {
                final var value = evaluate(e, s);
                if (value != null && value.isReturn())
                    return value;
            }
            return null;
        }

        if (stmt.elseBody != null)
            for (final var s : stmt.elseBody) {
                final var value = evaluate(e, s);
                if (value != null && value.isReturn())
                    return value;
            }

        return null;
    }

    public static Value evaluate(Environment env, IncStmt stmt) {
        final var path = env.getPath();
        final var file = new File(path, stmt.path);
        try {
            final var parser = new Parser(new FileInputStream(file), env.setPath(file.getParent()));
            parser.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        env.setPath(path);

        return null;
    }

    public static Value evaluate(Environment env, ParStmt stmt) {

        final var from = (int) (double) ((NumValue) evaluate(env, stmt.from)).getValue();
        final var length = (int) (double) ((NumValue) evaluate(env, stmt.length)).getValue();

        IntStream.range(from, from + length)
                .parallel()
                .forEach(i -> {
                    while (true) {
                        try {
                            final var environment = new Environment(env);
                            environment.createVariable(stmt.variable, Value.TYPE_NUM, new NumValue(i));
                            for (final var s : stmt.body) {
                                final var value = evaluate(environment, s);
                                if (value != null && value.isReturn())
                                    break;
                            }
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        return null;
    }

    public static Value evaluate(Environment env, RetStmt stmt) {
        return stmt.value == null ? null : evaluate(env, stmt.value).isReturn(true);
    }

    public static Value evaluate(Environment env, ThingStmt stmt) {
        env.createType(stmt.group, stmt.name, stmt.fields);
        return null;
    }

    public static Value evaluate(Environment env, VarStmt stmt) {
        var value = stmt.value == null ? null : evaluate(env, stmt.value);

        if (value != null && !env.isAssignable(value.getType(), stmt.type))
            throw new IllegalStateException(
                    String.format("cannot assign value of type '%s' to type '%s'", value.getType(), stmt.type));
        else if (value == null)
            value = Value.makeValue(env, stmt.type, false);

        env.createVariable(stmt.name, stmt.type, value);
        return null;
    }

    public static Value evaluate(Environment env, WhileStmt stmt) {
        final var e = new Environment(env);
        while (evaluate(e, stmt.condition).asBoolean()) {
            final var e1 = new Environment(e);
            for (final var s : stmt.body) {
                final var value = evaluate(e1, s);
                if (value != null && value.isReturn())
                    return value;
            }
        }

        return null;
    }

    public static Value evaluate(Environment env, Expr expr) {
        if (expr instanceof AssignExpr)
            return evaluate(env, (AssignExpr) expr);
        if (expr instanceof BinExpr)
            return evaluate(env, (BinExpr) expr);
        if (expr instanceof CallExpr)
            return evaluate(env, (CallExpr) expr);
        if (expr instanceof ChrExpr)
            return evaluate(env, (ChrExpr) expr);
        if (expr instanceof ConExpr)
            return evaluate(env, (ConExpr) expr);
        if (expr instanceof IdExpr)
            return evaluate(env, (IdExpr) expr);
        if (expr instanceof MemExpr)
            return evaluate(env, (MemExpr) expr);
        if (expr instanceof NumExpr)
            return evaluate(env, (NumExpr) expr);
        if (expr instanceof StrExpr)
            return evaluate(env, (StrExpr) expr);
        if (expr instanceof UnExpr)
            return evaluate(env, (UnExpr) expr);

        throw new RuntimeException();
    }

    public static Value evaluate(Environment env, AssignExpr expr) {
        if (expr.object instanceof IdExpr)
            return env.setVariable(((IdExpr) expr.object).name, evaluate(env, expr.value));
        if (expr.object instanceof MemExpr) {
            final var object = (ObjValue) evaluate(env, ((MemExpr) expr.object).object);
            return object.setField(((MemExpr) expr.object).member, evaluate(env, expr.value));
        }

        throw new IllegalStateException(String.format("unsupported assign operation %s", expr));
    }

    public static Value evaluate(Environment env, BinExpr expr) {
        final var left = evaluate(env, expr.left);
        final var right = evaluate(env, expr.right);

        return switch (expr.operator) {

            case "&" -> Value.binAnd(env, left, right);
            case "&&" -> Value.and(env, left, right);
            case "|" -> Value.binOr(env, left, right);
            case "||" -> Value.or(env, left, right);
            case "==" -> Value.cmpe(env, left, right);
            case "!=" -> Value.cmpne(env, left, right);
            case "<" -> Value.cmpl(env, left, right);
            case "<=" -> Value.cmple(env, left, right);
            case ">" -> Value.cmpg(env, left, right);
            case ">=" -> Value.cmpge(env, left, right);
            case "+" -> Value.add(env, left, right);
            case "-" -> Value.sub(env, left, right);
            case "*" -> Value.mul(env, left, right);
            case "/" -> Value.div(env, left, right);
            case "%" -> Value.mod(env, left, right);

            default -> throw new IllegalStateException(String.format("unsupported operator '%s'", expr.operator));
        };
    }

    public static Value evaluate(Environment env, CallExpr expr) {
        final var args = new Value[expr.arguments.length];
        final var argTypes = new String[expr.arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = evaluate(env, expr.arguments[i]);
            argTypes[i] = args[i].getType();
        }

        String name = null;
        Value member = null;

        if (expr.function instanceof IdExpr)
            name = ((IdExpr) expr.function).name;
        else if (expr.function instanceof MemExpr) {
            member = evaluate(env, ((MemExpr) expr.function).object);
            name = ((MemExpr) expr.function).member;
        }

        final var fun = env.getFunction(member != null ? member.getType() : null, name, argTypes);
        return fun.body.invoke(member, env, args);
    }

    public static Value evaluate(Environment env, ChrExpr expr) {
        return new ChrValue(expr.value);
    }

    public static Value evaluate(Environment env, ConExpr expr) {
        return evaluate(env, expr.condition).asBoolean()
                ? evaluate(env, expr.thenExpr)
                : evaluate(env, expr.elseExpr);
    }

    public static Value evaluate(Environment env, IdExpr expr) {
        return env.getVariable(expr.name);
    }

    public static Value evaluate(Environment env, MemExpr expr) {
        return ((ObjValue) evaluate(env, expr.object)).getField(expr.member);
    }

    public static Value evaluate(Environment env, NumExpr expr) {
        return new NumValue(expr.value);
    }

    public static Value evaluate(Environment env, StrExpr expr) {
        return new StrValue(expr.value);
    }

    public static Value evaluate(Environment env, UnExpr expr) {
        final var value = evaluate(env, expr.value);

        return switch (expr.operator) {

            case "-" -> Value.neg(env, value);
            case "!" -> Value.not(env, value);
            case "~" -> Value.inv(env, value);

            default -> throw new IllegalStateException(String.format("unsupported operator '%s'", expr.operator));
        };
    }
}
