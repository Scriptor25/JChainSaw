package io.scriptor.csaw.lang;

import static io.scriptor.csaw.impl.Types.TYPE_ANY;
import static io.scriptor.csaw.impl.interpreter.Environment.getFunction;

import java.util.List;
import java.util.Vector;

import io.scriptor.csaw.impl.interpreter.value.NativeValue;
import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("list")
public class CSawList {

    private final List<Value> mValues = new Vector<>();

    public CSawList() {
    }

    private CSawList(List<Value> values) {
        mValues.addAll(values);
    }

    public Value get(NumValue index) {
        return mValues.get((int) (double) index.getValue());
    }

    public void add(Value value) {
        mValues.add(value);
    }

    public void set(NumValue index, Value value) {
        mValues.set((int) (double) index.getValue(), value);
    }

    public NumValue size() {
        return new NumValue(mValues.size());
    }

    public NativeValue sub(NumValue from, NumValue to) {
        return new NativeValue(
                new CSawList(mValues.subList((int) (double) from.getValue(), (int) (double) to.getValue())));
    }

    public NativeValue sort(StrValue comparator) {
        final var list = new CSawList(mValues);
        final var cmp = getFunction(null, comparator.getValue(), new String[] { TYPE_ANY, TYPE_ANY });
        list.mValues.sort((v1, v2) -> {
            final var val = ((NumValue) cmp.invoke(null, v1, v2)).getValue();
            return (int) (double) val;
        });

        return new NativeValue(list);
    }
}
