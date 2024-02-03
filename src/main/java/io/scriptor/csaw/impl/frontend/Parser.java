package io.scriptor.csaw.impl.frontend;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;
import static io.scriptor.java.ErrorUtil.handle;
import static io.scriptor.java.ErrorUtil.handleVoid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.frontend.Token.TokenType;
import io.scriptor.csaw.impl.frontend.expr.AssignExpr;
import io.scriptor.csaw.impl.frontend.expr.BinExpr;
import io.scriptor.csaw.impl.frontend.expr.CallExpr;
import io.scriptor.csaw.impl.frontend.expr.ChrExpr;
import io.scriptor.csaw.impl.frontend.expr.ConExpr;
import io.scriptor.csaw.impl.frontend.expr.Expr;
import io.scriptor.csaw.impl.frontend.expr.IdExpr;
import io.scriptor.csaw.impl.frontend.expr.IndexExpr;
import io.scriptor.csaw.impl.frontend.expr.LambdaExpr;
import io.scriptor.csaw.impl.frontend.expr.MemExpr;
import io.scriptor.csaw.impl.frontend.expr.NumExpr;
import io.scriptor.csaw.impl.frontend.expr.StrExpr;
import io.scriptor.csaw.impl.frontend.expr.UnExpr;
import io.scriptor.csaw.impl.frontend.stmt.AliasStmt;
import io.scriptor.csaw.impl.frontend.stmt.EnclosedStmt;
import io.scriptor.csaw.impl.frontend.stmt.ForStmt;
import io.scriptor.csaw.impl.frontend.stmt.FunStmt;
import io.scriptor.csaw.impl.frontend.stmt.IfStmt;
import io.scriptor.csaw.impl.frontend.stmt.IncStmt;
import io.scriptor.csaw.impl.frontend.stmt.RetStmt;
import io.scriptor.csaw.impl.frontend.stmt.Stmt;
import io.scriptor.csaw.impl.frontend.stmt.ThingStmt;
import io.scriptor.csaw.impl.frontend.stmt.VarStmt;
import io.scriptor.csaw.impl.frontend.stmt.WhileStmt;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.Interpreter;
import io.scriptor.csaw.impl.interpreter.Type;

public class Parser {

    public static void parse(InputStream stream, Environment env, boolean output) {
        final var parser = new Parser(stream);

        parser.next();
        while (!parser.eof()) {
            final var stmt = parser.nextStmt(true);
            final var value = Interpreter.evaluate(env, stmt);
            if (output)
                System.out.println(value);
        }

        handleVoid(() -> stream.close());
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

            return mToken = new Token(TokenType.IDENTIFIER, builder.toString(), mLine);
        }

        if (isDigit(c)) {
            final var builder = new StringBuilder();
            do {
                builder.append((char) c);
                mark(1);
                int p = c;
                c = read();
                if (((p == 'e' || p == 'E') && c == '-')) {
                    builder.append((char) c);
                    mark(1);
                    c = read();
                }
            } while (isXDigit(c) || c == '.' || c == 'x' || c == 'b');
            reset();

            return mToken = new Token(TokenType.NUMBER, builder.toString(), mLine);
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

            return mToken = new Token(TokenType.STRING, builder.toString(), mLine);
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

            return mToken = new Token(TokenType.CHAR, builder.toString(), mLine);
        }

        return mToken = new Token(TokenType.OPERATOR, Character.toString(c), mLine);
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

    private static boolean isXDigit(int c) {
        return isDigit(c) || (c >= 0x41 && c <= 0x46) || (c >= 0x61 && c <= 0x66);
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

    private Type nextType() {
        return nextType(nextIndexExpr());
    }

    private Type nextType(Expr type) {
        if (type instanceof IdExpr t)
            return Type.get(t.value());
        if (type instanceof IndexExpr t) {
            if (!t.index().isConstant())
                throw new CSawException("cannot pre-evaluate non-constant expression!");
            final var size = Interpreter.evaluate(getGlobal(), t.index()).asNum().getInt();
            return Type.get(nextType(t.expr()), size);
        }

        throw new CSawException("unsupported type expression");
    }

    private Parameter nextParameter() {
        final var pname = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip name
        expectAndNext(":"); // skip :
        final var ptype = nextType();
        return new Parameter(pname, ptype);
    }

    private Stmt nextStmt(boolean semicolon) {

        if (at(";")) {
            next(); // skip ;
            return null;
        }

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

        final var expr = nextExpr();
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
        while (!eof() && !at("}"))
            enclosed.add(nextStmt(true));
        expectAndNext("}"); // skip }

        return new EnclosedStmt(enclosed.toArray(new Stmt[0]));
    }

    private AliasStmt nextAliasStmt(boolean semicolon) {
        String alias;
        Type origin;

        expectAndNext("alias"); // skip "alias"
        alias = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip alias
        expectAndNext(":"); // skip :
        origin = nextType();
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return new AliasStmt(alias, origin);
    }

    private ForStmt nextForStmt(boolean semicolon) {
        Stmt begin = null, loop = null;
        Expr condition;
        Stmt body;

        expectAndNext("for"); // skip "for"
        expectAndNext("("); // skip (
        if (!at(";"))
            begin = nextStmt(true);
        else
            next(); // skip ;
        condition = nextExpr();
        expectAndNext(";"); // skip ;
        if (!at(")"))
            loop = nextStmt(false);
        expectAndNext(")"); // skip )

        body = nextStmt(semicolon);

        return new ForStmt(begin, condition, loop, body);
    }

    private FunStmt nextFunStmt(boolean semicolon) {

        boolean constructor;
        String name, vararg = null;
        Type type = Type.getNull(), member = Type.getNull();
        Parameter[] parameters = new Parameter[0];
        EnclosedStmt body = null;

        constructor = at("$");
        if (constructor)
            next(); // skip $
        else
            expectAndNext("@"); // skip @

        if (at("(")) { // override operator
            next(); // skip (
            name = "";
            while (!eof() && !at(")")) { // at least one character
                name += mToken.value;
                expectAndNext(TokenType.OPERATOR); // skip operator
            }
            expectAndNext(")"); // skip )
        } else {
            name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name
        }

        if (constructor) {
            type = Type.get(name);
        } else if (at(":")) {
            next(); // skip :
            type = nextType();
        }

        if (at("(")) {
            final List<Parameter> params = new Vector<>();
            next(); // skip (
            while (!eof() && !at(")")) {
                params.add(nextParameter());
                if (!at(")"))
                    expectAndNext(","); // skip ,
            }
            expectAndNext(")"); // skip )
            parameters = params.toArray(new Parameter[0]);
        }

        if (at("$")) {
            next(); // skip $
            vararg = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip vararg name
        }

        if (at("-")) {
            next(); // skip -
            expectAndNext(">"); // skip >
            member = nextType();
        }

        if (at(";")) {
            next(); // skip ;
            return new FunStmt(constructor, name, type, parameters, vararg, member, body);
        }

        body = nextEnclosedStmt();

        return new FunStmt(constructor, name, type, parameters, vararg, member, body);
    }

    private IfStmt nextIfStmt(boolean semicolon) {
        Expr condition;
        Stmt thenBody, elseBody = null;

        expectAndNext("if"); // skip "if"
        expectAndNext("("); // skip (
        condition = nextExpr();
        expectAndNext(")"); // skip )

        thenBody = nextStmt(semicolon);

        if (at("else")) {
            next(); // skip "else"
            elseBody = nextStmt(semicolon);
        }

        return new IfStmt(condition, thenBody, elseBody);
    }

    private IncStmt nextIncStmt(boolean semicolon) {
        String path;

        expectAndNext("inc"); // skip "inc"
        path = mToken.value;
        expectAndNext(TokenType.STRING); // skip path
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return new IncStmt(path);
    }

    private RetStmt nextRetStmt(boolean semicolon) {
        Expr value = null;

        expectAndNext("ret"); // skip "ret"
        if (!at(";"))
            value = nextExpr();
        if (!eof() && semicolon)
            expectAndNext(";"); // skip ;

        return new RetStmt(value);
    }

    private ThingStmt nextThingStmt(boolean semicolon) {
        String name, group = "";
        Parameter[] fields = null;

        expectAndNext("thing"); // skip "thing"
        expectAndNext(":"); // skip :
        name = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip name

        if (at(":")) {
            next(); // skip :
            group = mToken.value;
            expectAndNext(TokenType.IDENTIFIER);
        }

        if (at(";")) {
            next(); // skip ;
            return new ThingStmt(name, group, fields);
        }

        final List<Parameter> flds = new Vector<>();
        expectAndNext("{"); // skip {
        while (!eof() && !at("}")) {
            flds.add(nextParameter());
            if (!at("}"))
                expectAndNext(","); // skip ,
        }
        expectAndNext("}"); // skip }
        fields = flds.toArray(new Parameter[0]);

        return new ThingStmt(name, group, fields);
    }

    private WhileStmt nextWhileStmt(boolean semicolon) {
        Expr condition;
        Stmt body;

        expectAndNext("while"); // skip "while"
        expectAndNext("("); // skip (
        condition = nextExpr();
        expectAndNext(")"); // skip )

        body = nextStmt(semicolon);

        return new WhileStmt(condition, body);
    }

    private VarStmt nextVarStmt(Expr expr, boolean semicolon) {
        if ((expr instanceof IdExpr || expr instanceof IndexExpr) && at(TokenType.IDENTIFIER)) {

            Type type;
            String name;
            Expr value = null;

            type = nextType(expr);
            name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name

            if (at(";")) {
                next();
                return new VarStmt(type, name, value);
            }

            expectAndNext("="); // skip =

            value = nextExpr();
            if (!eof() && semicolon)
                expectAndNext(";"); // skip ;

            return new VarStmt(type, name, value);
        }

        return null;
    }

    private Expr nextExpr() {
        return nextConExpr().makeConstant();
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

            if (at(operator) || at("=")) {
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
        var left = nextCallExpr();

        while (at("*") || at("/") || at("%")) {
            var operator = mToken.value;
            next(); // skip operator

            if (at("=")) {
                next(); // skip operator
                left = new AssignExpr(left, new BinExpr(left, nextExpr(), operator));
                continue;
            }

            left = new BinExpr(left, nextCallExpr(), operator);
        }

        return left;
    }

    private Expr nextCallExpr() {
        var expr = nextIndexExpr();

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

            if (at("["))
                expr = nextIndexExpr(expr);

            if (at("."))
                expr = nextMemExpr(expr);
        }

        return expr;
    }

    private Expr nextIndexExpr() {
        return nextIndexExpr(nextMemExpr());
    }

    private Expr nextIndexExpr(Expr expr) {
        while (at("[")) {
            next(); // skip [
            final var index = nextExpr();
            expectAndNext("]"); // skip ]
            expr = new IndexExpr(expr, index);

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
            expr = new MemExpr(expr, mToken.value);
            expectAndNext(TokenType.IDENTIFIER);
        }

        return expr;
    }

    private Expr nextPrimExpr() {
        if (eof())
            throw new CSawException("reached end of file in incomplete state");

        switch (mToken.type) {
            case IDENTIFIER: {
                final var expr = new IdExpr(mToken.value);
                next(); // skip id
                return expr;
            }
            case NUMBER: {
                Expr expr;
                if (mToken.value.startsWith("0x"))
                    expr = new NumExpr(mToken.value.substring(2), 16);
                else if (mToken.value.startsWith("0b"))
                    expr = new NumExpr(mToken.value.substring(2), 2);
                else
                    expr = new NumExpr(mToken.value, 10);
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
                return new UnExpr("-", nextCallExpr());
            }
            case "!": {
                next(); // skip !
                return new UnExpr("!", nextCallExpr());
            }
            case "~": {
                next(); // skip ~
                return new UnExpr("~", nextCallExpr());
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
                    parameters.add(nextParameter());
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
