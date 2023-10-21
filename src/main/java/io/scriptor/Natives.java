package io.scriptor;

import java.io.IOException;

import io.scriptor.value.ChrValue;
import io.scriptor.value.FileValue;
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

    public static NumValue abs(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        return new NumValue(Math.abs(x));
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

    public static NumValue pow(Value member, Environment env, Value... args) {
        final var x = ((NumValue) args[0]).getValue();
        final var y = ((NumValue) args[1]).getValue();
        return new NumValue(Math.pow(x, y));
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

    public static NumValue str_length(Value member, Environment env, Value... args) {
        final var str = ((StrValue) member).getValue();
        return new NumValue(str.length());
    }

    public static ChrValue str_at(Value member, Environment env, Value... args) {
        final var str = ((StrValue) member).getValue();
        final var idx = (int) (double) ((NumValue) args[0]).getValue();
        return new ChrValue(str.charAt(idx));
    }

    public static Value list_add(Value member, Environment env, Value... args) {
        final var list = (ListValue) member;
        list.add(args[0]);
        return null;
    }

    public static Value list_get(Value member, Environment env, Value... args) {
        final var list = (ListValue) member;
        return list.get((int) (double) ((NumValue) args[0]).getValue());
    }

    public static Value list_size(Value member, Environment env, Value... args) {
        final var list = (ListValue) member;
        return new NumValue(list.size());
    }

    public static Value file(Value member, Environment env, Value... args) {
        final var path = ((StrValue) args[0]).getValue();
        try {
            return new FileValue(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Value file_out(Value member, Environment env, Value... args) {
        final var fmt = ((StrValue) args[0]).getValue();
        final var newargs = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++)
            newargs[i - 1] = args[i].getValue();
        try {
            ((FileValue) member).out(String.format(fmt, newargs));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Value file_close(Value member, Environment env, Value... args) {
        try {
            ((FileValue) member).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
