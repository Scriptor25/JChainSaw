package io.scriptor.csaw.impl.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Pair;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.csaw.impl.stmt.EnclosedStmt;

public class Environment {

    private static Environment GLOBAL;

    private static final Map<Type, Map<String, List<FunDef>>> FUNCTIONS = new ConcurrentHashMap<>();
    private static final Map<String, Type> ALIAS = new ConcurrentHashMap<>();
    private static final Map<String, Parameter[]> THINGS = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> GROUPS = new ConcurrentHashMap<>();

    public static Environment initGlobal(String path) {
        return GLOBAL = new Environment(path);
    }

    public static Environment getGlobal() {
        return GLOBAL;
    }

    public static void reset() {
        GLOBAL.mVariables.clear();

        FUNCTIONS.clear();
        ALIAS.clear();
        THINGS.clear();
        GROUPS.clear();
    }

    public static boolean hasFunction(Type member, String name, Type... types) {
        if (FUNCTIONS.containsKey(member) && FUNCTIONS.get(member).containsKey(name)) {
            final var functions = FUNCTIONS.get(member).get(name);
            for (final var fun : functions) {
                if (fun.vararg == null && fun.parameters.length != types.length) // wrong params number
                    continue;
                if (fun.vararg != null && fun.parameters.length > types.length) // vararg but not enough params
                    continue;

                int i = 0;
                for (; i < fun.parameters.length; i++)
                    if (!isAssignable(types[i], fun.parameters[i]))
                        break;
                if (i == fun.parameters.length)
                    return true;
            }
        }

        return false;
    }

    public static FunDef createFunction(
            boolean constructor,
            String name,
            Type type,
            Parameter[] params,
            String vararg,
            Type member,
            EnclosedStmt body) {

        final var parameters = new String[params.length];
        final var paramTypes = new Type[params.length];
        for (int i = 0; i < params.length; i++) {
            parameters[i] = params[i].name;
            paramTypes[i] = params[i].type;
        }

        if (hasFunction(member, name, paramTypes))
            throw new CSawException(
                    "function '%s' %s already defined on type '%s'",
                    name,
                    Arrays.toString(paramTypes),
                    member);

        final var fun = new FunDef.Builder()
                .constructor(constructor)
                .type(type)
                .parameters(paramTypes)
                .vararg(vararg)
                .member(member)
                .body(new FunDef.FunBody(parameters, body))
                .build();

        FUNCTIONS
                .computeIfAbsent(member, key -> new HashMap<>())
                .computeIfAbsent(name, key -> new Vector<>())
                .add(fun);

        return fun;
    }

    public static void registerFunction(
            boolean constructor,
            String name,
            Type type,
            Type[] params,
            String vararg,
            Type member,
            IFunBody body) {

        final var parameters = new Type[params.length];
        for (int i = 0; i < params.length; i++)
            parameters[i] = params[i];

        if (hasFunction(member, name, parameters))
            throw new CSawException(
                    "function '%s' %s already defined on type '%s'",
                    name,
                    Arrays.toString(parameters),
                    member);

        final var fun = new FunDef.Builder()
                .constructor(constructor)
                .type(type)
                .parameters(parameters)
                .vararg(vararg)
                .member(member)
                .body(body)
                .build();

        FUNCTIONS
                .computeIfAbsent(member, key -> new HashMap<>())
                .computeIfAbsent(name, key -> new Vector<>())
                .add(fun);
    }

    public static FunDef getFunction(Type member, String name, Type... types) {
        if (FUNCTIONS.containsKey(member) && FUNCTIONS.get(member).containsKey(name)) {
            final var functions = FUNCTIONS.get(member).get(name);
            for (final var fun : functions) {
                if (fun.vararg == null && fun.parameters.length != types.length) // wrong params number and not vararg
                    continue;
                if (fun.vararg != null && fun.parameters.length > types.length) // vararg but not enough params
                    continue;

                int i = 0;
                for (; i < fun.parameters.length; i++)
                    if (!isAssignable(types[i], fun.parameters[i]))
                        break;
                if (i == fun.parameters.length)
                    return fun;
            }
        }

        throw new CSawException(
                "undefined function '%s', member of '%s', types %s",
                name,
                member,
                Arrays.toString(types));
    }

    public static Value getAndInvoke(Value member, String name, Value... args) {
        final var types = new Type[args.length];
        for (int i = 0; i < types.length; i++)
            types[i] = args[i].getType();

        return getFunction(member.getType(), name, types).invoke(member, args);
    }

    public static boolean hasAlias(String alias) {
        return ALIAS.containsKey(alias);
    }

    public static void createAlias(String alias, Type type) {
        if (hasAlias(alias))
            throw new CSawException("alias '%s' already defined", alias);
        ALIAS.put(alias, type);
    }

    public static Type getAlias(String alias) {
        if (!hasAlias(alias))
            throw new CSawException("undefined alias for type '%s'", alias);
        return ALIAS.get(alias);
    }

    public static Type getOrigin(Type type) {
        if (!hasAlias(type.name))
            return type;
        return getOrigin(getAlias(type.name));
    }

    public static boolean hasThing(String name) {
        return THINGS.containsKey(name);
    }

    public static void createThing(String group, String name, Parameter[] fields) {
        if (hasThing(name)) {
            if (getThing(name) != null) // type is not opaque
                throw new CSawException("cannot redefine non-opaque type '%s'", name);
            THINGS.put(name, fields);
            return;
        }

        THINGS.put(name, fields);
        GROUPS.computeIfAbsent(group, key -> new Vector<>()).add(name);
    }

    public static Parameter[] getThing(String name) {
        if (!hasThing(name))
            if (!hasAlias(name))
                throw new CSawException("undefined type '%s'", name);
            else
                return getThing(getAlias(name).name);
        return THINGS.get(name);
    }

    public static boolean isAliasFor(Type type, Type aliasFor) {
        return getOrigin(type).equals(getOrigin(aliasFor));
    }

    public static boolean isAssignable(Type type, Type to) {
        if (Type.getAny().equals(to) /* || Type.ANY.equals(type) */ || isAliasFor(type, to))
            return true;
        if (GROUPS.containsKey(to.name))
            return GROUPS.get(to.name).contains(type.name);
        return false;
    }

    private final Map<String, Pair<Type, Value>> mVariables = new HashMap<>();
    private String mPath;

    private Environment(String path) {
        mPath = path;
    }

    public Environment(Environment parent) {
        mVariables.putAll(parent.mVariables);
        mPath = parent.mPath;
    }

    public Environment setPath(String path) {
        mPath = path;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public boolean hasVariable(String id) {
        return mVariables.containsKey(id);
    }

    public <V extends Value> V createVariable(String id, Type type, V value) {
        if (hasVariable(id))
            throw new CSawException("variable '%s' already defined", id);
        mVariables.put(id, new Pair<>(type, value));
        return value;
    }

    private Pair<Type, Value> getVarEntry(String id) {
        if (!hasVariable(id))
            throw new CSawException("undefined variable '%s'", id);
        return mVariables.get(id);
    }

    public Value getVariable(String id) {
        return getVarEntry(id).second;
    }

    public <V extends Value> V setVariable(String id, V value) {
        final var variable = getVarEntry(id);
        if (!isAssignable(value.getType(), variable.first))
            throw new CSawException(
                    "variable '%s' (%s) cannot be assigned to value of type '%s'",
                    id,
                    variable.first,
                    value.getType());

        variable.second = value;
        return value;
    }
}
