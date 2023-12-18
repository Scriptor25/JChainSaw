package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handleVoid;

import io.scriptor.csaw.impl.interpreter.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstLambda;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("thrd")
public class CSawThread extends Value {

    private final Thread mThread;

    public CSawThread() {
        mThread = null;
    }

    public CSawThread(ConstLambda function) {
        mThread = new Thread(() -> function.invoke(), "CSawThread");
    }

    public CSawThread start() {
        mThread.start();
        return this;
    }

    public void join() {
        handleVoid(mThread::join);
    }

    public void stop() {
        mThread.interrupt();
    }

    @Override
    public ConstNum asNum() {
        return new ConstNum(mThread != null);
    }

    public ConstNum isAlive() {
        return new ConstNum(mThread.isAlive());
    }

    public ConstNum isInterrupted() {
        return new ConstNum(mThread.isInterrupted());
    }

    @Override
    protected Type type() {
        return Type.get("thrd");
    }

    @Override
    protected Object object() {
        return mThread;
    }

    @Override
    protected String string() {
        return mThread.toString();
    }

}
