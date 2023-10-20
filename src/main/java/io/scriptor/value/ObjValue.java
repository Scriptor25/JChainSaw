package io.scriptor.value;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.Environment;
import io.scriptor.Pair;

public class ObjValue extends Value {

    private final String mType;
    private final Map<String, Pair<String, Value>> mFields = new HashMap<>();

    public ObjValue(Environment env, String type) {
        mType = type;
        for (final var field : env.getType(type))
            mFields.put(field.name, new Pair<>(field.type, Value.makeValue(env, field.type, true)));
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    public Value getField(String field) {
        return mFields.get(field).second;
    }

    public <V extends Value> V setField(String field, V value) {
        mFields.get(field).second = value;
        return value;
    }

}
