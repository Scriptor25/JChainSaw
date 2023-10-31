package io.scriptor;

import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import io.scriptor.csaw.impl.Parser;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.java.Collector;
import io.scriptor.java.ErrorUtil;

public class CSaw {

    public static void main(String[] args) {

        switch (args.length) {
            case 0:
                shell();
                break;

            case 1:
                run(args[0]);
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
                System.out.println(t);
            }
        }
    }

    public static void run(String path) {
        final var file = new File(path);

        // final var ctx = new CSawContext(file);
        // new Parser(new FileInputStream(file), ctx);

        final var env = Environment.initGlobal(file.getParent());
        Collector.collect(env);

        Parser.parse(ErrorUtil.tryCatch(() -> new FileInputStream(file)), env);

        System.out.printf("Exit Code %s%n", getAndInvoke(null, "main"));
    }

}