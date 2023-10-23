package io.scriptor;

import static io.scriptor.csaw.impl.Environment.getAndInvoke;

import java.io.File;
import java.io.FileInputStream;

import io.scriptor.csaw.impl.Environment;
import io.scriptor.csaw.impl.Parser;
import io.scriptor.java.Collector;

public class Main {

    public static void main(String[] args) throws Exception {

        final var file = new File("csaw/rtx/main.csaw");
        final var env = Environment.initGlobal(file.getParent());
        Collector.collect(env);

        final var parser = new Parser(new FileInputStream(file), env);
        if (!parser.start())
            return;

        System.out.printf("Exit Code %s%n", getAndInvoke(null, "main"));
    }

}