package io.scriptor.csaw.impl;

import java.util.HashMap;
import java.util.Map;

public class Type {

    public static class ArrayType extends Type {

        public final Type type;
        public final int size;

        protected ArrayType(Type type, int size) {
            super(type.name);
            this.type = type;
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("%s[%d]", type, size);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (o == this)
                return true;
            if (!(o instanceof ArrayType t))
                return false;
            return type.equals(t.type) && size == t.size;
        }
    }

    public final String name;

    protected Type(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof Type t))
            return false;
        return name.equals(t.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private static final Map<String, Type> TYPES = new HashMap<>();
    private static final Map<Type, ArrayType> ARRAY_TYPES = new HashMap<>();

    public static Type getAny() {
        return get("any");
    }

    public static Type getNum() {
        return get("num");
    }

    public static Type getStr() {
        return get("str");
    }

    public static Type getChr() {
        return get("chr");
    }

    public static Type getLambda() {
        return get("lambda");
    }

    public static Type get(String name) {
        return TYPES.computeIfAbsent(name, key -> new Type(key));
    }

    public static ArrayType get(Type type, int size) {
        return ARRAY_TYPES.computeIfAbsent(type, key -> new ArrayType(key, size));
    }

}
