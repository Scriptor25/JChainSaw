package io.scriptor.csaw.impl.interpreter;

import io.scriptor.csaw.impl.interpreter.value.ConstNull;
import io.scriptor.csaw.impl.interpreter.value.Value;

public class FunDef {

    public static class Builder {

        public boolean constructor;
        public Type type;
        public Type[] parameters;
        public String vararg;
        public Type member;
        public IFunBody body;

        public Builder constructor(boolean constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder parameters(Type[] parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder vararg(String vararg) {
            this.vararg = vararg;
            return this;
        }

        public Builder member(Type member) {
            this.member = member;
            return this;
        }

        public Builder body(IFunBody body) {
            this.body = body;
            return this;
        }

        public FunDef build() {
            return new FunDef(constructor, type, parameters, vararg, member, body);
        }
    }

    public final boolean constructor;
    public final Type type;
    public final Type[] parameters;
    public final String vararg;
    public final Type member;
    public final IFunBody body;

    public FunDef(boolean constructor, Type type, Type[] parameters, String vararg, Type member, IFunBody body) {
        this.constructor = constructor;
        this.type = type;
        this.parameters = parameters;
        this.vararg = vararg;
        this.member = member;
        this.body = body;

        if (body instanceof FunBody)
            ((FunBody) body).definition = this;
    }

    public Value invoke(Value member, Value... args) {
        final var value = body.invoke(member, args);
        if (value == null)
            return new ConstNull("void return");
        return value;
    }
}
