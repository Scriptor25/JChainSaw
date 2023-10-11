package io.scriptor;

import io.scriptor.value.Value;

public class FunParam<T extends Value> {

    public String name;
    public final Class<T> type;

    public FunParam(Class<T> type) {
        this.type = type;
    }

    public boolean isOfType(Class<?> type) {
        return this.type.equals(type);
    }
}
