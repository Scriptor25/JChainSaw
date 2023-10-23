package io.scriptor.java;

public class ErrorUtil {

    @FunctionalInterface
    public static interface ICatchIt<T, E extends Throwable> {

        T run() throws E;
    }

    @FunctionalInterface
    public static interface ICatchVoid<E extends Throwable> {

        void run() throws E;
    }

    public static <T, E extends Throwable> T tryCatch(ICatchIt<T, E> catchIt) {
        try {
            return catchIt.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Throwable> void tryCatchVoid(ICatchVoid<E> catchVoid) {
        try {
            catchVoid.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
