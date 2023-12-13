package io.scriptor.csaw.lang;

import static io.scriptor.csaw.impl.interpreter.Environment.getFunction;

import java.util.List;
import java.util.Vector;

import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
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
    public Value get(ConstNum index) {
        return mValues.get(index.getInt());
    }

    public void add(Value value) {
        mValues.add(value);
    }

    public void set(ConstNum index, Value value) {
        mValues.set(index.getInt(), value);
    }

    public ConstNum size() {
        return new ConstNum(mValues.size());
    }

    public CSawList sub(ConstNum from, ConstNum to) {
        return new CSawList(mValues.subList(from.getInt(), to.getInt()));
    }

    public CSawList sort(ConstStr comparator) {
        final var list = new CSawList(mValues);
        final var cmp = getFunction(null, comparator.get(), Type.getAny(), Type.getAny());
        list.mValues.sort((v1, v2) -> cmp.invoke(null, v1, v2).asNum().getInt());
        return list;
    }

    @Override
    protected Type type() {
        return Type.get("list");
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
