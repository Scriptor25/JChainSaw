package io.scriptor.csaw.lang;

import io.scriptor.csaw.impl.value.NumValue;
import io.scriptor.csaw.impl.value.StrValue;
import io.scriptor.csaw.impl.value.Value;
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
        return new NumValue(Math.floor(x.getValue()));
    }

    public static NumValue abs(NumValue x) {
        return new NumValue(Math.abs(x.getValue()));
    }

    public static NumValue sin(NumValue x) {
        return new NumValue(Math.sin(x.getValue()));
    }

    public static NumValue cos(NumValue x) {
        return new NumValue(Math.cos(x.getValue()));
    }

    public static NumValue tan(NumValue x) {
        return new NumValue(Math.tan(x.getValue()));
    }

    public static NumValue asin(NumValue x) {
        return new NumValue(Math.asin(x.getValue()));
    }

    public static NumValue acos(NumValue x) {
        return new NumValue(Math.acos(x.getValue()));
    }

    public static NumValue atan(NumValue x) {
        return new NumValue(Math.atan(x.getValue()));
    }

    public static NumValue atan2(NumValue y, NumValue x) {
        return new NumValue(Math.atan2(y.getValue(), x.getValue()));
    }

    public static NumValue sqrt(NumValue x) {
        return new NumValue(Math.sqrt(x.getValue()));
    }

    public static NumValue pow(NumValue x, NumValue y) {
        return new NumValue(Math.pow(x.getValue(), y.getValue()));
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
}
