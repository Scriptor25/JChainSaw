package io.scriptor.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import io.github.classgraph.ClassGraph;
import io.scriptor.csaw.impl.Environment;
import io.scriptor.csaw.impl.IFunBody;
import io.scriptor.csaw.impl.Parameter;
import io.scriptor.csaw.impl.value.ChrValue;
import io.scriptor.csaw.impl.value.NativeValue;
import io.scriptor.csaw.impl.value.NumValue;
import io.scriptor.csaw.impl.value.StrValue;
import io.scriptor.csaw.impl.value.Value;

public class Collector {

    private Collector() {
    }

    public static void collect(Environment env) {

        try (final var result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            final var classes = result.getClassesWithAnnotation(CSawNative.class).loadClasses();
            for (final var cls : classes) {
                final var typename = cls.getAnnotation(CSawNative.class).value().trim();
                // System.out.printf("%s: '%s'%n", cls, typename);

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
                env.createType(null, typename, typefields.toArray(new Parameter[0]));
                env.createAlias(cls.getName(), typename);

                final var constructors = cls.getDeclaredConstructors();
                for (final var cnstr : constructors) {
                    if (!Modifier.isPublic(cnstr.getModifiers()))
                        continue;

                    try {
                        final var params = new String[cnstr.getParameterCount() - (cnstr.isVarArgs() ? 1 : 0)];
                        for (int i = 0; i < params.length; i++)
                            params[i] = getType(env, cnstr.getParameterTypes()[i]);

                        final IFunBody body = (member, environment, args) -> {
                            try {
                                return new NativeValue(cnstr.newInstance((Object[]) args));
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                    | InstantiationException e) {
                                e.printStackTrace();
                                return null;
                            }
                        };

                        env.registerFunction(
                                false,
                                typename,
                                typename,
                                params,
                                cnstr.isVarArgs(),
                                null,
                                body);
                    } catch (IllegalStateException e) {
                        System.out.println(e.getMessage());
                    }
                }

                final var methods = cls.getDeclaredMethods();
                for (final var mthd : methods) {
                    if (!Modifier.isPublic(mthd.getModifiers()))
                        continue;

                    final var params = new String[mthd.getParameterCount() - (mthd.isVarArgs() ? 1 : 0)];
                    for (int i = 0; i < params.length; i++)
                        params[i] = getType(env, mthd.getParameterTypes()[i]);

                    final IFunBody body = (member, environment, args) -> {
                        final var object = member != null ? member.getValue() : null;
                        if (mthd.isVarArgs()) {
                            final var pc = mthd.getParameterCount() - 1;
                            final var allArgs = new Object[pc + 1];
                            final var varArgs = new Value[args.length - pc];

                            int i = 0;
                            for (; i < allArgs.length - 1; i++)
                                allArgs[i] = args[i];
                            for (int j = i; i < args.length; i++)
                                varArgs[i - j] = args[i];

                            allArgs[allArgs.length - 1] = varArgs;

                            return Value.class.cast(mthd.invoke(object, allArgs));
                        } else
                            return Value.class.cast(mthd.invoke(object, (Object[]) args));
                    };

                    env.registerFunction(
                            false,
                            mthd.getName(),
                            getType(env, mthd.getReturnType()),
                            params,
                            mthd.isVarArgs(),
                            Modifier.isStatic(mthd.getModifiers()) ? null : typename,
                            body);
                }
            }
        }
    }

    private static String getType(Environment env, Class<?> cls) {
        if (cls.equals(Void.class) || cls.equals(void.class))
            return null;

        if (cls.equals(NumValue.class))
            return Value.TYPE_NUM;

        if (cls.equals(ChrValue.class))
            return Value.TYPE_CHR;

        if (cls.equals(StrValue.class))
            return Value.TYPE_STR;

        if (cls.equals(Value.class))
            return Value.TYPE_ANY;

        if (!env.hasAlias(cls.getName()))
            throw new IllegalStateException(String.format("unhandled class-to-type conversion for class '%s'", cls));

        return env.getOrigin(cls.getName());
    }
}
