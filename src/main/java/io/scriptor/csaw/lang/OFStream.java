package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.tryCatch;
import static io.scriptor.java.ErrorUtil.tryCatchVoid;

import java.io.BufferedWriter;
import java.io.FileWriter;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("ofstream")
public class OFStream {

    private BufferedWriter mWriter;

    public OFStream(StrValue filename) {
        mWriter = new BufferedWriter(tryCatch(() -> new FileWriter(filename.getValue())));
    }

    public NumValue open() {
        return new NumValue(mWriter != null);
    }

    public void write(StrValue fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getValue();
        tryCatchVoid(() -> mWriter.write(String.format(fmt.getValue(), objArgs)));
    }

    public void close() {
        tryCatchVoid(mWriter::close);
    }
}
