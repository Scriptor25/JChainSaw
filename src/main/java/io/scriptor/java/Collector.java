package io.scriptor.java;

import static io.scriptor.csaw.impl.interpreter.Environment.createAlias;
import static io.scriptor.csaw.impl.interpreter.Environment.createThing;
import static io.scriptor.csaw.impl.interpreter.Environment.getOrigin;
import static io.scriptor.csaw.impl.interpreter.Environment.hasAlias;
import static io.scriptor.csaw.impl.interpreter.Environment.registerFunction;
import static io.scriptor.java.ErrorUtil.handle;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import io.github.classgraph.ClassGraph;
import io.scriptor.csaw.impl.CSawException;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.IFunBody;
import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstChr;
import io.scriptor.csaw.impl.interpreter.value.ConstLambda;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.csaw.impl.interpreter.value.Value;

public class Collector {

    private Collector() {
    }

    public static void collect(Environment env) {

        final var result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan();
        final var classes = result.getClassesWithAnnotation(CSawNative.class).loadClasses();
        for (final var cls : classes) {
            final var typename = cls.getAnnotation(CSawNative.class).value().trim();
            final var type = Type.get(typename);

            final var fields = cls.getDeclaredFields();
            final List<Parameter> typefields = new Vector<>();
            for (final var fld : fields) {
                if (!Modifier.isPublic(fld.getModifiers()))
                    continue;

                typefields.add(new Parameter(fld.getName(), getType(fld.getType())));
            }
            createThing("", typename, typefields.toArray(new Parameter[0]));
            createAlias(cls.getName(), type);

            final var constructors = cls.getDeclaredConstructors();
            for (final var cnstr : constructors) {
                if (!Modifier.isPublic(cnstr.getModifiers()))
                    continue;

                final var params = new Type[cnstr.getParameterCount() - (cnstr.isVarArgs() ? 1 : 0)];
                for (int i = 0; i < params.length; i++)
                    params[i] = getType(cnstr.getParameterTypes()[i]);

                final IFunBody body = (member, args) -> {
                    return handle(() -> (Value) cnstr.newInstance((Object[]) args));
                };

                registerFunction(
                        false,
                        typename,
                        type,
                        params,
                        cnstr.isVarArgs() ? "va" : null,
                        Type.getNull(),
                        body);
            }

            final var methods = cls.getDeclaredMethods();
            for (final var mthd : methods) {
                if (!Modifier.isPublic(mthd.getModifiers()))
                    continue;

                final var params = new Type[mthd.getParameterCount() - (mthd.isVarArgs() ? 1 : 0)];
                for (int i = 0; i < params.length; i++)
                    params[i] = getType(mthd.getParameterTypes()[i]);

                final IFunBody body = (member, args) -> {
                    return Value.class.cast(handle(() -> mthd.invoke(
                            member,
                            mthd.isVarArgs()
                                    ? prepareArgs(mthd.getParameterCount(), args)
                                    : args)));
                };

                registerFunction(
                        false,
                        mthd.getName(),
                        getType(mthd.getReturnType()),
                        params,
                        mthd.isVarArgs() ? "va" : null,
                        Modifier.isStatic(mthd.getModifiers()) ? Type.getNull() : type,
                        body);

                if (mthd.isAnnotationPresent(CSawAlias.class))
                    registerFunction(
                            false,
                            mthd.getAnnotation(CSawAlias.class).value(),
                            getType(mthd.getReturnType()),
                            params,
                            mthd.isVarArgs() ? "va" : null,
                            Modifier.isStatic(mthd.getModifiers()) ? Type.getNull() : type,
                            body);
            }
        }
        result.close();
    }

    private static Object[] prepareArgs(int paramsCount, Value[] args) {
        final var pc = paramsCount - 1;
        final var allArgs = new Object[pc + 1];
        final var varArgs = new Value[args.length - pc];

        int i = 0;
        for (; i < allArgs.length - 1; i++)
            allArgs[i] = args[i];
        for (int j = i; i < args.length; i++)
            varArgs[i - j] = args[i];

        allArgs[allArgs.length - 1] = varArgs;

        return allArgs;
    }

    private static Type getType(Class<?> cls) {
        if (cls.equals(Void.class) || cls.equals(void.class))
            return Type.getNull();

        if (cls.equals(ConstNum.class))
            return Type.getNum();

        if (cls.equals(ConstChr.class))
            return Type.getChr();

        if (cls.equals(ConstStr.class))
            return Type.getStr();

        if (cls.equals(ConstLambda.class))
            return Type.getLambda();

        if (cls.equals(Value.class))
            return Type.getAny();

        if (!hasAlias(cls.getName()))
            throw new CSawException("unhandled class-to-type conversion for class '%s'", cls);

        return getOrigin(Type.get(cls.getName()));
    }
}
