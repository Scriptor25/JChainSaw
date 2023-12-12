package io.scriptor.csaw.lang;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("std")
public class CSawStd {

    private CSawStd() {
    }

    public static NumValue random() {
        return new NumValue(Math.random());
    }

    public static NumValue infinity() {
        return new NumValue(Double.MAX_VALUE);
    }

    public static NumValue floor(NumValue x) {
        return new NumValue(Math.floor(x.get()));
    }

    public static NumValue abs(NumValue x) {
        return new NumValue(Math.abs(x.get()));
    }

    public static NumValue sin(NumValue x) {
        return new NumValue(Math.sin(x.get()));
    }

    public static NumValue cos(NumValue x) {
        return new NumValue(Math.cos(x.get()));
    }

    public static NumValue tan(NumValue x) {
        return new NumValue(Math.tan(x.get()));
    }

    public static NumValue asin(NumValue x) {
        return new NumValue(Math.asin(x.get()));
    }

    public static NumValue acos(NumValue x) {
        return new NumValue(Math.acos(x.get()));
    }

    public static NumValue atan(NumValue x) {
        return new NumValue(Math.atan(x.get()));
    }

    public static NumValue atan2(NumValue y, NumValue x) {
        return new NumValue(Math.atan2(y.get(), x.get()));
    }

    public static NumValue sqrt(NumValue x) {
        return new NumValue(Math.sqrt(x.get()));
    }

    public static NumValue pow(NumValue x, NumValue y) {
        return new NumValue(Math.pow(x.get(), y.get()));
    }

    public static void out(StrValue fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getValue();
        System.out.printf(fmt.getValue(), objArgs);
    }

    public static StrValue in(StrValue fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getValue();
        return new StrValue(System.console().readLine(fmt.getValue(), objArgs));
    }

    public static NumValue num(StrValue x) {
        return new NumValue(Double.parseDouble(x.getValue()));
    }

    public static NumValue time() {
        return new NumValue(System.currentTimeMillis());
    }
}
