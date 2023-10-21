package io.scriptor.value;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileValue extends Value {

    private final String mPath;
    private final BufferedReader mReader;
    private final BufferedWriter mWriter;

    public FileValue(String path) throws IOException {
        mPath = path;
        mReader = new BufferedReader(new FileReader(path));
        mWriter = new BufferedWriter(new FileWriter(path, false));
    }

    public void out(String value) throws IOException {
        mWriter.append(value);
    }

    public String in() throws IOException {
        return mReader.readLine();
    }

    public void close() throws IOException {
        mReader.close();
        mWriter.close();
    }

    @Override
    public Object getValue() {
        return mPath;
    }

    @Override
    public String getType() {
        return "file";
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return mPath;
    }

}
