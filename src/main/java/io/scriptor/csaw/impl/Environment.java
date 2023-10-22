package io.scriptor.csaw.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.csaw.impl.stmt.Stmt;
import io.scriptor.csaw.impl.value.Value;

public class Environment {

    private final Environment mParent;

    private final Map<String, Pair<String, Value>> mVariables = new HashMap<>();
    private final Map<String, Map<String, List<FunDef>>> mFunctions = new HashMap<>();

    private final Map<String, Parameter[]> mTypes = new HashMap<>();
    private final Map<String, String> mAlias = new HashMap<>();
    private final Map<String, List<String>> mGroups = new HashMap<>();

    private String mPath;

    public Environment(String path) {
        mParent = null;
        mPath = path;
    }

    public Environment(Environment parent) {
        mParent = parent;
        mPath = parent.mPath;
    }

    public boolean isGlobal() {
        return mParent == null;
    }

    public Environment setPath(String path) {
        mPath = path;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public boolean existsVariable(String id) {
        if (mVariables.containsKey(id))
            return true;
        if (isGlobal())
            return false;
        return mParent.existsVariable(id);
    }

    public <V extends Value> V createVariable(String name, String type, V value) {
        if (mVariables.containsKey(name))
            throw new IllegalStateException(String.format("variable '%s' already existing", name));
        mVariables.put(name, new Pair<>(type, value));
        return value;
    }

    public <V extends Value> V setVariable(String id, V value) {
        if (!mVariables.containsKey(id))
            if (isGlobal())
                throw new RuntimeException();
            else
                return mParent.setVariable(id, value);

        if (!isAssignable(value.getType(), mVariables.get(id).first))
            throw new RuntimeException();

        mVariables.get(id).second = value;
        return value;
    }

    public Value getVariable(String id) {
        if (!mVariables.containsKey(id))
            if (isGlobal())
                throw new IllegalStateException(String.format("undefined variable '%s'", id));
            else
                return mParent.getVariable(id);

        return mVariables.get(id).second;
    }

    public boolean hasFunction(String member, String name, String[] types) {
        if (!mFunctions.containsKey(member) || !mFunctions.get(member).containsKey(name))
            return false;

        final var functions = mFunctions.get(member).get(name);
        for (final var fun : functions) {
            if (fun.parameters.length != types.length) // wrong params number
                continue;

            int i = 0;
            for (; i < fun.parameters.length; i++)
                if (!isAssignable(types[i], fun.parameters[i]))
                    break;
            if (i == fun.parameters.length)
                return true;
        }

        return false;
    }

    public FunDef getFunction(String member, String name, String... params) {
        if (!mFunctions.containsKey(member) || !mFunctions.get(member).containsKey(name))
            if (isGlobal())
                throw new IllegalStateException(
                        String.format("undefined function '%s', member of '%s', types %s",
                                name,
                                member,
                                Arrays.toString(params)));
            else
                return mParent.getFunction(member, name, params);

        final var functions = mFunctions.get(member).get(name);
        for (final var fun : functions) {
            if (!fun.vararg && fun.parameters.length != params.length) // wrong params number and not vararg
                continue;
            if (fun.vararg && fun.parameters.length > params.length) // vararg but not enough params
                continue;

            int i = 0;
            for (; i < fun.parameters.length; i++)
                if (!isAssignable(params[i], fun.parameters[i]))
                    break;
            if (i == fun.parameters.length || (fun.vararg && i < params.length)) // either right number of
                                                                                 // params or the function has
                                                                                 // to be vararg and then the
                                                                                 // number must be less than the
                                                                                 // params length
                return fun;
        }

        if (isGlobal())
            throw new IllegalStateException(
                    String.format("undefined function '%s', member of '%s', types %s",
                            name,
                            member,
                            Arrays.toString(params)));

        return mParent.getFunction(member, name, params);
    }

    public Value getAndInvoke(Value member, String name, Value... args) throws Exception {
        final var argTypes = new String[args == null ? 0 : args.length];
        for (int i = 0; i < argTypes.length; i++)
            argTypes[i] = args[i].getType();

        return getFunction(member != null ? member.getType() : null, name, argTypes).body.invoke(member, this, args);
    }

    public FunDef createFunction(
            boolean constructor,
            String name,
            String type,
            Parameter[] params,
            boolean vararg,
            String member,
            Stmt[] body) {

        final var paramc = params == null ? 0 : params.length;

        final var parameters = new String[paramc];
        final var paramTypes = new String[paramc];
        for (int i = 0; i < paramc; i++) {
            parameters[i] = params[i].name;
            paramTypes[i] = params[i].type;
        }

        if (hasFunction(member, name, paramTypes))
            throw new RuntimeException(
                    String.format("function '%s' %s already defined on type '%s'",
                            name,
                            Arrays.toString(paramTypes),
                            member));

        final var fun = new FunDef.Builder()
                .constructor(constructor)
                .type(type)
                .parameters(paramTypes)
                .vararg(vararg)
                .member(member)
                .body(new FunDef.FunBody(parameters, body))
                .build();

        mFunctions
                .computeIfAbsent(member, key -> new HashMap<>())
                .computeIfAbsent(name, key -> new Vector<>())
                .add(fun);

        return fun;
    }

    public void registerFunction(
            boolean constructor,
            String name,
            String type,
            String[] params,
            boolean vararg,
            String member,
            IFunBody body) {

        final var paramc = params == null ? 0 : params.length;

        final var parameters = new String[paramc];
        for (int i = 0; i < paramc; i++)
            parameters[i] = params[i];

        if (hasFunction(member, name, parameters))
            throw new RuntimeException(
                    String.format("function '%s' %s already defined on type '%s'",
                            name,
                            Arrays.toString(parameters),
                            member));

        final var fun = new FunDef.Builder()
                .constructor(constructor)
                .type(type)
                .parameters(parameters)
                .vararg(vararg)
                .member(member)
                .body(body)
                .build();

        mFunctions
                .computeIfAbsent(member, key -> new HashMap<>())
                .computeIfAbsent(name, key -> new Vector<>())
                .add(fun);
    }

    public void createAlias(String alias, String type) {
        mAlias.put(alias, type);
    }

    private void putType(String name, Parameter[] fields) {
        if (!mTypes.containsKey(name))
            if (isGlobal())
                return;
            else
                mParent.putType(name, fields);
        mTypes.put(name, fields);
    }

    public void createType(String group, String name, Parameter[] fields) {
        if (existsType(name)) {
            if (getType(name) != null)
                throw new IllegalStateException(String.format("cannot redefine non-opaque type '%s'", name));
            putType(name, fields);
            return;
        }

        mTypes.put(name, fields);
        mGroups.computeIfAbsent(group, key -> new Vector<>()).add(name);
    }

    public String getAlias(String alias) {
        if (!mAlias.containsKey(alias))
            if (isGlobal())
                throw new IllegalStateException(String.format("undefined alias '%s'", alias));
            else
                return mParent.getAlias(alias);
        return mAlias.get(alias);
    }

    public boolean hasAlias(String alias) {
        if (!mAlias.containsKey(alias))
            if (isGlobal())
                return false;
            else
                return mParent.hasAlias(alias);
        return true;
    }

    public String getOrigin(String alias) {
        if (!mAlias.containsKey(alias))
            if (isGlobal())
                return alias;
            else
                return mParent.getOrigin(alias);
        return getOrigin(mAlias.get(alias));
    }

    public boolean existsType(String name) {
        if (!mTypes.containsKey(name))
            if (isGlobal())
                return false;
            else
                return mParent.existsType(name);
        return true;
    }

    public Parameter[] getType(String name) {
        if (!mTypes.containsKey(name))
            if (isGlobal())
                throw new IllegalStateException(String.format("undefined type '%s'", name));
            else
                return mParent.getType(name);
        return mTypes.get(name);
    }

    public boolean isAliasFor(String type, String alias) {
        if (type.equals(alias))
            return true;
        if (!mAlias.containsKey(type))
            if (isGlobal())
                return false;
            else
                return mParent.isAliasFor(type, alias);
        return isAliasFor(mAlias.get(type), alias);
    }

    public boolean isAssignable(String type, String to) {
        if (to.equals(Value.TYPE_ANY) || type.equals(to) || isAliasFor(type, to) || isAliasFor(to, type))
            return true;
        if (!mGroups.containsKey(to))
            if (isGlobal())
                return false;
            else
                return mParent.isAssignable(type, to);
        return mGroups.get(to).contains(type);
    }
}
