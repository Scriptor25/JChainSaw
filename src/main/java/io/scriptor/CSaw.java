package io.scriptor;

import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.hasFunction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.scriptor.csaw.impl.frontend.Parser;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstNull;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.java.Collector;
import io.scriptor.java.ErrorUtil;

public class CSaw {

    public static void main(String[] args) {

        final Map<String, String> options = new HashMap<>();

        int i = 0;
        for (; i < args.length; i++) {
            final var arg = args[i];
            if (!arg.startsWith("-"))
                break;

            final var split = arg.split("=");
            if (split.length == 1)
                options.put(arg, "");
            else
                options.put(split[0], split[1]);
        }

        if (args.length == 0 || options.containsKey("--help") || options.containsKey("-h")) {
            System.out.println("csaw [OPTIONS...] [FILE] [ARGS...]");
            System.out.println("Options:");
            System.out.println("--help, -h: show csaw help");
            System.out.println("--shell, -sh: open csaw shell");
            return;
        }

        if (options.containsKey("--shell") || options.containsKey("-sh")) {
            shell();
            return;
        }

        run(args[i], Arrays.copyOfRange(args, i + 1, args.length));
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
                case "path":
                    System.out.println(env.getPath());
                    continue;
                case "reset":
                    Environment.reset();
                    Collector.collect(env);
                    continue;
                default:
                    try {
                        Parser.parse(new ByteArrayInputStream(input.getBytes()), env, true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    break;
            }
        }
    }

    public static void run(String path, String[] args) {
        final var file = new File(path);

        final var env = Environment.initGlobal(file.getParent());
        Collector.collect(env);

        Parser.parse(ErrorUtil.handle(() -> new FileInputStream(file)), env, false);

        if (hasFunction(Type.getNull(), "main")) {
            final var argv = Arrays.stream(args).map(arg -> new ConstStr(arg)).toArray(size -> new ConstStr[size]);
            System.out.printf("Exit Code %s%n",
                    getAndInvoke(new ConstNull("call of main"), "main", argv));
        }
    }

}