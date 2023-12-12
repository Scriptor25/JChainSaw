package io.scriptor.csaw.impl;

import static io.scriptor.java.ErrorUtil.handle;
import static io.scriptor.java.ErrorUtil.handleVoid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import io.scriptor.csaw.impl.expr.AssignExpr;
import io.scriptor.csaw.impl.expr.BinExpr;
import io.scriptor.csaw.impl.expr.CallExpr;
import io.scriptor.csaw.impl.expr.ChrExpr;
import io.scriptor.csaw.impl.expr.ConExpr;
import io.scriptor.csaw.impl.expr.Expr;
import io.scriptor.csaw.impl.expr.IdExpr;
import io.scriptor.csaw.impl.expr.LambdaExpr;
import io.scriptor.csaw.impl.expr.MemExpr;
import io.scriptor.csaw.impl.expr.NumExpr;
import io.scriptor.csaw.impl.expr.StrExpr;
import io.scriptor.csaw.impl.expr.UnExpr;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.Interpreter;
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

public class Parser {

    private enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        CHAR,
        OPERATOR,
        EOF
    }

    private static class Token {

        public String value;
        public TokenType type;
        public final long line;

        public Token(long line) {
            this.line = line;
        }

        public Token value(String value) {
            this.value = value;
            return this;
        }

        public Token type(TokenType type) {
            this.type = type;
            return this;
        }

        public static Token EOF(long line) {
            return new Token(line).type(TokenType.EOF);
        }

        @Override
        public String toString() {
            return String.format("[ '%s' -> %s (%d) ]", value, type, line);
        }
    }

    public static void parse(InputStream stream, Environment env) {
        final var parser = new Parser(stream);

        parser.next();
        while (!parser.eof()) {
            final var stmt = parser.nextStmt(true);
            Interpreter.evaluate(env, stmt);
        }
    }

    private final BufferedReader mReader;
    private Token mToken;
    private long mLine = 1;

    private Parser(InputStream stream) {
        mReader = new BufferedReader(new InputStreamReader(stream));
    }

    private int read() {
        return handle(mReader::read);
    }

    private void mark(int readAheadLimit) {
        handleVoid(() -> mReader.mark(readAheadLimit));
    }

    private void reset() {
        handleVoid(mReader::reset);
    }

    private Token next() {
        int c = read();

        while (isIgnorable(c)) {
            if (c == '\n')
                mLine++;
            c = read();
        }

        if (c < 0)
            return mToken = Token.EOF(mLine);

        if (c == '#') {
            c = read();
            final char LIMIT = c == '#' ? '\n' : '#';
            while ((c = read()) != LIMIT && c >= 0)
                if (c == '\n')
                    mLine++;
            return next();
        }

        if (isAlpha(c) || c == '_') {
            final var builder = new StringBuilder();
            do {
                builder.append((char) c);
                mark(1);
                c = read();
            } while (isAlnum(c) || c == '_');
            reset();

            return mToken = new Token(mLine).type(TokenType.IDENTIFIER).value(builder.toString());
        }

        if (isDigit(c)) {
            final var builder = new StringBuilder();
            do {
                builder.append((char) c);
                mark(1);
                int p = c;
                c = read();
                if ((p == 'e' || p == 'E') && c == '-') {
                    builder.append((char) c);
                    mark(1);
                    c = read();
                }
            } while (isDigit(c) || c == '.' || c == 'e' || c == 'E');
            reset();

            return mToken = new Token(mLine).type(TokenType.NUMBER).value(builder.toString());
        }

        if (c == '"') {
            final var builder = new StringBuilder();
            c = read();
            while (c != '"' && c >= 0) {
                if (c == '\\') {
                    c = read();
                    switch (c) {
                        case 't':
                            builder.append('\t');
                            c = read();
                            continue;
                        case 'r':
                            builder.append('\r');
                            c = read();
                            continue;
                        case 'n':
                            builder.append('\n');
                            c = read();
                            continue;
                        case 'f':
                            builder.append('\f');
                            c = read();
                            continue;
                        case 'b':
                            builder.append('\b');
                            c = read();
                            continue;
                    }
                }

                builder.append((char) c);
                c = read();
            }

            return mToken = new Token(mLine).type(TokenType.STRING).value(builder.toString());
        }

        if (c == '\'') {
            final var builder = new StringBuilder();
            c = read();
            while (c != '\'' && c >= 0) {
                if (c == '\\') {
                    c = read();
                    switch (c) {
                        case 't':
                            builder.append('\t');
                            c = read();
                            continue;
                        case 'r':
                            builder.append('\r');
                            c = read();
                            continue;
                        case 'n':
                            builder.append('\n');
                            c = read();
                            continue;
                        case 'f':
                            builder.append('\f');
                            c = read();
                            continue;
                        case 'b':
                            builder.append('\b');
                            c = read();
                            continue;
                    }
                }

                builder.append((char) c);
                c = read();
            }

            return mToken = new Token(mLine).type(TokenType.CHAR).value(builder.toString());
        }

        return mToken = new Token(mLine).type(TokenType.OPERATOR).value(Character.toString(c));
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
        return !eof() && mToken.value.equals(value);
    }

    private boolean at(TokenType type) {
        return !eof() && mToken.type == type;
    }

    private boolean expect(String value) {
        if (at(value))
            return true;
        throw new CSawException("unexpected token %s, expected value '%s'", mToken, value);
    }

    private boolean expect(TokenType type) {
        if (at(type))
            return true;
        throw new CSawException("unexpected token %s, expected type %s", mToken, type);
    }

    private boolean expectAndNext(String value) {
        expect(value);
        next();
        return true;
    }

    private boolean expectAndNext(TokenType type) {
        expect(type);
        next();
        return true;
    }

    private boolean eof() {
        return mToken == null || mToken.type == TokenType.EOF;
    }

    private Stmt nextStmt(boolean semicolon) {

        if (at("{"))
            return nextEnclosedStmt();

        if (at("alias"))
            return nextAliasStmt(semicolon);

        if (at("for"))
            return nextForStmt(semicolon);

        if (at("@") || at("$"))
            return nextFunStmt(semicolon);

        if (at("if"))
            return nextIfStmt(semicolon);

        if (at("inc"))
            return nextIncStmt(semicolon);

        if (at("ret"))
            return nextRetStmt(semicolon);

        if (at("thing"))
            return nextThingStmt(semicolon);

        if (at("while"))
            return nextWhileStmt(semicolon);

        var expr = nextExpr();

        final var stmt = nextVarStmt(expr, semicolon);
        if (stmt != null)
            return stmt;

        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return expr;
    }

    private EnclosedStmt nextEnclosedStmt() {
        expectAndNext("{"); // skip {

        final List<Stmt> enclosed = new Vector<>();
        while (!eof() && !at("}")) {
            final var stmt = nextStmt(true);
            if (stmt != null)
                enclosed.add(stmt);
        }
        expectAndNext("}"); // skip }

        return new EnclosedStmt(enclosed.toArray(new Stmt[0]));
    }

    private AliasStmt nextAliasStmt(boolean semicolon) {
        final var stmt = new AliasStmt();

        expectAndNext("alias"); // skip "alias"
        stmt.alias = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip alias
        expectAndNext(":"); // skip :
        stmt.origin = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip origin
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return stmt;
    }

    private ForStmt nextForStmt(boolean semicolon) {
        final var stmt = new ForStmt();

        expectAndNext("for"); // skip "for"
        expectAndNext("("); // skip (
        if (!at(";"))
            stmt.begin = nextStmt(true);
        else
            next(); // skip ;
        stmt.condition = nextExpr();
        expectAndNext(";"); // skip ;
        if (!at(")"))
            stmt.loop = nextStmt(false);
        expectAndNext(")"); // skip )

        stmt.body = nextStmt(semicolon);

        return stmt;
    }

    private FunStmt nextFunStmt(boolean semicolon) {
        final var stmt = new FunStmt();

        stmt.constructor = at("$");
        if (stmt.constructor)
            next(); // skip $
        else
            expectAndNext("@"); // skip @

        if (at("(")) { // override operator
            next(); // skip (
            stmt.name = "";
            while (!eof() && !at(")")) { // at least one character
                stmt.name += mToken.value;
                expectAndNext(TokenType.OPERATOR); // skip operator
            }
            expectAndNext(")"); // skip )
        } else {
            stmt.name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name
        }

        if (stmt.constructor) {
            stmt.type = stmt.name;
        } else if (at(":")) {
            next(); // skip :
            stmt.type = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip type
        }

        if (at("(")) {
            final List<Parameter> parameters = new Vector<>();
            next(); // skip (
            while (!eof() && !at(")")) {
                final var param = new Parameter();
                param.name = mToken.value;
                expectAndNext(TokenType.IDENTIFIER); // skip name
                expectAndNext(":"); // skip :
                param.type = mToken.value;
                expectAndNext(TokenType.IDENTIFIER); // skip type
                parameters.add(param);
                if (!at(")"))
                    expectAndNext(","); // skip ,
            }
            expectAndNext(")"); // skip )
            stmt.parameters = parameters.toArray(new Parameter[0]);
        }

        if (at("$")) {
            next(); // skip $
            stmt.vararg = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip vararg name
        }

        if (at("-")) {
            next(); // skip -
            expectAndNext(">"); // skip >
            stmt.member = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip member
        }

        if (at(";")) {
            next(); // skip ;
            return stmt;
        }

        stmt.body = nextEnclosedStmt();

        return stmt;
    }

    private IfStmt nextIfStmt(boolean semicolon) {
        final var stmt = new IfStmt();

        expectAndNext("if"); // skip "if"
        expectAndNext("("); // skip (
        stmt.condition = nextExpr();
        expectAndNext(")"); // skip )

        stmt.thenBody = nextStmt(semicolon);

        if (at("else")) {
            next(); // skip "else"
            stmt.elseBody = nextStmt(semicolon);
        }

        return stmt;
    }

    private IncStmt nextIncStmt(boolean semicolon) {
        final var stmt = new IncStmt();

        expectAndNext("inc"); // skip "inc"
        stmt.path = mToken.value;
        expectAndNext(TokenType.STRING); // skip path
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return stmt;
    }

    private RetStmt nextRetStmt(boolean semicolon) {
        final var stmt = new RetStmt();

        expectAndNext("ret"); // skip "ret"
        stmt.value = nextExpr();
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return stmt;
    }

    private ThingStmt nextThingStmt(boolean semicolon) {
        final var stmt = new ThingStmt();

        expectAndNext("thing"); // skip "thing"
        expectAndNext(":"); // skip :
        stmt.name = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip name

        if (at(":")) {
            next(); // skip :
            stmt.group = mToken.value;
            expectAndNext(TokenType.IDENTIFIER);
        }

        if (at(";")) {
            next(); // skip ;
            return stmt;
        }

        final List<Parameter> fields = new Vector<>();
        expectAndNext("{"); // skip {
        while (!eof() && !at("}")) {
            final var param = new Parameter();
            param.name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name
            expectAndNext(":"); // skip :
            param.type = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip type
            fields.add(param);
            if (!at("}"))
                expectAndNext(","); // skip ,
        }
        expectAndNext("}"); // skip }
        stmt.fields = fields.toArray(new Parameter[0]);

        return stmt;
    }

    private WhileStmt nextWhileStmt(boolean semicolon) {
        final var stmt = new WhileStmt();

        expectAndNext("while"); // skip "while"
        expectAndNext("("); // skip (
        stmt.condition = nextExpr();
        expectAndNext(")"); // skip )

        stmt.body = nextStmt(semicolon);

        return stmt;
    }

    private VarStmt nextVarStmt(Expr type, boolean semicolon) {
        if (type instanceof IdExpr && at(TokenType.IDENTIFIER)) {
            final var stmt = new VarStmt();
            stmt.type = ((IdExpr) type).value;
            stmt.name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name

            if (at(";")) {
                next();
                return stmt;
            }

            expectAndNext("="); // skip =

            stmt.value = nextExpr();
            if (!eof() && semicolon)
                expectAndNext(";"); // skip ;

            return stmt;
        }

        return null;
    }

    private Expr nextExpr() {
        return nextConExpr();
    }

    private Expr nextConExpr() {
        var expr = nextBinExprAnd();

        while (at("?")) {
            next(); // skip ?
            final var thenExpr = nextExpr();
            expectAndNext(":"); // skip :
            final var elseExpr = nextExpr();

            expr = new ConExpr(expr, thenExpr, elseExpr);
        }

        return expr;
    }

    private Expr nextBinExprAnd() {
        var left = nextBinExprOr();

        while (at("&")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip =
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            } else if (at("&")) {
                operator += mToken.value;
                next(); // skip operator
            }

            left = new BinExpr(left, nextBinExprOr(), operator);
        }

        return left;
    }

    private Expr nextBinExprOr() {
        var left = nextBinExprXOr();

        while (at("|")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip =
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            } else if (at("|")) {
                operator += mToken.value;
                next(); // skip operator
            }

            left = new BinExpr(left, nextBinExprXOr(), operator);
        }

        return left;
    }

    private Expr nextBinExprXOr() {
        var left = nextBinExprCmp();

        while (at("^")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip =
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            }

            left = new BinExpr(left, nextBinExprCmp(), operator);
        }

        return left;
    }

    private Expr nextBinExprCmp() {
        var left = nextBinExprSum();

        while (at("=") || at("!") || at("<") || at(">")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                operator += mToken.value;
                next(); // skip operator
            } else if (operator.equals("=")) {
                left = new AssignExpr(left, nextExpr());
                continue;
            }

            left = new BinExpr(left, nextBinExprSum(), operator);
        }

        return left;
    }

    private Expr nextBinExprSum() {
        var left = nextBinExprPro();

        while (at("+") || at("-")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip =
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            } else if (at(operator)) {
                next(); // skip operator
                left = new AssignExpr(left, new BinExpr(left, new NumExpr(1), operator));
                continue;
            }

            left = new BinExpr(left, nextBinExprPro(), operator);
        }

        return left;
    }

    private Expr nextBinExprPro() {
        var left = nextBinIndexExpr();

        while (at("*") || at("/") || at("%")) {
            var operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip operator
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            }

            left = new BinExpr(left, nextBinIndexExpr(), operator);
        }

        return left;
    }

    private Expr nextBinIndexExpr() {
        var expr = nextCallExpr();

        while (at("[")) {
            next(); // skip [
            final var index = nextExpr();
            expectAndNext("]"); // skip ]
            expr = new BinExpr(expr, index, "[]");

            if (at("."))
                expr = nextMemExpr(expr);

            if (at("("))
                expr = nextCallExpr(expr);
        }

        return expr;
    }

    private Expr nextCallExpr() {
        return nextCallExpr(nextMemExpr());
    }

    private Expr nextCallExpr(Expr expr) {
        while (at("(")) {
            next(); // skip (
            final List<Expr> arguments = new Vector<>();
            while (!eof() && !at(")")) {
                arguments.add(nextExpr());
                if (!at(")"))
                    expectAndNext(","); // skip ,
            }
            expectAndNext(")"); // skip )

            expr = new CallExpr(expr, arguments.toArray(new Expr[0]));

            if (at("."))
                expr = nextMemExpr(expr);
        }

        return expr;
    }

    private Expr nextMemExpr() {
        return nextMemExpr(nextPrimExpr());
    }

    private Expr nextMemExpr(Expr expr) {
        while (at(".")) {
            next(); // skip .
            expr = new MemExpr(expr, ((IdExpr) nextPrimExpr()).value);
        }

        return expr;
    }

    private Expr nextPrimExpr() {
        if (eof())
            return null;

        switch (mToken.type) {
            case IDENTIFIER: {
                final var expr = new IdExpr(mToken.value);
                next(); // skip id
                return expr;
            }
            case NUMBER: {
                final var expr = new NumExpr(mToken.value);
                next(); // skip num
                return expr;
            }
            case STRING: {
                final var expr = new StrExpr(mToken.value);
                next(); // skip str
                return expr;
            }
            case CHAR: {
                final var expr = new ChrExpr(mToken.value.charAt(0));
                next(); // skip chr
                return expr;
            }
            default:
                break;
        }

        switch (mToken.value) {
            case "(": {
                next(); // skip (
                final var expr = nextExpr();
                expectAndNext(")"); // skip )
                return expr;
            }
            case "-": {
                next(); // skip -
                return new UnExpr("-", nextBinIndexExpr());
            }
            case "!": {
                next(); // skip !
                return new UnExpr("!", nextBinIndexExpr());
            }
            case "~": {
                next(); // skip ~
                return new UnExpr("~", nextBinIndexExpr());
            }

            case "[": { // lambda!
                next(); // skip [
                final List<IdExpr> passed = new Vector<>();
                while (!eof() && !at("]")) {
                    passed.add((IdExpr) nextPrimExpr());
                    if (!at("]"))
                        expectAndNext(","); // skip ,
                }
                expectAndNext("]"); // skip ]

                expectAndNext("("); // skip (
                final List<Parameter> parameters = new Vector<>();
                while (!eof() && !at(")")) {
                    final var param = new Parameter();
                    param.name = mToken.value;
                    expectAndNext(TokenType.IDENTIFIER); // skip name
                    expectAndNext(":"); // skip :
                    param.type = mToken.value;
                    expectAndNext(TokenType.IDENTIFIER); // skip type
                    parameters.add(param);
                    if (!at(")"))
                        expectAndNext(","); // skip ,
                }
                expectAndNext(")"); // skip )

                final var body = nextStmt(false);
                return new LambdaExpr(passed.toArray(new IdExpr[0]), parameters.toArray(new Parameter[0]), body);
            }
        }

        throw new CSawException("unhandled token %s", mToken);
    }
}
