package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;

import java.util.Arrays;

import io.scriptor.csaw.impl.Pair;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.Types;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.Interpreter;
import io.scriptor.csaw.impl.stmt.Stmt;
import io.scriptor.csaw.lang.CSawList;

public class ConstLambda extends Value {

    private final Pair<String, Value>[] mPassed;
    private final Parameter[] mParameters;
    private final Stmt mBody;

    public ConstLambda(Pair<String, Value>[] passed, Parameter[] parameters, Stmt body) {
        mPassed = passed;
        mParameters = parameters;
        mBody = body;
    }

    public Value invoke(Value... args) {
        if (args.length == 1 && args[0] instanceof CSawList) {
            final var list = (CSawList) args[0];
            final var size = list.size().getInt();
            args = new Value[size];
            for (int i = 0; i < size; i++)
                args[i] = list.get(new ConstNum(i));
        }

        final var env = new Environment(getGlobal());
        for (final var p : mPassed)
            env.createVariable(p.first, p.second.getType(), p.second);

        for (int i = 0; i < mParameters.length; i++)
            env.createVariable(mParameters[i].name, mParameters[i].type, args[i]);

        final var value = Interpreter.evaluate(env, mBody);
        if (value != null)
            value.isReturn(false);

        return value;
    }

    @Override
    protected String type() {
        return Types.TYPE_LAMBDA;
    }

    @Override
    protected Object object() {
        return null;
    }

    @Override
    protected String string() {
        return String.format("{ %s %s %s }", Types.TYPE_LAMBDA, Arrays.toString(mPassed), Arrays.toString(mParameters));
    }

}
