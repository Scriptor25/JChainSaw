package io.scriptor.csaw.impl.frontend;

public class Token {

    public enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        CHAR,
        OPERATOR,
        EOF
    }

    public final TokenType type;
    public final String value;
    public final long line;

    public Token(TokenType type, String value, long line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public static Token EOF(long line) {
        return new Token(TokenType.EOF, null, line);
    }

    @Override
    public String toString() {
        return String.format("[ '%s' -> %s (%d) ]", value, type, line);
    }
}
