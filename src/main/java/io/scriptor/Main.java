package io.scriptor;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new Parser(new FileInputStream("test.csaw"));
    }

}