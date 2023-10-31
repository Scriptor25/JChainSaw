package io.scriptor.csaw.impl;

public class CSawException extends RuntimeException {

    public CSawException() {
        super();
    }

    public CSawException(Throwable t) {
        super(t);
    }

    public CSawException(String fmt, Object... args) {
        super(String.format(fmt, args));
    }
}
