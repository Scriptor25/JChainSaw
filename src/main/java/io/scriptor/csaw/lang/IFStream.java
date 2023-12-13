package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handle;
import static io.scriptor.java.ErrorUtil.handleVoid;

import java.io.BufferedReader;
import java.io.FileReader;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("ifstream")
public class IFStream extends Value {

    private final String mFilename;
    private final BufferedReader mReader;

    public IFStream(StrValue filename) {
        mFilename = filename.get();
        mReader = new BufferedReader(handle(() -> new FileReader(mFilename)));
    }

    public NumValue open() {
        return new NumValue(mReader != null);
    }

    public StrValue readLine() {
        return new StrValue(handle(mReader::readLine));
    }

    public void close() {
        handleVoid(mReader::close);
    }

    @Override
    protected String type() {
        return "ifstream";
    }

    @Override
    protected Object object() {
        return mReader;
    }

    @Override
    protected String string() {
        return String.format("{ ifstream %s }", mFilename);
    }
}
