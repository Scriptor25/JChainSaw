package io.scriptor;

import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.hasFunction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import io.scriptor.csaw.impl.Parser;
import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.value.ConstNull;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.java.Collector;
import io.scriptor.java.ErrorUtil;

public class CSaw {

    public static void main(String[] args) {
        if (args.length == 0) {
            shell();
            return;
        }

        switch (args[0]) {
            case "-h":
            case "--help":
                System.out.println("csaw shell: csaw");
                System.out.println("run file: csaw <file> [args]");
                return;
        }

        run(args[0], Arrays.copyOfRange(args, 1, args.length));
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
                t.printStackTrace();
            }
        }
    }

    public static void run(String path, String[] args) {
        final var file = new File(path);

        final var env = Environment.initGlobal(file.getParent());
        Collector.collect(env);

        Parser.parse(ErrorUtil.handle(() -> new FileInputStream(file)), env);

        if (hasFunction(Type.getNull(), "main")) {
            final var argv = Arrays.stream(args).map(arg -> new ConstStr(arg)).toArray(size -> new ConstStr[size]);
            System.out.printf("Exit Code %s%n", getAndInvoke(new ConstNull(), "main", argv));
        }
    }

}