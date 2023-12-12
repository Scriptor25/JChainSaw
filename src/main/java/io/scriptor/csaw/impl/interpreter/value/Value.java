package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.Types.TYPE_CHR;
import static io.scriptor.csaw.impl.Types.TYPE_NUM;
import static io.scriptor.csaw.impl.Types.TYPE_STR;
import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.getOrigin;
import static io.scriptor.csaw.impl.interpreter.Environment.hasFunction;
import static io.scriptor.csaw.impl.interpreter.Environment.hasType;

import io.scriptor.csaw.impl.interpreter.Environment;

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

    public boolean isStr() {
        return this instanceof StrValue;
    }

    public boolean isChr() {
        return this instanceof ChrValue;
    }

    public boolean isObj() {
        return this instanceof ObjValue;
    }

    public boolean isNative() {
        return this instanceof NativeValue;
    }

    public boolean isNull() {
        return this instanceof NullValue;
    }

    public boolean sameType(Value value) {
        return getClass().isInstance(value);
    }

    public NumValue asNum() {
        return (NumValue) this;
    }

    public StrValue asStr() {
        return (StrValue) this;
    }

    public ChrValue asChr() {
        return (ChrValue) this;
    }

    public ObjValue asObj() {
        return (ObjValue) this;
    }

    public NativeValue asNative() {
        return (NativeValue) this;
    }

    public abstract Object getValue();

    public abstract String getType();

    public abstract boolean asBoolean();

    public abstract String toString();

    public static Value makeValue(Environment env, String type, boolean primitives, boolean dontConstruct) {
        switch (getOrigin(type)) {
            case TYPE_NUM:
                return new NumValue();
            case TYPE_STR:
                return new StrValue();
            case TYPE_CHR:
                return new ChrValue();
            default:
                break;
        }

        if (primitives || !hasType(getOrigin(type)))
            return new NullValue(type);

        if (!dontConstruct && hasFunction(null, type))
            return getAndInvoke(null, type);

        return new ObjValue(env, type);
    }

    public static Value binAnd(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().getInt() & right.asNum().getInt());

        return getAndInvoke(null, "&", left, right);
    }

    public static Value and(Environment env, Value left, Value right) {
        return new NumValue(left.asBoolean() && right.asBoolean());
    }

    public static Value binOr(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().getInt() | right.asNum().getInt());

        return getAndInvoke(null, "|", left, right);
    }

    public static Value or(Environment env, Value left, Value right) {
        return new NumValue(left.asBoolean() || right.asBoolean());
    }

    public static Value cmpe(Environment env, Value left, Value right) {
        return new NumValue(left.equals(right));
    }

    public static Value cmpne(Environment env, Value left, Value right) {
        return new NumValue(!left.equals(right));
    }

    public static Value cmpl(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() < right.asNum().get());

        return getAndInvoke(null, "<", left, right);
    }

    public static Value cmple(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() <= right.asNum().get());

        return getAndInvoke(null, "<=", left, right);
    }

    public static Value cmpg(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() > right.asNum().get());

        return getAndInvoke(null, ">", left, right);
    }

    public static Value cmpge(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() >= right.asNum().get());

        return getAndInvoke(null, ">=", left, right);
    }

    public static Value add(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() + right.asNum().get());

        if (left.isStr())
            return new StrValue(((StrValue) left).getValue() + right.toString());

        if (right.isStr())
            return new StrValue(left.toString() + ((StrValue) right).getValue());

        return getAndInvoke(null, "+", left, right);
    }

    public static Value sub(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() - right.asNum().get());

        return getAndInvoke(null, "-", left, right);
    }

    public static Value mul(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() * right.asNum().get());

        return getAndInvoke(null, "*", left, right);
    }

    public static Value div(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() / right.asNum().get());

        return getAndInvoke(null, "/", left, right);
    }

    public static Value mod(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() % right.asNum().get());

        return getAndInvoke(null, "%", left, right);
    }

    public static Value xor(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(
                    (int) (double) left.asNum().getValue() ^ (int) (double) right.asNum().getValue());

        return getAndInvoke(null, "^", left, right);
    }

    public static Value index(Environment env, Value left, Value right) {
        return getAndInvoke(null, "[]", left, right);
    }

    public static Value neg(Environment env, Value value) {
        if (value.isNum())
            return new NumValue(-value.asNum().get());

        return getAndInvoke(value, "-");
    }

    public static Value not(Environment env, Value value) {
        return new NumValue(!value.asBoolean());
    }

    public static Value inv(Environment env, Value value) {
        if (value.isNum())
            return new NumValue(~value.asNum().getInt());

        return getAndInvoke(value, "~");
    }
}
