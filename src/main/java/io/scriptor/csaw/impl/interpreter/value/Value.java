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

    public boolean isThing() {
        return this instanceof ThingValue;
    }

    public boolean isLambda() {
        return this instanceof LambdaValue;
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

    public ThingValue asThing() {
        return (ThingValue) this;
    }

    public LambdaValue asLambda() {
        return (LambdaValue) this;
    }

    protected abstract String type();

    protected abstract Object object();

    protected abstract String string();

    public String getType() {
        return type();
    }

    public Object getObject() {
        return object();
    }

    @Override
    public String toString() {
        return string();
    }

    public static Value makeValue(Environment env, String type, boolean onlyPrimitives, boolean dontConstruct) {
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

        if (onlyPrimitives || !hasType(getOrigin(type)))
            return null;

        if (!dontConstruct && hasFunction(null, type))
            return getAndInvoke(null, type);

        return new ThingValue(env, type);
    }

    public static Value binAnd(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().getInt() & right.asNum().getInt());

        return getAndInvoke(null, "&", left, right);
    }

    public static Value and(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() != 0.0 && right.asNum().get() != 0.0);

        return getAndInvoke(null, "&&", left, right);
    }

    public static Value binOr(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().getInt() | right.asNum().getInt());

        return getAndInvoke(null, "|", left, right);
    }

    public static Value or(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() != 0.0 || right.asNum().get() != 0.0);

        return getAndInvoke(null, "||", left, right);
    }

    public static Value cmpe(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() == right.asNum().get());

        if (left.isStr() && right.isStr())
            return new NumValue(left.asStr().get().equals(right.asStr().get()));

        return getAndInvoke(null, "==", left, right);
    }

    public static Value cmpne(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new NumValue(left.asNum().get() != right.asNum().get());

        if (left.isStr() && right.isStr())
            return new NumValue(!left.asStr().get().equals(right.asStr().get()));

        return getAndInvoke(null, "!=", left, right);
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

        if (left.isStr() || right.isStr())
            return new StrValue(left.toString() + right.toString());

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
            return new NumValue(left.asNum().getInt() ^ right.asNum().getInt());

        return getAndInvoke(null, "^", left, right);
    }

    public static Value index(Environment env, Value left, Value right) {
        return getAndInvoke(left, "[]", right);
    }

    public static Value neg(Environment env, Value value) {
        if (value.isNum())
            return new NumValue(-value.asNum().get());

        return getAndInvoke(value, "-");
    }

    public static Value not(Environment env, Value value) {
        if (value.isNum())
            return new NumValue(value.asNum().get() == 0.0);

        return getAndInvoke(value, "!");
    }

    public static Value inv(Environment env, Value value) {
        if (value.isNum())
            return new NumValue(~value.asNum().getInt());

        return getAndInvoke(value, "~");
    }
}
