package io.scriptor.csaw.lang;

import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("std")
public class CSawStd {

    private CSawStd() {
    }

    public static ConstNum random() {
        return new ConstNum(Math.random());
    }

    public static ConstNum infinity() {
        return new ConstNum(Double.MAX_VALUE);
    }

    public static ConstNum floor(ConstNum x) {
        return new ConstNum(Math.floor(x.get()));
    }

    public static ConstNum abs(ConstNum x) {
        return new ConstNum(Math.abs(x.get()));
    }

    public static ConstNum sin(ConstNum x) {
        return new ConstNum(Math.sin(x.get()));
    }

    public static ConstNum cos(ConstNum x) {
        return new ConstNum(Math.cos(x.get()));
    }

    public static ConstNum tan(ConstNum x) {
        return new ConstNum(Math.tan(x.get()));
    }

    public static ConstNum asin(ConstNum x) {
        return new ConstNum(Math.asin(x.get()));
    }

    public static ConstNum acos(ConstNum x) {
        return new ConstNum(Math.acos(x.get()));
    }

    public static ConstNum atan(ConstNum x) {
        return new ConstNum(Math.atan(x.get()));
    }

    public static ConstNum atan2(ConstNum y, ConstNum x) {
        return new ConstNum(Math.atan2(y.get(), x.get()));
    }

    public static ConstNum sqrt(ConstNum x) {
        return new ConstNum(Math.sqrt(x.get()));
    }

    public static ConstNum pow(ConstNum x, ConstNum y) {
        return new ConstNum(Math.pow(x.get(), y.get()));
    }

    public static void out(ConstStr fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getObject();
        System.out.printf(fmt.get(), objArgs);
    }

    public static ConstStr in(ConstStr fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getObject();
        return new ConstStr(System.console().readLine(fmt.get(), objArgs));
    }

    public static ConstNum num(ConstStr x) {
        return new ConstNum(Double.parseDouble(x.get()));
    }

    public static ConstNum time() {
        return new ConstNum(System.currentTimeMillis());
    }
}
