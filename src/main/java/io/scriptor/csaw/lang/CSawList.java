package io.scriptor.csaw.lang;

import io.scriptor.csaw.impl.value.NumValue;
import io.scriptor.csaw.impl.value.Value;
import io.scriptor.java.CSawNative;

import java.util.List;
import java.util.Vector;

@CSawNative("list")
public class CSawList {

    private final List<Value> mValues = new Vector<>();

    public Value get(NumValue index) {
        return mValues.get((int) (double) index.getValue());
    }

    public void add(Value value) {
        mValues.add(value);
    }

    public NumValue size() {
        return new NumValue(mValues.size());
    }
}
