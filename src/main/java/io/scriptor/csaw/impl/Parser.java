package io.scriptor.csaw.impl;

import java.io.BufferedReader;
import java.io.IOException;
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
import io.scriptor.csaw.impl.expr.MemExpr;
import io.scriptor.csaw.impl.expr.NumExpr;
import io.scriptor.csaw.impl.expr.StrExpr;
import io.scriptor.csaw.impl.expr.UnExpr;
import io.scriptor.csaw.impl.stmt.AliasStmt;
import io.scriptor.csaw.impl.stmt.ForStmt;
import io.scriptor.csaw.impl.stmt.FunStmt;
import io.scriptor.csaw.impl.stmt.IfStmt;
import io.scriptor.csaw.impl.stmt.IncStmt;
import io.scriptor.csaw.impl.stmt.ParStmt;
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

        @Override
        public String toString() {
            return String.format("[ '%s' -> %s ]", value, type);
        }
    }

    private final BufferedReader mReader;
    private final Environment mEnvironment;
    private Token mToken;
    private int mLine = 1;

    public Parser(InputStream stream, Environment env) throws IOException {
        mReader = new BufferedReader(new InputStreamReader(stream));
        mEnvironment = env;
    }

    public boolean start() {
        try {
            next(); // prepare first token
            while (mToken.type != TokenType.EOF) {
                final var stmt = nextStmt(true);
                // System.out.println(stmt);
                /* final var value = */
                Interpreter.evaluate(mEnvironment, stmt);
                // System.out.println(value);
            }
            return true;
        } catch (Exception e) {
            System.err.printf("At line %d: %s%n", mLine, e.getMessage());
            return false;
        }
    }

    private Token next() throws IOException {
        int c = mReader.read();

        while (isIgnorable(c)) {
            if (c == '\n')
                mLine++;
            c = mReader.read();
        }

        if (c < 0)
            return mToken = Token.EOF();

        if (c == '#') {
            c = mReader.read();
            final char LIMIT = c == '#' ? '\n' : '#';
            while ((c = mReader.read()) != LIMIT && c >= 0)
                if (c == '\n')
                    mLine++;
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
                int p = c;
                c = mReader.read();
                if ((p == 'e' || p == 'E') && c == '-') {
                    builder.append((char) c);
                    mReader.mark(1);
                    c = mReader.read();
                }
            } while (isDigit(c) || c == '.' || c == 'e' || c == 'E');
            mReader.reset();

            return mToken = new Token().type(TokenType.NUMBER).value(builder.toString());
        }

        if (c == '"') {
            final var builder = new StringBuilder();
            c = mReader.read();
            while (c != '"' && c >= 0) {
                if (c == '\\') {
                    c = mReader.read();
                    switch (c) {
                        case 't':
                            builder.append('\t');
                            c = mReader.read();
                            continue;
                        case 'r':
                            builder.append('\r');
                            c = mReader.read();
                            continue;
                        case 'n':
                            builder.append('\n');
                            c = mReader.read();
                            continue;
                        case 'f':
                            builder.append('\f');
                            c = mReader.read();
                            continue;
                        case 'b':
                            builder.append('\b');
                            c = mReader.read();
                            continue;
                    }
                }

                builder.append((char) c);
                c = mReader.read();
            }

            return mToken = new Token().type(TokenType.STRING).value(builder.toString());
        }

        if (c == '\'') {
            final var builder = new StringBuilder();
            c = mReader.read();
            while (c != '\'' && c >= 0) {
                if (c == '\\') {
                    c = mReader.read();
                    switch (c) {
                        case 't':
                            builder.append('\t');
                            c = mReader.read();
                            continue;
                        case 'r':
                            builder.append('\r');
                            c = mReader.read();
                            continue;
                        case 'n':
                            builder.append('\n');
                            c = mReader.read();
                            continue;
                        case 'f':
                            builder.append('\f');
                            c = mReader.read();
                            continue;
                        case 'b':
                            builder.append('\b');
                            c = mReader.read();
                            continue;
                    }
                }

                builder.append((char) c);
                c = mReader.read();
            }

            return mToken = new Token().type(TokenType.CHAR).value(builder.toString());
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
        return !eof() && mToken.value.equals(value);
    }

    private boolean at(TokenType type) {
        return !eof() && mToken.type == type;
    }

    private boolean expect(String value) {
        if (at(value))
            return true;
        throw new IllegalStateException(String.format("unexpected token %s, expected value '%s'", mToken, value));
    }

    private boolean expect(TokenType type) {
        if (at(type))
            return true;
        throw new IllegalStateException(String.format("unexpected token %s, expected type %s", mToken, type));
    }

    private boolean expectAndNext(String value) throws IOException {
        expect(value);
        next();
        return true;
    }

    private boolean expectAndNext(TokenType type) throws IOException {
        expect(type);
        next();
        return true;
    }

    private boolean eof() {
        return mToken == null || mToken.type == TokenType.EOF;
    }

    private Stmt nextStmt(boolean semicolon) throws Exception {

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

        if (at("par"))
            return nextParStmt(semicolon);

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

        if (semicolon)
            expectAndNext(";"); // skip ;

        return expr;
    }

    private Stmt[] nextEnclosedStmt() throws Exception {
        expectAndNext("{"); // skip {

        final List<Stmt> enclosed = new Vector<>();
        while (!eof() && !at("}")) {
            enclosed.add(nextStmt(true));
        }

        expectAndNext("}"); // skip }

        return enclosed.toArray(new Stmt[0]);
    }

    private AliasStmt nextAliasStmt(boolean semicolon) throws IOException {
        final var stmt = new AliasStmt();

        expectAndNext("alias"); // skip "alias"
        stmt.alias = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip alias
        expectAndNext(":"); // skip :
        stmt.origin = mToken.value;
        expectAndNext(TokenType.IDENTIFIER); // skip origin
        if (semicolon)
            expectAndNext(";"); // skip ;

        return stmt;
    }

    private ForStmt nextForStmt(boolean semicolon) throws Exception {
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

        if (at("{"))
            stmt.body = nextEnclosedStmt();
        else
            stmt.body = new Stmt[] { nextStmt(semicolon) };

        return stmt;
    }

    private FunStmt nextFunStmt(boolean semicolon) throws Exception {
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
            expectAndNext("("); // skip (
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
            stmt.vararg = true;
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

    private IfStmt nextIfStmt(boolean semicolon) throws Exception {
        final var stmt = new IfStmt();

        expectAndNext("if"); // skip "if"
        expectAndNext("("); // skip (
        stmt.condition = nextExpr();
        expectAndNext(")"); // skip )

        if (at("{"))
            stmt.thenBody = nextEnclosedStmt();
        else
            stmt.thenBody = new Stmt[] { nextStmt(semicolon) };

        if (at("else")) {
            next(); // skip "else"
            if (at("{"))
                stmt.elseBody = nextEnclosedStmt();
            else
                stmt.elseBody = new Stmt[] { nextStmt(semicolon) };
        }

        return stmt;
    }

    private IncStmt nextIncStmt(boolean semicolon) throws IOException {
        final var stmt = new IncStmt();

        expectAndNext("inc"); // skip "inc"
        stmt.path = mToken.value;
        expectAndNext(TokenType.STRING); // skip path
        expectAndNext(";"); // skip ;

        return stmt;
    }

    private ParStmt nextParStmt(boolean semicolon) throws Exception {
        final var stmt = new ParStmt();

        expectAndNext("par"); // skip "par"
        expectAndNext("("); // skip (
        stmt.from = nextExpr();
        expectAndNext(";"); // skip ;
        stmt.length = nextExpr();
        expectAndNext(";"); // skip ;
        stmt.variable = mToken.value;
        expectAndNext(TokenType.IDENTIFIER);
        expectAndNext(")"); // skip )

        if (at("{"))
            stmt.body = nextEnclosedStmt();
        else
            stmt.body = new Stmt[] { nextStmt(semicolon) };

        return stmt;
    }

    private RetStmt nextRetStmt(boolean semicolon) throws IOException {
        final var stmt = new RetStmt();

        expectAndNext("ret"); // skip "ret"
        stmt.value = nextExpr();
        expectAndNext(";"); // skip ;

        return stmt;
    }

    private ThingStmt nextThingStmt(boolean semicolon) throws IOException {
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

    private WhileStmt nextWhileStmt(boolean semicolon) throws Exception {
        final var stmt = new WhileStmt();

        expectAndNext("while"); // skip "while"
        expectAndNext("("); // skip (
        stmt.condition = nextExpr();
        expectAndNext(")"); // skip )
        if (at("{"))
            stmt.body = nextEnclosedStmt();
        else
            stmt.body = new Stmt[] { nextStmt(semicolon) };

        return stmt;
    }

    private VarStmt nextVarStmt(Expr type, boolean semicolon) throws IOException {
        if (type instanceof IdExpr && at(TokenType.IDENTIFIER)) {
            final var varExpr = new VarStmt();
            varExpr.type = ((IdExpr) type).name;
            varExpr.name = mToken.value;
            expectAndNext(TokenType.IDENTIFIER); // skip name

            if (at(";")) {
                next();
                return varExpr;
            }

            expectAndNext("="); // skip =
            varExpr.value = nextExpr();
            if (semicolon)
                expectAndNext(";"); // skip ;
            return varExpr;
        }

        return null;
    }

    private Expr nextExpr() throws IOException {
        return nextConExpr();
    }

    private Expr nextConExpr() throws IOException {
        var expr = nextBinExprAnd();

        while (at("?")) {
            final var conExpr = new ConExpr();
            conExpr.condition = expr;
            next(); // skip ?
            conExpr.thenExpr = nextExpr();
            expectAndNext(":"); // skip :
            conExpr.elseExpr = nextExpr();

            expr = conExpr;
        }

        return expr;
    }

    private Expr nextBinExprAnd() throws IOException {
        var expr = nextBinExprOr();

        while (at("&")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                next(); // skip =
                binExpr.right = nextExpr();
                expr = new AssignExpr(expr, binExpr);
                continue;
            } else if (at("&")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            }

            binExpr.right = nextBinExprOr();
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
            if (at("=")) {
                next(); // skip =
                binExpr.right = nextExpr();
                expr = new AssignExpr(expr, binExpr);
                continue;
            } else if (at("|")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            }

            binExpr.right = nextBinExprCmp();
            expr = binExpr;
        }

        return expr;
    }

    private Expr nextBinExprCmp() throws IOException {
        var expr = nextBinExprSum();

        while (at("=") || at("!") || at("<") || at(">")) {
            final var binExpr = new BinExpr();
            binExpr.left = expr;
            binExpr.operator = mToken.value;

            next(); // skip operator
            if (at("=")) {
                binExpr.operator += mToken.value;
                next(); // skip operator
            } else if (binExpr.operator.equals("=")) {
                expr = new AssignExpr(expr, nextExpr());
                continue;
            }

            binExpr.right = nextBinExprSum();
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
            if (at("=")) {
                next(); // skip operator
                binExpr.right = nextExpr();
                expr = new AssignExpr(expr, binExpr);
                continue;
            } else if (at(binExpr.operator)) {
                next(); // skip operator
                binExpr.right = new NumExpr(1);
                expr = new AssignExpr(expr, binExpr);
                continue;
            }

            binExpr.right = nextBinExprPro();
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
            if (at("=")) {
                next(); // skip operator
                binExpr.right = nextExpr();
                expr = new AssignExpr(expr, binExpr);
                continue;
            }

            binExpr.right = nextCallExpr();
            expr = binExpr;
        }

        return expr;
    }

    private Expr nextCallExpr() throws IOException {
        var expr = nextMemExpr();

        while (at("(")) {
            final var callExpr = new CallExpr();
            callExpr.function = expr;

            final List<Expr> arguments = new Vector<>();
            next(); // skip (
            while (!eof() && !at(")")) {
                arguments.add(nextExpr());
                if (!at(")"))
                    expectAndNext(","); // skip ,
            }
            expectAndNext(")"); // skip )
            callExpr.arguments = arguments.toArray(new Expr[0]);

            expr = callExpr;

            if (at("."))
                expr = nextMemExpr(expr);
        }

        return expr;
    }

    private Expr nextMemExpr() throws IOException {
        return nextMemExpr(nextPrimExpr());
    }

    private Expr nextMemExpr(Expr expr) throws IOException {
        while (at(".")) {
            final var memExpr = new MemExpr();
            memExpr.object = expr;
            next(); // skip .
            memExpr.member = ((IdExpr) nextPrimExpr()).name;

            expr = memExpr;
        }

        return expr;
    }

    private Expr nextPrimExpr() throws IOException {
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
        }

        throw new IllegalStateException(String.format("unhandled token %s", mToken));
    }
}
