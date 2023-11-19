package io.scriptor.java;

import io.scriptor.csaw.impl.CSawException;

public class ErrorUtil {

    @FunctionalInterface
    public static interface ICatchable<T> {
        T run() throws Throwable;
    }

    @FunctionalInterface
    public static interface ICatchableVoid {
        void run() throws Throwable;
    }

    public static <T> T handle(ICatchable<T> catchIt) {
        try {
            return catchIt.run();
        } catch (Throwable e) {
            throw new CSawException(e);
        }
    }

    public static void handleVoid(ICatchableVoid catchVoid) {
        try {
            catchVoid.run();
        } catch (Throwable e) {
            throw new CSawException(e);
        }
    }

    public static <T> T error(String fmt, Object... args) {
        throw new CSawException(fmt, args);
    }
}
