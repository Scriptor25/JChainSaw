package io.scriptor.csaw.impl.interpreter.value;

import static io.scriptor.csaw.impl.interpreter.Environment.getGlobal;

import java.util.Arrays;

import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.frontend.stmt.Stmt;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.FunDef;
import io.scriptor.csaw.impl.interpreter.Interpreter;
import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.lang.CSawList;

public class ConstLambda extends Value {

    private final NamedValue[] mPassed;
    private final Parameter[] mParameters;
    private final Stmt mBody;
    private final FunDef mFunc;

    public ConstLambda(NamedValue[] passed, Parameter[] parameters, Stmt body) {
        mPassed = passed;
        mParameters = parameters;
        mBody = body;
        mFunc = null;
    }

    public ConstLambda(FunDef def) {
        mPassed = null;
        mParameters = null;
        mBody = null;
        mFunc = def;
    }

    public Value invoke(Value... args) {
        if (args.length == 1 && args[0] instanceof CSawList) {
            final var list = (CSawList) args[0];
            final var size = list.size().getInt();
            args = new Value[size];
            for (int i = 0; i < size; i++)
                args[i] = list.get(new ConstNum(i));
        }

        if (mFunc != null)
            return mFunc.invoke(new ConstNull("global function lambda"), args);

        final var env = new Environment(getGlobal());
        for (final var p : mPassed)
            env.createVariable(p.getName(), p.get().getType(), p.get());

        for (int i = 0; i < mParameters.length; i++)
            env.createVariable(mParameters[i].name, mParameters[i].type, args[i]);

        return Interpreter.evaluate(env, mBody).setReturn(false);
    }

    @Override
    protected Type type() {
        return Type.getLambda();
    }

    @Override
    protected Object object() {
        return null;
    }

    @Override
    protected String string() {
        return String.format("{ lambda passed=%s }", Arrays.toString(mPassed));
    }

}
