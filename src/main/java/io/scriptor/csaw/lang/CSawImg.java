package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.handle;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.scriptor.csaw.impl.Type;
import io.scriptor.csaw.impl.interpreter.value.ConstNum;
import io.scriptor.csaw.impl.interpreter.value.ConstStr;
import io.scriptor.csaw.impl.interpreter.value.Value;
import io.scriptor.java.CSawNative;

@CSawNative("img")
public class CSawImg extends Value {

    private final BufferedImage mData;

    public CSawImg(ConstStr filepath) {
        mData = handle(() -> ImageIO.read(new File(filepath.get())));
    }

    public CSawImg(ConstNum width, ConstNum height) {
        mData = new BufferedImage(width.getInt(), height.getInt(), BufferedImage.TYPE_INT_ARGB);
    }

    private boolean noData() {
        return mData == null;
    }

    public ConstNum getWidth() {
        return new ConstNum(noData() ? 0 : mData.getWidth());
    }

    public ConstNum getHeight() {
        return new ConstNum(noData() ? 0 : mData.getHeight());
    }

    private int getRGB(ConstNum x, ConstNum y) {
        return mData.getRGB(x.getInt(), y.getInt());
    }

    public ConstNum get(ConstNum x, ConstNum y) {
        return new ConstNum(getRGB(x, y));
    }

    public ConstNum r(ConstNum x, ConstNum y) {
        return new ConstNum((getRGB(x, y) >> 16) & 0xff);
    }

    public ConstNum g(ConstNum x, ConstNum y) {
        return new ConstNum((getRGB(x, y) >> 8) & 0xff);
    }

    public ConstNum b(ConstNum x, ConstNum y) {
        return new ConstNum(getRGB(x, y) & 0xff);
    }

    public void set(ConstNum x, ConstNum y, ConstNum rgb) {
        mData.setRGB(x.getInt(), y.getInt(), rgb.getInt());
    }

    public ConstNum write(ConstStr format, ConstStr file) {
        return new ConstNum(handle(() -> ImageIO.write(mData, format.get(), new File(file.get()))));
    }

    @Override
    protected Type type() {
        return Type.get("img");
    }

    @Override
    protected Object object() {
        return mData;
    }

    @Override
    protected String string() {
        return mData.toString();
    }
}
