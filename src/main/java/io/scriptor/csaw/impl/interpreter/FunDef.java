package io.scriptor.csaw.impl.interpreter;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;
import static io.scriptor.csaw.impl.interpreter.Environment.isAssignable;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstNull;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.csaw.impl.stmt.EnclosedStmt;

public class FunDef {

    public static class FunBody implements IFunBody {

        public FunDef definition;
        public String[] parameters;
        public EnclosedStmt implementation;

        public FunBody(FunDef def, EnclosedStmt impl) {
            definition = def;
            implementation = impl;
        }

        public FunBody(String[] params, EnclosedStmt impl) {
            parameters = params;
            implementation = impl;
        }

        @Override
        public Value invoke(Value member, Value... args) {
            final var env = new Environment(getGlobal());
            for (int i = 0; i < parameters.length; i++)
                env.createVariable(parameters[i], definition.parameters[i], args[i]);

            if (definition.vararg != null) {
                final var at = Type.get(Type.getAny(), args.length);
                final var va = Value.makeValue(getGlobal(), at, false, false).asRef();
                for (int i = parameters.length; i < args.length; i++)
                    va.set(i, args[i]);
                env.createVariable(definition.vararg, at, va);
            }

            if (definition.constructor)
                env.createVariable("my", definition.type, Value.makeValue(env, definition.type, false, true));
            if (!definition.member.isNull())
                env.createVariable("my", definition.member, member);

            final var value = Interpreter.evaluate(env, implementation).isReturn(false);

            if (definition.constructor) {
                if (!value.isNull())
                    throw new CSawException("a constructor must not return anything");
                return env.getVariable("my");
            }

            if (!value.isNull() && !isAssignable(value.getType(), definition.type))
                throw new CSawException(
                        "invalid return value: value type is '%s', but function has to provide type '%s'",
                        value.getType(), definition.type);

            return value;
        }
    }

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
            return new ConstNull();
        return value;
    }
}
