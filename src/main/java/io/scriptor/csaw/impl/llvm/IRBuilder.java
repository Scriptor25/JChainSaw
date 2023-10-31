package io.scriptor.csaw.impl.llvm;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;

import io.scriptor.csaw.impl.*;
import io.scriptor.csaw.impl.expr.*;
import io.scriptor.csaw.impl.stmt.*;
import io.scriptor.java.*;

import static org.bytedeco.llvm.global.LLVM.*;
import static io.scriptor.csaw.impl.Types.*;

import java.io.*;

public class IRBuilder {

    // a 'char *' used to retrieve error messages from LLVM
    private static final BytePointer error = new BytePointer();

    private static LLVMContextRef context;
    private static LLVMModuleRef module;
    private static LLVMBuilderRef builder;

    private IRBuilder() {
    }

    public static LLVMTypeRef getType(String type) {
        if (type == null)
            return LLVMVoidType();

        switch (type) {
            case TYPE_ANY:
                return LLVMPointerType(LLVMVoidType(), 0); // void pointer
            case TYPE_NUM:
                return LLVMDoubleType(); // double
            case TYPE_STR:
                return LLVMPointerType(LLVMInt8Type(), 0); // char pointer
            case TYPE_CHR:
                return LLVMInt8Type(); // char
        }

        return ErrorUtil.error("undefined type '%s'", type);
    }

    public static void initLLVM(String moduleID) {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Build the fibonacci function.
        context = LLVMContextCreate();
        module = LLVMModuleCreateWithNameInContext(moduleID, context);
        builder = LLVMCreateBuilderInContext(context);
    }

    public static void verify() {
        // Verify the module using LLVMVerifier
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            throw new CSawException();
        }
    }

    public static void optimize() {
        // Create a pass pipeline using the legacy pass manager
        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddInstructionCombiningPass(pm);
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMRunPassManager(pm, module);
        LLVMDumpModule(module);
    }

    public static LLVMValueRef build(CSawContext ctx, Stmt stmt) {
        if (stmt instanceof AliasStmt)
            return build(ctx, (AliasStmt) stmt);
        if (stmt instanceof ForStmt)
            return build(ctx, (ForStmt) stmt);
        if (stmt instanceof FunStmt)
            return build(ctx, (FunStmt) stmt);
        if (stmt instanceof IfStmt)
            return build(ctx, (IfStmt) stmt);
        if (stmt instanceof IncStmt)
            return build(ctx, (IncStmt) stmt);
        if (stmt instanceof RetStmt)
            return build(ctx, (RetStmt) stmt);
        if (stmt instanceof ThingStmt)
            return build(ctx, (ThingStmt) stmt);
        if (stmt instanceof VarStmt)
            return build(ctx, (VarStmt) stmt);
        if (stmt instanceof WhileStmt)
            return build(ctx, (WhileStmt) stmt);

        if (stmt instanceof Expr)
            return build(ctx, (Expr) stmt);

        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, AliasStmt stmt) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, ForStmt stmt) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, FunStmt stmt) {

        final var paramCount = (stmt.member != null ? 1 : 0) + (stmt.parameters != null ? stmt.parameters.length : 0);

        final var paramTypes = new PointerPointer<>(paramCount);
        final var types = new LLVMTypeRef[paramCount];
        final var names = new String[paramCount];
        for (var i = 0; i < paramCount; i++) {
            final var type = getType(
                    stmt.member != null
                            ? (i == 0 ? stmt.member : stmt.parameters[i - 1].type)
                            : stmt.parameters[i].type);
            paramTypes.put(i, type);
            types[i] = type;

            names[i] = stmt.member != null
                    ? (i == 0 ? "my" : stmt.parameters[i - 1].name)
                    : stmt.parameters[i].name;
        }

        final var rettype = getType(stmt.type);
        final var funtype = LLVMFunctionType(rettype, paramTypes, paramCount, stmt.vararg ? 1 : 0);

        final var uuid = CSawContext.createFunction(stmt.name, rettype, types);
        final var fun = LLVMAddFunction(module, uuid.toString(), funtype);

        if (stmt.body == null)
            return null;

        final var funCtx = new CSawContext(ctx);

        for (var i = 0; i < paramCount; i++) {
            final var param = LLVMGetParam(fun, i);
            LLVMSetValueName2(param, names[i], names[i].length());
            funCtx.createValue(names[i], param);
        }

        if (stmt.constructor) {
            final var my = LLVMBuildAlloca(builder, LLVMGetReturnType(funtype), "my");
            funCtx.createValue("my", my);
        }

        final var entry = LLVMAppendBasicBlock(fun, "entry");

        LLVMPositionBuilderAtEnd(builder, entry);

        build(funCtx, stmt.body);

        if (LLVMGetReturnType(funtype).equals(LLVMVoidType()))
            LLVMBuildRetVoid(builder);

        if (LLVMVerifyFunction(fun, LLVMPrintMessageAction) != 0) {
            LLVMDisposeErrorMessage(error);
            return ErrorUtil.error("verification of function failed");
        }

        LLVMDumpValue(fun);

        return null;
    }

    public static LLVMValueRef build(CSawContext ctx, IfStmt stmt) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, IncStmt stmt) {
        final var file = new File(ctx.getFile().getParentFile(), stmt.path);
        final var incCtx = new CSawContext(file);

        Parser.parse(ErrorUtil.tryCatch(() -> new FileInputStream(file)), incCtx);

        return null;
    }

    public static LLVMValueRef build(CSawContext ctx, RetStmt stmt) {
        if (stmt.value == null)
            return LLVMBuildRetVoid(builder);
        return LLVMBuildRet(builder, build(ctx, stmt.value));
    }

    public static LLVMValueRef build(CSawContext ctx, ThingStmt stmt) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, VarStmt stmt) {

        final var type = getType(stmt.type);
        final var value = build(ctx, stmt.value);

        if (!LLVMTypeOf(value).equals(type))
            return ErrorUtil.error("wrong type assignment");

        ctx.createValue(stmt.name, value);

        return null;
    }

    public static LLVMValueRef build(CSawContext ctx, WhileStmt stmt) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, Expr expr) {
        if (expr instanceof AssignExpr)
            return build(ctx, (AssignExpr) expr);
        if (expr instanceof BinExpr)
            return build(ctx, (BinExpr) expr);
        if (expr instanceof CallExpr)
            return build(ctx, (CallExpr) expr);
        if (expr instanceof ChrExpr)
            return build(ctx, (ChrExpr) expr);
        if (expr instanceof ConExpr)
            return build(ctx, (ConExpr) expr);
        if (expr instanceof IdExpr)
            return build(ctx, (IdExpr) expr);
        if (expr instanceof MemExpr)
            return build(ctx, (MemExpr) expr);
        if (expr instanceof NumExpr)
            return build(ctx, (NumExpr) expr);
        if (expr instanceof StrExpr)
            return build(ctx, (StrExpr) expr);
        if (expr instanceof UnExpr)
            return build(ctx, (UnExpr) expr);

        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, AssignExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, BinExpr expr) {
        final var left = build(ctx, expr.left);
        final var right = build(ctx, expr.right);

        switch (expr.operator) {
            case "+":
                return LLVMBuildFAdd(builder, left, right, "add");
            case "-":
                return LLVMBuildFSub(builder, left, right, "subtract");
            case "*":
                return LLVMBuildFMul(builder, left, right, "multiply");
            case "/":
                return LLVMBuildFDiv(builder, left, right, "divide");
        }

        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, CallExpr expr) {
        String function = null;
        LLVMValueRef member = null;
        if (expr.function instanceof IdExpr) {
            function = ((IdExpr) expr.function).name;
        } else if (expr.function instanceof MemExpr) {
            function = ((MemExpr) expr.function).member;
            member = build(ctx, ((MemExpr) expr.function).object);
        }

        if (function == null)
            return ErrorUtil.error("invalid function call: function == null");

        final var c = expr.arguments.length + (member == null ? 0 : 1);
        final var args = new PointerPointer<>(c);
        final var types = new LLVMTypeRef[c];
        for (int i = 0; i < c; i++) {
            final var v = build(ctx, expr.arguments[i]);
            args.put(i, v);
            types[i] = LLVMTypeOf(v);
        }

        final var func = CSawContext.getFunction(function, types);

        final var fn = LLVMGetNamedFunction(module, func.first.toString());
        final var ty = func.second;

        LLVMDumpValue(fn);
        LLVMDumpType(ty);

        return LLVMBuildCall2(builder, ty, fn, args, c, "call");
    }

    public static LLVMValueRef build(CSawContext ctx, ChrExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, ConExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, IdExpr expr) {
        return ctx.getValue(expr.name);
    }

    public static LLVMValueRef build(CSawContext ctx, MemExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, NumExpr expr) {
        return LLVMConstReal(LLVMDoubleType(), expr.value);
    }

    public static LLVMValueRef build(CSawContext ctx, StrExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }

    public static LLVMValueRef build(CSawContext ctx, UnExpr expr) {
        return ErrorUtil.error("not yet implemented");
    }
}
