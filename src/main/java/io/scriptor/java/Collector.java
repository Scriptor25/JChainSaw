package io.scriptor.java;

import static io.scriptor.csaw.impl.Types.TYPE_ANY;
import static io.scriptor.csaw.impl.Types.TYPE_CHR;
import static io.scriptor.csaw.impl.Types.TYPE_LAMBDA;
import static io.scriptor.csaw.impl.Types.TYPE_NUM;
import static io.scriptor.csaw.impl.Types.TYPE_STR;
import static io.scriptor.csaw.impl.interpreter.Environment.createAlias;
import static io.scriptor.csaw.impl.interpreter.Environment.createType;
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
import io.scriptor.csaw.impl.interpreter.value.ChrValue;
import io.scriptor.csaw.impl.interpreter.value.LambdaValue;
import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.csaw.impl.interpreter.value.Value;

public class Collector {

    private Collector() {
    }

    public static void collect(Environment env) {

        final var result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan();
        final var classes = result.getClassesWithAnnotation(CSawNative.class).loadClasses();
        for (final var cls : classes) {
            final var typename = cls.getAnnotation(CSawNative.class).value().trim();

            final var fields = cls.getDeclaredFields();
            final List<Parameter> typefields = new Vector<>();
            for (final var fld : fields) {
                if (!Modifier.isPublic(fld.getModifiers()))
                    continue;

                final var field = new Parameter();
                field.name = fld.getName();
                field.type = getType(env, fld.getType());
                typefields.add(field);
            }
            createType(null, typename, typefields.toArray(new Parameter[0]));
            createAlias(cls.getName(), typename);

            final var constructors = cls.getDeclaredConstructors();
            for (final var cnstr : constructors) {
                if (!Modifier.isPublic(cnstr.getModifiers()))
                    continue;

                final var params = new String[cnstr.getParameterCount() - (cnstr.isVarArgs() ? 1 : 0)];
                for (int i = 0; i < params.length; i++)
                    params[i] = getType(env, cnstr.getParameterTypes()[i]);

                final IFunBody body = (member, args) -> {
                    return handle(() -> (Value) cnstr.newInstance((Object[]) args));
                };

                registerFunction(
                        false,
                        typename,
                        typename,
                        params,
                        cnstr.isVarArgs() ? "va" : null,
                        null,
                        body);
            }

            final var methods = cls.getDeclaredMethods();
            for (final var mthd : methods) {
                if (!Modifier.isPublic(mthd.getModifiers()))
                    continue;

                final var params = new String[mthd.getParameterCount() - (mthd.isVarArgs() ? 1 : 0)];
                for (int i = 0; i < params.length; i++)
                    params[i] = getType(env, mthd.getParameterTypes()[i]);

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
                        getType(env, mthd.getReturnType()),
                        params,
                        mthd.isVarArgs() ? "va" : null,
                        Modifier.isStatic(mthd.getModifiers()) ? null : typename,
                        body);

                if (mthd.isAnnotationPresent(CSawAlias.class))
                    registerFunction(
                            false,
                            mthd.getAnnotation(CSawAlias.class).value(),
                            getType(env, mthd.getReturnType()),
                            params,
                            mthd.isVarArgs() ? "va" : null,
                            Modifier.isStatic(mthd.getModifiers()) ? null : typename,
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

    private static String getType(Environment env, Class<?> cls) {
        if (cls.equals(Void.class) || cls.equals(void.class))
            return null;

        if (cls.equals(NumValue.class))
            return TYPE_NUM;

        if (cls.equals(ChrValue.class))
            return TYPE_CHR;

        if (cls.equals(StrValue.class))
            return TYPE_STR;

        if (cls.equals(LambdaValue.class))
            return TYPE_LAMBDA;

        if (cls.equals(Value.class))
            return TYPE_ANY;

        if (!hasAlias(cls.getName()))
            throw new CSawException("unhandled class-to-type conversion for class '%s'", cls);

        return getOrigin(cls.getName());
    }
}
