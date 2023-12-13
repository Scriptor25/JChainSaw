package io.scriptor.csaw.impl;

public class Parameter {

    public String name;
    public Type type;

    @Override
    public String toString() {
        return String.format("%s: %s", name, type);
    }
}
