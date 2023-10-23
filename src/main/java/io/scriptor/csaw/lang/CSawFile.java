package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.tryCatch;
import static io.scriptor.java.ErrorUtil.tryCatchVoid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import io.scriptor.csaw.impl.value.StrValue;
import io.scriptor.csaw.impl.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("file")
public class CSawFile {

    private final BufferedReader mReader;
    private final BufferedWriter mWriter;

    public CSawFile(StrValue path) {
        mReader = new BufferedReader(tryCatch(() -> new FileReader(path.getValue())));
        mWriter = new BufferedWriter(tryCatch(() -> new FileWriter(path.getValue(), false)));
    }

    public void out(StrValue fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getValue();
        tryCatchVoid(() -> mWriter.append(String.format(fmt.getValue(), objArgs)));
    }

    public StrValue in() {
        return new StrValue(tryCatch(mReader::readLine));
    }

    public void close() {
        tryCatchVoid(mReader::close);
        tryCatchVoid(mWriter::close);
    }
}
