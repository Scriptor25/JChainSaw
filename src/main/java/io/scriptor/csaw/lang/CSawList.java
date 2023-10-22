package io.scriptor.csaw.lang;

import io.scriptor.csaw.impl.Environment;
import io.scriptor.csaw.impl.value.NativeValue;
import io.scriptor.csaw.impl.value.NumValue;
import io.scriptor.csaw.impl.value.StrValue;
import io.scriptor.csaw.impl.value.Value;
import io.scriptor.java.CSawNative;

import java.util.List;
import java.util.Vector;

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

    public NumValue size() {
        return new NumValue(mValues.size());
    }

    public NativeValue sub(NumValue from, NumValue to) {
        return new NativeValue(
                new CSawList(mValues.subList((int) (double) from.getValue(), (int) (double) to.getValue())));
    }

    public NativeValue sort(StrValue comparator) {
        final var list = new CSawList(mValues);
        final var env = Environment.getGlobal();
        final var cmp = env.getFunction(null, comparator.getValue(), new String[2]);
        list.mValues.sort((v1, v2) -> {
            try {
                final var val = ((NumValue) cmp.body.invoke(null, env, v1, v2)).getValue();
                return (int) (double) val;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        });

        return new NativeValue(list);
    }
}
