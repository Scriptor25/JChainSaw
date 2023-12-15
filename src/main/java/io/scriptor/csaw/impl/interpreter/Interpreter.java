package io.scriptor.csaw.impl.interpreter;

import static io.scriptor.csaw.impl.interpreter.Environment.createAlias;
import static io.scriptor.csaw.impl.interpreter.Environment.createFunction;
import static io.scriptor.csaw.impl.interpreter.Environment.createThing;
import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.isAssignable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Parser;
import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.expr.AssignExpr;
import io.scriptor.csaw.impl.expr.BinExpr;
import io.scriptor.csaw.impl.expr.CallExpr;
import io.scriptor.csaw.impl.expr.ChrExpr;
import io.scriptor.csaw.impl.expr.ConExpr;
import io.scriptor.csaw.impl.expr.ConstExpr;
import io.scriptor.csaw.impl.expr.Expr;
import io.scriptor.csaw.impl.expr.IdExpr;
import io.scriptor.csaw.impl.expr.IndexExpr;
import io.scriptor.csaw.impl.expr.LambdaExpr;
import io.scriptor.csaw.impl.expr.MemExpr;
import io.scriptor.csaw.impl.expr.NumExpr;
import io.scriptor.csaw.impl.expr.StrExpr;
import io.scriptor.csaw.impl.expr.UnExpr;
import io.scriptor.csaw.impl.interpreter.value.ConstChr;
import io.scriptor.csaw.impl.interpreter.value.ConstNull;
import io.scriptor.csaw.impl.interpreter.value.ConstLambda;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.csaw.impl.interpreter.value.NamedValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.csaw.impl.stmt.AliasStmt;
import io.scriptor.csaw.impl.stmt.EnclosedStmt;
import io.scriptor.csaw.impl.stmt.ForStmt;
import io.scriptor.csaw.impl.stmt.FunStmt;
import io.scriptor.csaw.impl.stmt.IfStmt;
import io.scriptor.csaw.impl.stmt.IncStmt;
import io.scriptor.csaw.impl.stmt.RetStmt;
import io.scriptor.csaw.impl.stmt.Stmt;
import io.scriptor.csaw.impl.stmt.ThingStmt;
import io.scriptor.csaw.impl.stmt.VarStmt;
import io.scriptor.csaw.impl.stmt.WhileStmt;
import io.scriptor.java.ErrorUtil;

public class Interpreter {

    private Interpreter() {
    }

    public static Value evaluate(Environment env, Stmt stmt) {
        if (stmt == null)
            return new ConstNull();

        if (stmt instanceof EnclosedStmt)
            return evaluate(env, (EnclosedStmt) stmt);

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

        throw new CSawException("evaluation of statement '%s' not yet implemented", stmt);
    }

    public static Value evaluate(Environment env, EnclosedStmt stmt) {
        final var environment = new Environment(env);
        for (final var s : stmt.body) {
            final var value = evaluate(environment, s);
            if (value.isReturn())
                return value;
        }
        return new ConstNull();
    }

    public static Value evaluate(Environment env, AliasStmt stmt) {
        createAlias(stmt.alias, stmt.origin);
        return new ConstNull();
    }

    public static Value evaluate(Environment env, ForStmt stmt) {
        final var e = new Environment(env);
        for (evaluate(e, stmt.begin); evaluate(e, stmt.condition).asNum().getBool(); evaluate(e, stmt.loop)) {
            final var value = evaluate(e, stmt.body);
            if (value.isReturn())
                return value;
        }
        return new ConstNull();
    }

    public static Value evaluate(Environment env, FunStmt stmt) {
        createFunction(
                stmt.constructor,
                stmt.name,
                stmt.type,
                stmt.parameters,
                stmt.vararg,
                stmt.member,
                stmt.body);
        return new ConstNull();
    }

    public static Value evaluate(Environment env, IfStmt stmt) {
        final var condition = evaluate(env, stmt.condition);
        if (condition.asNum().getBool())
            return evaluate(env, stmt.thenBody);
        return evaluate(env, stmt.elseBody);
    }

    public static Value evaluate(Environment env, IncStmt stmt) {
        final var path = env.getPath();
        final var file = new File(path, stmt.path);
        Parser.parse(ErrorUtil.handle(() -> new FileInputStream(file)), env.setPath(file.getParent()));
        env.setPath(path);
        return new ConstNull();
    }

    public static Value evaluate(Environment env, RetStmt stmt) {
        return evaluate(env, stmt.value).isReturn(true);
    }

    public static Value evaluate(Environment env, ThingStmt stmt) {
        createThing(stmt.group, stmt.name, stmt.fields);
        return new ConstNull();
    }

    public static Value evaluate(Environment env, VarStmt stmt) {
        Value value;
        if (stmt.value == null)
            value = Value.makeValue(env, stmt.type, false, false);
        else if (!isAssignable((value = evaluate(env, stmt.value)).getType(), stmt.type))
            throw new CSawException("cannot assign value of type '%s' to type '%s'", value.getType(), stmt.type);

        env.createVariable(stmt.name, stmt.type, value);
        return new ConstNull();
    }

    public static Value evaluate(Environment env, WhileStmt stmt) {
        final var e = new Environment(env);
        while (evaluate(e, stmt.condition).asNum().getBool()) {
            final var value = evaluate(e, stmt.body);
            if (value.isReturn())
                return value;
        }

        return new ConstNull();
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
        if (expr instanceof ConstExpr)
            return ((ConstExpr) expr).value;
        if (expr instanceof IdExpr)
            return evaluate(env, (IdExpr) expr);
        if (expr instanceof IndexExpr)
            return evaluate(env, (IndexExpr) expr);
        if (expr instanceof LambdaExpr)
            return evaluate(env, (LambdaExpr) expr);
        if (expr instanceof MemExpr)
            return evaluate(env, (MemExpr) expr);
        if (expr instanceof NumExpr)
            return evaluate(env, (NumExpr) expr);
        if (expr instanceof StrExpr)
            return evaluate(env, (StrExpr) expr);
        if (expr instanceof UnExpr)
            return evaluate(env, (UnExpr) expr);

        throw new CSawException("evaluation of expression '%s' not yet implemented", expr);
    }

    public static Value evaluate(Environment env, AssignExpr expr) {
        final var value = evaluate(env, expr.value);

        if (expr.object instanceof IdExpr e)
            return env.setVariable(e.value, value);

        if (expr.object instanceof MemExpr e)
            return evaluate(env, e.object).asThing().setField(e.member, value);

        if (expr.object instanceof IndexExpr e) {
            final var ref = evaluate(env, e.expr);
            final var idx = evaluate(env, e.index);
            if (ref.isRef())
                return ref.asRef().set(idx.asNum().getInt(), value);
            return getAndInvoke(ref, "[]", idx, value);
        }

        throw new CSawException("unsupported assign operation %s", expr);
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
            case "^" -> Value.xor(env, left, right);
            case "<<" -> Value.sl(env, left, right);
            case ">>" -> Value.sr(env, left, right);

            default -> throw new CSawException(
                    "operator '%s' not supported for types '%s' and '%s'",
                    expr.operator,
                    left.getType(),
                    right.getType());
        };
    }

    public static Value evaluate(Environment env, CallExpr expr) {
        final var args = new Value[expr.arguments.length];
        final var argTypes = new Type[expr.arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = evaluate(env, expr.arguments[i]);
            argTypes[i] = args[i].getType();
        }

        String name = null;
        Value member = new ConstNull();

        if (expr.function instanceof IdExpr e)
            name = e.value;
        else if (expr.function instanceof MemExpr e) {
            member = evaluate(env, e.object);
            if (member.isNull())
                throw new CSawException("failed to evaluate object of member expression");
            name = e.member;
        }

        if (env.hasVariable(name)) {
            final var lambda = env.getVariable(name);
            if (lambda.isLambda())
                return lambda.asLambda().invoke(args);
        }

        return getAndInvoke(member, name, args);
    }

    public static Value evaluate(Environment env, ChrExpr expr) {
        return new ConstChr(expr.value);
    }

    public static Value evaluate(Environment env, ConExpr expr) {
        return evaluate(env, expr.condition).asNum().getBool()
                ? evaluate(env, expr.thenExpr)
                : evaluate(env, expr.elseExpr);
    }

    public static Value evaluate(Environment env, IdExpr expr) {
        return env.getVariable(expr.value);
    }

    public static Value evaluate(Environment env, IndexExpr expr) {
        final var value = evaluate(env, expr.expr);
        final var index = evaluate(env, expr.index);

        if (value.isRef())
            return value.asRef().get(index.asNum().getInt());

        return getAndInvoke(value, "[]", index);
    }

    public static Value evaluate(Environment env, LambdaExpr expr) {
        final var passed = Arrays.stream(expr.passed)
                .map(e -> new NamedValue(e.value, evaluate(env, e)))
                .toArray(n -> new NamedValue[n]);
        return new ConstLambda(passed, expr.parameters, expr.body);
    }

    public static Value evaluate(Environment env, MemExpr expr) {
        return evaluate(env, expr.object).asThing().getField(expr.member);
    }

    public static Value evaluate(Environment env, NumExpr expr) {
        return new ConstNum(expr.value);
    }

    public static Value evaluate(Environment env, StrExpr expr) {
        return new ConstStr(expr.value);
    }

    public static Value evaluate(Environment env, UnExpr expr) {
        final var value = evaluate(env, expr.value);

        return switch (expr.operator) {

            case "-" -> Value.neg(env, value);
            case "!" -> Value.not(env, value);
            case "~" -> Value.inv(env, value);

            default -> throw new CSawException("unsupported operator '%s'", expr.operator);
        };
    }
}
