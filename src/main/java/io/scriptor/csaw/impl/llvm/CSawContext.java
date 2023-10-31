package io.scriptor.csaw.impl.llvm;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import io.scriptor.csaw.impl.Pair;
import io.scriptor.java.ErrorUtil;

public class CSawContext {

    private static final Map<String, List<Pair<LLVMTypeRef[], Pair<UUID, LLVMTypeRef>>>> FUNCTIONS = new HashMap<>();

    public static UUID createFunction(String name, LLVMTypeRef fn, LLVMTypeRef... types) {
        final var uuid = UUID.randomUUID();
        FUNCTIONS
                .computeIfAbsent(name, key -> new Vector<>())
                .add(new Pair<>(types, new Pair<>(uuid, fn)));
        return uuid;
    }

    public static Pair<UUID, LLVMTypeRef> getFunction(String name, LLVMTypeRef[] types) {
        final var funcs = FUNCTIONS.get(name);
        for (final var fun : funcs) {
            if (fun.first.length != types.length)
                continue;
            int i = 0;
            for (; i < types.length; i++)
                if (!fun.first[i].equals(types[i]))
                    break;
            if (i == types.length)
                return fun.second;
        }

        ErrorUtil.error("undefined function '%s', %s", name, Arrays.toString(types));
        return null;
    }

    private final File mFile;
    private final Map<String, LLVMValueRef> mValues = new HashMap<>();

    public CSawContext(File file) {
        mFile = file;
    }

    public CSawContext(CSawContext ctx) {
        mFile = ctx.mFile;
        mValues.putAll(ctx.mValues);
    }

    public File getFile() {
        return mFile;
    }

    public void createValue(String id, LLVMValueRef value) {
        if (mValues.containsKey(id))
            ErrorUtil.error("value '%s' already defined", id);

        mValues.put(id, value);
    }

    public LLVMValueRef getValue(String id) {
        if (!mValues.containsKey(id))
            ErrorUtil.error("value '%s' undefined", id);

        return mValues.get(id);
    }
}
