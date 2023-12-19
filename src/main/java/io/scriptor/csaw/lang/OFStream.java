package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handle;
import static io.scriptor.java.ErrorUtil.handleVoid;

import java.io.BufferedWriter;
import java.io.FileWriter;

import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("ofstream")
public class OFStream extends Value {

    private final String mFilename;
    private final BufferedWriter mWriter;

    public OFStream(ConstStr filename) {
        mFilename = filename.get();
        mWriter = new BufferedWriter(handle(() -> new FileWriter(mFilename)));
    }

    @Override
    public ConstNum asNum() {
        return new ConstNum(mWriter != null);
    }

    public void write(ConstStr fmt, Value... args) {
        final var objArgs = new Object[args.length];
        for (int i = 0; i < objArgs.length; i++)
            objArgs[i] = args[i].getObject();
        handleVoid(() -> mWriter.write(String.format(fmt.get(), objArgs)));
    }

    public void close() {
        handleVoid(mWriter::close);
    }

    @Override
    protected Type type() {
        return Type.get("ofstream");
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
