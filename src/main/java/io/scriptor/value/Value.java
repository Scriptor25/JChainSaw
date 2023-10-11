package io.scriptor.value;

public abstract class Value {

    private boolean mReturn = false;

    public boolean isReturn() {
        return mReturn;
    }

    public Value isReturn(boolean ret) {
        mReturn = ret;
        return this;
    }

    public boolean isNum() {
        return this instanceof NumValue;
    }

    public boolean sameType(Value value) {
        return getClass().isInstance(value);
    }

    public abstract boolean asBoolean();

    public static Value binAnd(Value left, Value right) {
        return null;
    }

    public static Value and(Value left, Value right) {
        return null;
    }

    public static Value binOr(Value left, Value right) {
        return null;
    }

    public static Value or(Value left, Value right) {
        return null;
    }

    public static Value cmpe(Value left, Value right) {
        return new NumValue(left.equals(right));
    }

    public static Value cmpl(Value left, Value right) {
        return null;
    }

    public static Value cmple(Value left, Value right) {
        return null;
    }

    public static Value cmpg(Value left, Value right) {
        return null;
    }

    public static Value cmpge(Value left, Value right) {
        return null;
    }

    public static Value add(Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(((NumValue) left).getValue() + ((NumValue) right).getValue());

        return null;
    }

    public static Value sub(Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(((NumValue) left).getValue() - ((NumValue) right).getValue());

        return null;
    }

    public static Value mul(Value left, Value right) {
        return null;
    }

    public static Value div(Value left, Value right) {
        return null;
    }

    public static Value mod(Value left, Value right) {
        return null;
    }
}
