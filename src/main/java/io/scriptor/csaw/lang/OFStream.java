package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handle;
import static io.scriptor.java.ErrorUtil.handleVoid;

import java.io.BufferedWriter;
import java.io.FileWriter;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("ofstream")
public class OFStream extends Value {

    private final String mFilename;
    private final BufferedWriter mWriter;

    public OFStream(StrValue filename) {
        mFilename = filename.get();
        mWriter = new BufferedWriter(handle(() -> new FileWriter(mFilename)));
    }

    public NumValue open() {
        return new NumValue(mWriter != null);
    }

    public void write(StrValue fmt, Value... args) {
        final var objArgs = new Object[args == null ? 0 : args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getObject();
        handleVoid(() -> mWriter.write(String.format(fmt.get(), objArgs)));
    }

    public void close() {
        handleVoid(mWriter::close);
    }

    @Override
    protected String type() {
        return "ofstream";
    }

    @Override
    protected Object object() {
        return mWriter;
    }

    @Override
    protected String string() {
        return String.format("{ ofstream %s }", mFilename);
    }
}
