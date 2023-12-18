package io.scriptor.csaw.impl.interpreter;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;
import static io.scriptor.csaw.impl.interpreter.Environment.isAssignable;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.frontend.stmt.EnclosedStmt;
import io.scriptor.csaw.impl.interpreter.value.Value;

public class FunBody implements IFunBody {

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

    // TODO: make this f**ing synchronized...
    @Override
    public Value invoke(Value member, Value... args) {
        final var env = pre(member, args);
        Value value;
        synchronized (this) { // slow as f
            value = Interpreter.evaluate(env, implementation).setReturn(false);
        }
        return post(env, value);
    }

    private Environment pre(Value member, Value... args) {
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

        return env;
    }

    private Value post(Environment env, Value value) {
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
