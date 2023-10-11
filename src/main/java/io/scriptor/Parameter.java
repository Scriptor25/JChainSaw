package io.scriptor;

public class Parameter {

    public String name;
    public String type;

    @Override
    public String toString() {
        return String.format("%s: %s", name, type);
    }
}
