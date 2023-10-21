package io.scriptor;

import java.io.File;
import java.io.FileInputStream;

public class Main {

    public static void main(String[] args) {
        try {
            final var file = new File("csaw/rtx/main.csaw");
            final var env = new Environment(file.getParent());
            final var parser = new Parser(new FileInputStream(file), env);
            if (!parser.start())
                return;

            System.out.printf("Exit Code %s%n", env.getAndInvoke(null, "main"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}