package io.scriptor;

import io.scriptor.value.ChrValue;
import io.scriptor.value.ListValue;
import io.scriptor.value.NumValue;
import io.scriptor.value.StrValue;
import io.scriptor.value.Value;

public class Natives {

    private Natives() {
    }

    public static NumValue random(Value member, Environment env, Value... args) {
        return new NumValue(Math.random());
    }

    public static NumValue inf(Value member, Environment env, Value... args) {
        return new NumValue(Double.MAX_VALUE);
    }

    public static NumValue floor(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.floor(x));
    }

    public static NumValue sin(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.sin(x));
    }

    public static NumValue cos(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.cos(x));
    }

    public static NumValue tan(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.tan(x));
    }

    public static NumValue sqrt(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.sqrt(x));
    }

    public static Value out(Value member, Environment env, Value... args) {
        final var fmt = ((StrValue) args[0]).getValue();
        final var newargs = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++)
            newargs[i - 1] = args[i].getValue();
        System.out.printf(fmt, newargs);
        return null;
    }

    public static StrValue in(Value member, Environment env, Value... args) {
        final var fmt = ((StrValue) args[0]).getValue();
        final var newargs = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++)
            newargs[i - 1] = args[i].getValue();
        return new StrValue(System.console().readLine(fmt, newargs));
    }

    public static NumValue num(Value member, Environment env, Value... args) {
        final var x = ((StrValue) args[0]).getValue();
        return new NumValue(Double.parseDouble(x));
    }

    public static NumValue length(Value member, Environment env, Value... args) {
        final var str = ((StrValue) member).getValue();
        return new NumValue(str.length());
    }

    public static ChrValue at(Value member, Environment env, Value... args) {
        final var str = ((StrValue) member).getValue();
        final var idx = (int) (double) ((NumValue) args[0]).getValue();
        return new ChrValue(str.charAt(idx));
    }

    public static Value add(Value member, Environment env, Value... args) {
        final var list = (ListValue) member;
        list.add(args[0]);
        return null;
    }

    public static Value file(Value member, Environment env, Value... args) {
        return null;
    }
}
