package io.scriptor.csaw.impl;

import io.scriptor.csaw.impl.stmt.Stmt;
import io.scriptor.csaw.impl.value.Value;

public class FunDef {

    public static class FunBody implements IFunBody {

        public FunDef definition;
        public String[] parameters;
        public Stmt[] implementation;

        public FunBody(FunDef def, Stmt[] impl) {
            definition = def;
            implementation = impl;
        }

        public FunBody(String[] params, Stmt[] impl) {
            parameters = params;
            implementation = impl;
        }

        @Override
        public Value invoke(Value member, Environment env, Value... args) throws Exception {
            final var e = new Environment(env);
            for (int i = 0; i < args.length; i++)
                e.createVariable(parameters[i], definition.parameters[i], args[i]);

            if (definition.constructor)
                e.createVariable("my", definition.type, Value.makeValue(e, definition.type, false));
            if (definition.member != null)
                e.createVariable("my", definition.member, member);

            Value value = null;
            for (final var stmt : implementation) {
                final var v = Interpreter.evaluate(e, stmt);
                if (v != null && v.isReturn()) {
                    value = v.isReturn(false);
                    break;
                }
            }

            if (definition.constructor) {
                if (value != null)
                    throw new IllegalStateException();
                return e.getVariable("my");
            }

            if (value == null && definition.type != null)
                throw new IllegalStateException(String.format(
                        "invalid return value: value is null, but function has to provide type '%s'", definition.type));

            if (value != null && !e.isAssignable(value.getType(), definition.type))
                throw new IllegalStateException(String.format(
                        "invalid return value: value type is '%s', but function has to provide type '%s'",
                        value.getType(), definition.type));

            return value;
        }
    }

    public static class Builder {

        public boolean constructor;
        public String type;
        public String[] parameters;
        public boolean vararg;
        public String member;
        public IFunBody body;

        public Builder constructor(boolean constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder parameters(String[] parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder vararg(boolean vararg) {
            this.vararg = vararg;
            return this;
        }

        public Builder member(String member) {
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
    public final String type;
    public final String[] parameters;
    public final boolean vararg;
    public final String member;
    public final IFunBody body;

    public FunDef(boolean constructor, String type, String[] parameters, boolean vararg, String member, IFunBody body) {
        this.constructor = constructor;
        this.type = type;
        this.parameters = parameters;
        this.vararg = vararg;
        this.member = member;
        this.body = body;

        if (body instanceof FunBody)
            ((FunBody) body).definition = this;
    }
}
