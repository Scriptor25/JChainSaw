package io.scriptor.csaw.impl;

import io.scriptor.csaw.impl.interpreter.Type;

public class Parameter {

    public final String name;
    public final Type type;

    public Parameter(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, type);
    }
}
