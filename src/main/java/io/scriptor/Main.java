package io.scriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final var file = new File("csaw/rtx/rtx.csaw");

        final var env = new Environment(file.getParent());
        new Parser(new FileInputStream(file), env);

        System.out.printf("Exit Code %s%n", env.getAndInvoke(null, "main"));
    }

}