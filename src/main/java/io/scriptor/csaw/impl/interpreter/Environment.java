package io.scriptor.csaw.impl.interpreter;

import static io.scriptor.csaw.impl.Types.TYPE_ANY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Pair;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.csaw.impl.stmt.EnclosedStmt;

public class Environment {

    private static Environment GLOBAL;

    private static final Map<String, Map<String, List<FunDef>>> FUNCTIONS = new HashMap<>();
    private static final Map<String, String> ALIAS = new HashMap<>();
    private static final Map<String, Parameter[]> TYPES = new HashMap<>();
    private static final Map<String, List<String>> GROUPS = new HashMap<>();

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
        TYPES.clear();
        GROUPS.clear();
    }

    public static boolean hasFunction(String member, String name, String... types) {
        if (FUNCTIONS.containsKey(member) && FUNCTIONS.get(member).containsKey(name)) {
            final var functions = FUNCTIONS.get(member).get(name);
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
        }

        return false;
    }

    public static FunDef createFunction(
            boolean constructor,
            String name,
            String type,
            Parameter[] params,
            boolean vararg,
            String member,
            EnclosedStmt body) {

        final var paramc = params == null ? 0 : params.length;

        final var parameters = new String[paramc];
        final var paramTypes = new String[paramc];
        for (int i = 0; i < paramc; i++) {
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

    public static FunDef getFunction(String member, String name, String... types) {
        if (FUNCTIONS.containsKey(member) && FUNCTIONS.get(member).containsKey(name)) {
            final var functions = FUNCTIONS.get(member).get(name);
            for (final var fun : functions) {
                if (!fun.vararg && fun.parameters.length != types.length) // wrong params number and not vararg
                    continue;
                if (fun.vararg && fun.parameters.length > types.length) // vararg but not enough params
                    continue;

                int i = 0;
                for (; i < fun.parameters.length; i++)
                    if (!isAssignable(types[i], fun.parameters[i]))
                        break;
                if (i == fun.parameters.length || (fun.vararg && i < types.length)) // either right number of
                                                                                    // params or the function has
                                                                                    // to be vararg and then the
                                                                                    // number must be less than the
                                                                                    // params length
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
        final var types = new String[args == null ? 0 : args.length];
        for (int i = 0; i < types.length; i++)
            types[i] = args[i].getType();

        return getFunction(member != null ? member.getType() : null, name, types).invoke(member, args);
    }

    public static boolean hasAlias(String type) {
        return ALIAS.containsKey(type);
    }

    public static void createAlias(String alias, String type) {
        if (hasAlias(type))
            throw new CSawException("alias already defined for type '%s'", type);
        ALIAS.put(alias, type);
    }

    public static String getAlias(String type) {
        if (!hasAlias(type))
            throw new CSawException("undefined alias for type '%s'", type);
        return ALIAS.get(type);
    }

    public static String getOrigin(String type) {
        if (!hasAlias(type))
            return type;
        return getOrigin(getAlias(type));
    }

    public static boolean hasType(String type) {
        return TYPES.containsKey(type);
    }

    public static void createType(String group, String name, Parameter[] fields) {
        if (hasType(name)) {
            if (getType(name) != null) // type is not opaque
                throw new CSawException("cannot redefine non-opaque type '%s'", name);
            TYPES.put(name, fields);
            return;
        }

        TYPES.put(name, fields);
        GROUPS.computeIfAbsent(group, key -> new Vector<>()).add(name);
    }

    public static Parameter[] getType(String type) {
        if (!hasType(type))
            if (!hasAlias(type))
                throw new CSawException("undefined type '%s'", type);
            else
                return getType(getAlias(type));
        return TYPES.get(type);
    }

    public static boolean isAliasFor(String type, String aliasFor) {
        return getOrigin(type).equals(getOrigin(aliasFor));
    }

    public static boolean isAssignable(String type, String to) {
        if (TYPE_ANY.equals(to) || TYPE_ANY.equals(type) || isAliasFor(type, to))
            return true;
        if (GROUPS.containsKey(to))
            return GROUPS.get(to).contains(type);
        return false;
    }

    private final Map<String, Pair<String, Value>> mVariables = new HashMap<>();
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

    public <V extends Value> V createVariable(String id, String type, V value) {
        if (hasVariable(id))
            throw new CSawException("variable '%s' already defined", id);
        mVariables.put(id, new Pair<>(type, value));
        return value;
    }

    private Pair<String, Value> getVarEntry(String id) {
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
