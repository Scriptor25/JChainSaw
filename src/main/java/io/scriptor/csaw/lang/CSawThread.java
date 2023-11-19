package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handleVoid;

import io.scriptor.csaw.impl.interpreter.Environment;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.java.CSawNative;

@CSawNative("thrd")
public class CSawThread {

    private final Thread mThread;

    public CSawThread(StrValue function) {
        mThread = new Thread(() -> Environment.getAndInvoke(null, function.getValue()), "CSawThread");
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

}
