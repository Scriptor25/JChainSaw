package io.scriptor;

import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.hasFunction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import io.scriptor.csaw.impl.Parser;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.Collector;
import io.scriptor.java.ErrorUtil;

public class CSaw {

    public static void main(String[] args) {

        switch (args.length) {
            case 0:
                shell();
                break;

            case 1:
                run(args[0], Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                System.out.println("wrong number arguments, usage: \"csaw\" or \"csaw <filepath>\"");
                break;
        }
    }

    public static void shell() {
        final var env = Environment.initGlobal(System.getProperty("user.dir"));
        Collector.collect(env);

        while (true) {
            final var input = System.console().readLine(">> ").trim();

            switch (input) {
                case "exit":
                    return;
                case "clear":
                    System.out.print("\033\143");
                    continue;
                case "env":
                    System.out.println(env.getPath());
                    continue;
                case "reset":
                    Environment.reset();
                    Collector.collect(env);
                    continue;
            }

            try {
                Parser.parse(new ByteArrayInputStream(input.getBytes()), env);
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            }
        }
    }

    public static void run(String path, String[] args) {
        final var file = new File(path);

        final var env = Environment.initGlobal(file.getParent());
        Collector.collect(env);

        Parser.parse(ErrorUtil.handle(() -> new FileInputStream(file)), env);

        Value exit;
        if (hasFunction(null, "main")) {
            exit = getAndInvoke(null, "main");
        } else {
            final var argc = new NumValue(args.length);
            final var argv = Arrays.stream(args).map(arg -> new StrValue(arg)).toArray(size -> new StrValue[size]);
            final var valargs = new Value[1 + argv.length];
            valargs[0] = argc;
            for (int i = 0; i < argv.length; i++)
                valargs[i + 1] = argv[i];

            exit = getAndInvoke(null, "main", valargs);
        }

        System.out.printf("Exit Code %s%n", exit);
    }

}