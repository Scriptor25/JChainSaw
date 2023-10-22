package io.scriptor.csaw.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.scriptor.csaw.impl.value.StrValue;
import io.scriptor.csaw.impl.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("file")
public class CSawFile {

    private final BufferedReader mReader;
    private final BufferedWriter mWriter;

    public CSawFile(StrValue path) throws IOException {
        mReader = new BufferedReader(new FileReader(path.getValue()));
        mWriter = new BufferedWriter(new FileWriter(path.getValue(), false));
    }

    public void out(StrValue fmt, Value... args) throws IOException {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getValue();
        mWriter.append(String.format(fmt.getValue(), objArgs));
    }

    public StrValue in() throws IOException {
        return new StrValue(mReader.readLine());
    }

    public void close() throws IOException {
        mReader.close();
        mWriter.close();
    }
}
