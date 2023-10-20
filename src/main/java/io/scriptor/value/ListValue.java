package io.scriptor.value;

import java.util.List;
import java.util.Vector;

public class ListValue extends Value {

    private final List<Value> mValues = new Vector<>();

    public Value get(int index) {
        return mValues.get(index);
    }

    public void add(Value v) {
        mValues.add(v);
    }

    public int size() {
        return mValues.size();
    }

    @Override
    public List<Value> getValue() {
        return mValues;
    }

    @Override
    public String getType() {
        return Value.TYPE_LIST;
    }

    @Override
    public boolean asBoolean() {
        return !mValues.isEmpty();
    }

}
