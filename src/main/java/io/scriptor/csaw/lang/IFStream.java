package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.tryCatch;
import static io.scriptor.java.ErrorUtil.tryCatchVoid;

import java.io.BufferedReader;
import java.io.FileReader;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.java.CSawNative;

@CSawNative("ifstream")
public class IFStream {

    private BufferedReader mReader;

    public IFStream(StrValue filename) {
        mReader = new BufferedReader(tryCatch(() -> new FileReader(filename.getValue())));
    }

    public NumValue open() {
        return new NumValue(mReader != null);
    }

    public StrValue readLine() {
        return new StrValue(tryCatch(mReader::readLine));
    }

    public void close() {
        tryCatchVoid(mReader::close);
    }
}
