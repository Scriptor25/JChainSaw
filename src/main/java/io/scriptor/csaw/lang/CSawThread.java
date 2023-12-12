package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handleVoid;

import io.scriptor.csaw.impl.interpreter.value.LambdaValue;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("thrd")
public class CSawThread extends Value {

    private final Thread mThread;

    public CSawThread(LambdaValue function) {
        mThread = new Thread(() -> function.invoke(), "CSawThread");
    }

    public void start() {
        mThread.start();
    }

    public void join() {
        handleVoid(mThread::join);
    }

    public void stop() {
        mThread.interrupt();
    }

    @Override
    protected Thread value() {
        return mThread;
    }

    @Override
    protected String type() {
        return "thrd";
    }

    @Override
    protected boolean bool() {
        return mThread.isAlive();
    }

    @Override
    protected String string() {
        return mThread.toString();
    }

}
