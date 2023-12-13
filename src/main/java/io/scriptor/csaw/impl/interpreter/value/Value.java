package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.interpreter.Environment.getAndInvoke;
import static io.scriptor.csaw.impl.interpreter.Environment.getOrigin;
import static io.scriptor.csaw.impl.interpreter.Environment.hasFunction;
import static io.scriptor.csaw.impl.interpreter.Environment.hasType;

import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.Type.ArrayType;
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
        return this instanceof ConstNum;
    }

    public boolean isStr() {
        return this instanceof ConstStr;
    }

    public boolean isChr() {
        return this instanceof ConstChr;
    }

    public boolean isThing() {
        return this instanceof ConstThing;
    }

    public boolean isLambda() {
        return this instanceof ConstLambda;
    }

    public boolean isRef() {
        return this instanceof ValueRef;
    }

    public ConstNum asNum() {
        return (ConstNum) this;
    }

    public ConstStr asStr() {
        return (ConstStr) this;
    }

    public ConstChr asChr() {
        return (ConstChr) this;
    }

    public ConstThing asThing() {
        return (ConstThing) this;
    }

    public ConstLambda asLambda() {
        return (ConstLambda) this;
    }

    public ValueRef asRef() {
        return (ValueRef) this;
    }

    protected abstract Type type();

    protected abstract Object object();

    protected abstract String string();

    public Type getType() {
        return type();
    }

    public Object getObject() {
        return object();
    }

    @Override
    public String toString() {
        return string();
    }

    public static Value makeValue(Environment env, Type type, boolean onlyPrimitives, boolean dontConstruct) {
        final var origin = getOrigin(type);

        if (origin instanceof ArrayType at)
            return new ValueRef(at.size, at.type);

        if (origin.equals(Type.getNum()))
            return new ConstNum();
        if (origin.equals(Type.getStr()))
            return new ConstStr();
        if (origin.equals(Type.getChr()))
            return new ConstChr();

        if (onlyPrimitives || !hasType(origin.name))
            return null;

        if (!dontConstruct && hasFunction(null, type.name))
            return getAndInvoke(null, type.name);

        return new ConstThing(env, type.name);
    }

    public static Value binAnd(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().getInt() & right.asNum().getInt());

        return getAndInvoke(null, "&", left, right);
    }

    public static Value and(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() != 0.0 && right.asNum().get() != 0.0);

        return getAndInvoke(null, "&&", left, right);
    }

    public static Value binOr(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().getInt() | right.asNum().getInt());

        return getAndInvoke(null, "|", left, right);
    }

    public static Value or(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() != 0.0 || right.asNum().get() != 0.0);

        return getAndInvoke(null, "||", left, right);
    }

    public static Value cmpe(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() == right.asNum().get());

        if (left.isStr() && right.isStr())
            return new ConstNum(left.asStr().get().equals(right.asStr().get()));

        return getAndInvoke(null, "==", left, right);
    }

    public static Value cmpne(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() != right.asNum().get());

        if (left.isStr() && right.isStr())
            return new ConstNum(!left.asStr().get().equals(right.asStr().get()));

        return getAndInvoke(null, "!=", left, right);
    }

    public static Value cmpl(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() < right.asNum().get());

        return getAndInvoke(null, "<", left, right);
    }

    public static Value cmple(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() <= right.asNum().get());

        return getAndInvoke(null, "<=", left, right);
    }

    public static Value cmpg(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() > right.asNum().get());

        return getAndInvoke(null, ">", left, right);
    }

    public static Value cmpge(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() >= right.asNum().get());

        return getAndInvoke(null, ">=", left, right);
    }

    public static Value add(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() + right.asNum().get());

        if (left.isStr() || right.isStr())
            return new ConstStr(left.toString() + right.toString());

        return getAndInvoke(null, "+", left, right);
    }

    public static Value sub(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() - right.asNum().get());

        return getAndInvoke(null, "-", left, right);
    }

    public static Value mul(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() * right.asNum().get());

        return getAndInvoke(null, "*", left, right);
    }

    public static Value div(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() / right.asNum().get());

        return getAndInvoke(null, "/", left, right);
    }

    public static Value mod(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().get() % right.asNum().get());

        return getAndInvoke(null, "%", left, right);
    }

    public static Value xor(Environment env, Value left, Value right) {
        if (left.isNum() && right.isNum())
            return new ConstNum(left.asNum().getInt() ^ right.asNum().getInt());

        return getAndInvoke(null, "^", left, right);
    }

    public static Value neg(Environment env, Value value) {
        if (value.isNum())
            return new ConstNum(-value.asNum().get());

        return getAndInvoke(value, "-");
    }

    public static Value not(Environment env, Value value) {
        if (value.isNum())
            return new ConstNum(value.asNum().get() == 0.0);

        return getAndInvoke(value, "!");
    }

    public static Value inv(Environment env, Value value) {
        if (value.isNum())
            return new ConstNum(~value.asNum().getInt());

        return getAndInvoke(value, "~");
    }
}
