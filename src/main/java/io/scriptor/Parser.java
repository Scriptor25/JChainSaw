package io.scriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

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

public class Parser {

    private enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        OPERATOR,
        EOF
    }

    private static class Token {

        public String value;
        public TokenType type;

        public Token value(String value) {
            this.value = value;
            return this;
        }

        public Token type(TokenType type) {
            this.type = type;
            return this;
        }

        public static Token EOF() {
            return new Token().type(TokenType.EOF);
        }
    }

    private final BufferedReader mReader;
    private Token mToken;

    public Parser(InputStream stream) throws IOException {
        mReader = new BufferedReader(new InputStreamReader(stream));

        final var env = new Environment();

        next(); // prepare first token
        while (mToken.type != TokenType.EOF)
            Interpreter.evaluate(env, nextStmt());

        System.out.println(Interpreter.evaluate(env, env.getFunction("main")));
    }

    private Token next() throws IOException {
        int c = mReader.read();

        while (isIgnorable(c))
            c = mReader.read();

        if (c < 0)
            return mToken = Token.EOF();

        if (c == '#') {
            c = mReader.read();
            final char LIMIT = c == '#' ? '\n' : '#';
            while ((c = mReader.read()) != LIMIT && c >= 0)
                ;
            return next();
        }

        if (isAlpha(c) || c == '_') {
            final var builder = new StringBuilder();
            do {
                builder.append((char) c);
                mReader.mark(1);
                c = mReader.read();
            } while (isAlnum(c) || c == '_');
            mReader.reset();

            return mToken = new Token().type(TokenType.IDENTIFIER).value(builder.toString());
        }

        if (isDigit(c)) {
            final var builder = new StringBuilder();
            do {
                builder.append((char) c);
                mReader.mark(1);
                c = mReader.read();
            } while (isDigit(c) || c == '.' || c == 'e' || c == 'E');
            mReader.reset();

            return mToken = new Token().type(TokenType.NUMBER).value(builder.toString());
        }

        if (c == '"') {
            final var builder = new StringBuilder();
            c = mReader.read();
            while (c != '"' && c >= 0) {
                builder.append((char) c);
                c = mReader.read();
            }

            return mToken = new Token().type(TokenType.STRING).value(builder.toString());
        }

        return mToken = new Token().type(TokenType.OPERATOR).value(Character.toString(c));
    }

    private static boolean isIgnorable(int c) {
        return c >= 0x00 && c <= 0x20;
    }

    private static boolean isAlpha(int c) {
        return (c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A);
    }

    private static boolean isDigit(int c) {
        return c >= 0x30 && c <= 0x39;
    }

    private static boolean isAlnum(int c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean at(String value) {
        return mToken != null && mToken.value != null && mToken.value.equals(value);
    }

    private Stmt nextStmt() throws IOException {

        if (at("if"))
            return nextIfStmt();

        if (at("ret"))
            return nextRetStmt();

        if (at("@"))
            return nextFunStmt();

        final var expr = nextExpr();
        next(); // skip ;

        return expr;
    }

    private Stmt[] nextEnclosedStmt() throws IOException {
        next(); // skip {

        final List<Stmt> enclosed = new Vector<>();
        do {
            enclosed.add(nextStmt());
        } while (!at("}"));

        next(); // skip }

        return enclosed.toArray(new Stmt[0]);
    }

    private FunStmt nextFunStmt() throws IOException {
        final var stmt = new FunStmt();
        stmt.name = next().value;
        next(); // :
        stmt.type = next().value;
        next(); // ( -> params | { -> impl | ; -> end

        if (at("(")) {
            final List<Parameter> parameters = new Vector<>();
            next(); // skip (
            do {
                final var param = new Parameter();
                param.name = mToken.value;
                next(); // :
                param.type = next().value;
                parameters.add(param);
            } while (next().value.equals(","));
            next(); // skip )
            stmt.parameters = parameters.toArray(new Parameter[0]);
        }

        if (at(";")) {
            next(); // skip ;
            return stmt;
        }

        stmt.implementation = nextEnclosedStmt();

        return stmt;
    }

    private IfStmt nextIfStmt() throws IOException {
        final var stmt = new IfStmt();

        next(); // (
        next(); // skip (
        stmt.condition = nextExpr();
        next(); // skip )
        if (at("{"))
            stmt.thenStmt = nextEnclosedStmt();
        else
            stmt.thenStmt = new Stmt[] { nextStmt() };
        if (at("else")) {
            next(); // skip "else"
            if (at("{"))
                stmt.elseStmt = nextEnclosedStmt();
            else
                stmt.elseStmt = new Stmt[] { nextStmt() };
        }

        return stmt;
    }

    private RetStmt nextRetStmt() throws IOException {
        final var stmt = new RetStmt();

        next(); // skip "ret"
        stmt.value = nextExpr();
        next(); // skip ;

        return stmt;
    }

    private Expr nextExpr() throws IOException {
        return nextBinExprAnd();
    }

    private Expr nextBinExprAnd() throws IOException {
        var expr = nextBinExprOr();

        while (at("&")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            boolean assign = at("=");
            if (assign)
                next();
            else if (at("&")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            }

            binExpr.right = nextBinExprOr();

            if (assign)
                expr = new AssignExpr(((IdExpr) binExpr.left).name, binExpr);
            else
                expr = binExpr;
        }

        return expr;
    }

    private Expr nextBinExprOr() throws IOException {
        var expr = nextBinExprCmp();

        while (at("|")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            boolean assign = at("=");
            if (assign)
                next();
            else if (at("|")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            }

            binExpr.right = nextBinExprCmp();

            if (assign)
                expr = new AssignExpr(((IdExpr) binExpr.left).name, binExpr);
            else
                expr = binExpr;
        }

        return expr;
    }

    private Expr nextBinExprCmp() throws IOException {
        var expr = nextBinExprSum();

        while (at("=") || at("<") || at(">")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            boolean assign = false;
            if (at("=")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            } else if (binExpr.operator.equals("="))
                assign = true;

            binExpr.right = nextBinExprSum();

            if (assign)
                expr = new AssignExpr(((IdExpr) binExpr.left).name, binExpr.right);
            else
                expr = binExpr;
        }

        return expr;
    }

    private Expr nextBinExprSum() throws IOException {
        var expr = nextBinExprPro();

        while (at("+") || at("-")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            boolean assign = at("=");
            if (assign)
                next(); // skip operator

            binExpr.right = nextBinExprPro();

            if (assign)
                expr = new AssignExpr(((IdExpr) binExpr.left).name, binExpr);
            else
                expr = binExpr;
        }

        return expr;
    }

    private Expr nextBinExprPro() throws IOException {
        var expr = nextCallExpr();

        while (at("*") || at("/") || at("%")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            boolean assign = at("=");
            if (assign)
                next(); // skip operator

            binExpr.right = nextCallExpr();

            if (assign)
                expr = new AssignExpr(((IdExpr) binExpr.left).name, binExpr);
            else
                expr = binExpr;
        }

        return expr;
    }

    private Expr nextCallExpr() throws IOException {
        var expr = nextPrimExpr();

        if (at("(")) {
            final var callExpr = new CallExpr();
            callExpr.function = ((IdExpr) expr).name;

            final List<Expr> arguments = new Vector<>();
            do {
                next();
                arguments.add(nextExpr());
            } while (at(","));
            next(); // skip )
            callExpr.arguments = arguments.toArray(new Expr[0]);

            expr = callExpr;
        }

        return expr;
    }

    private Expr nextPrimExpr() throws IOException {

        var expr = switch (mToken.type) {
            case IDENTIFIER -> new IdExpr(mToken.value);
            case NUMBER -> new NumExpr(mToken.value);
            case STRING -> new StrExpr(mToken.value);
            default -> null;
        };

        next();

        return expr;
    }
}
