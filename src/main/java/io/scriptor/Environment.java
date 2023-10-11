package io.scriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.stmt.Stmt;
import io.scriptor.value.NumValue;
import io.scriptor.value.StrValue;
import io.scriptor.value.Value;

public class Environment {

    private final Environment mParent;
    private final Map<String, Value> mVariables = new HashMap<>();
    private final Map<String, Map<Integer, List<FunDef<?>>>> mFunctions = new HashMap<>();

    public Environment() {
        mParent = null;
    }

    public Environment(Environment parent) {
        mParent = parent;
    }

    public boolean isGlobal() {
        return mParent == null;
    }

    public boolean existsVariable(String id) {
        if (mVariables.containsKey(id))
            return true;
        if (isGlobal())
            return false;
        return mParent.existsVariable(id);
    }

    public <V extends Value> V createVariable(String id, V value) {
        if (mVariables.containsKey(id))
            return null;
        mVariables.put(id, value);
        return value;
    }

    public <V extends Value> V setVariable(String id, V value) {
        if (!mVariables.containsKey(id))
            if (isGlobal())
                return null;
            else
                return mParent.setVariable(id, value);

        if (!mVariables.get(id).sameType(value))
            return null;

        mVariables.put(id, value);
        return value;
    }

    public Value getVariable(String id) {
        if (!mVariables.containsKey(id))
            if (isGlobal())
                return null;
            else
                return mParent.getVariable(id);

        return mVariables.get(id);
    }

    public boolean hasFunction(String name, Class<?>[] params) {
        if (!mFunctions.containsKey(name) || !mFunctions.get(name).containsKey(params.length))
            return false;

        final var functions = mFunctions.get(name).get(params.length);
        for (final var fun : functions) {
            int i = 0;
            for (; i < params.length; i++)
                if (!fun.parameters[i].isOfType(params[i]))
                    break;
            if (i == params.length)
                return true;
        }

        return false;
    }

    public FunDef<?> getFunction(String name, Class<?>... params) {
        if (!mFunctions.containsKey(name) || !mFunctions.get(name).containsKey(params.length))
            if (isGlobal())
                return null;
            else
                return mParent.getFunction(name, params);

        final var functions = mFunctions.get(name).get(params.length);
        for (final var fun : functions) {
            int i = 0;
            for (; i < params.length; i++)
                if (!fun.parameters[i].isOfType(params[i]))
                    break;
            if (i == params.length)
                return fun;
        }

        if (isGlobal())
            return null;

        return mParent.getFunction(name, params);
    }

    public static FunParam<?> createFunParam(Parameter param) {
        final var funParam = switch (param.type) {
            case "num" -> new FunParam<>(NumValue.class);
            case "str" -> new FunParam<>(StrValue.class);
            default -> new FunParam<>(Value.class);
        };
        funParam.name = param.name;
        return funParam;
    }

    public FunDef<?> createFunction(String name, String type, Parameter[] parameters, Stmt[] implementation) {
        final var paramc = parameters == null ? 0 : parameters.length;

        final var params = new FunParam<?>[paramc];
        final var paramsClasses = new Class<?>[paramc];
        for (int i = 0; i < paramc; i++) {
            params[i] = createFunParam(parameters[i]);
            paramsClasses[i] = params[i].type;
        }

        if (hasFunction(name, paramsClasses))
            return null;

        final var fun = switch (type) {
            case "num" -> new FunDef<>(NumValue.class);
            case "str" -> new FunDef<>(StrValue.class);
            default -> new FunDef<>(Value.class);
        };

        fun.parameters = params;
        fun.implementation = implementation;

        mFunctions
                .computeIfAbsent(name, id -> new HashMap<>())
                .computeIfAbsent(paramc, argc -> new Vector<>())
                .add(fun);

        return fun;
    }
}
