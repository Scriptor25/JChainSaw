package io.scriptor.csaw.lang;

import static io.scriptor.csaw.impl.Types.TYPE_ANY;
import static io.scriptor.csaw.impl.interpreter.Environment.getFunction;

import java.util.List;
import java.util.Vector;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawAlias;
import io.scriptor.java.CSawNative;

@CSawNative("list")
public class CSawList extends Value {

    private final List<Value> mValues = new Vector<>();

    public CSawList() {
    }

    private CSawList(List<Value> values) {
        mValues.addAll(values);
    }

    @CSawAlias("[]")
    public Value get(NumValue index) {
        return mValues.get(index.getInt());
    }

    public void add(Value value) {
        mValues.add(value);
    }

    public void set(NumValue index, Value value) {
        mValues.set(index.getInt(), value);
    }

    public NumValue size() {
        return new NumValue(mValues.size());
    }

    public CSawList sub(NumValue from, NumValue to) {
        return new CSawList(mValues.subList(from.getInt(), to.getInt()));
    }

    public CSawList sort(StrValue comparator) {
        final var list = new CSawList(mValues);
        final var cmp = getFunction(null, comparator.get(), new String[] { TYPE_ANY, TYPE_ANY });
        list.mValues.sort((v1, v2) -> cmp.invoke(null, v1, v2).asNum().getInt());
        return list;
    }

    @Override
    protected String type() {
        return "list";
    }

    @Override
    protected Object object() {
        return mValues;
    }

    @Override
    protected String string() {
        return mValues.toString();
    }
}
