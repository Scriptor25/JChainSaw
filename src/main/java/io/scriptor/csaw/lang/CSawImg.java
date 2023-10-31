package io.scriptor.csaw.lang;

import static io.scriptor.java.ErrorUtil.tryCatch;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.scriptor.csaw.impl.interpreter.value.NumValue;
import io.scriptor.csaw.impl.interpreter.value.StrValue;
import io.scriptor.java.CSawNative;

@CSawNative("img")
public class CSawImg {

    private final BufferedImage mData;

    public CSawImg(StrValue filepath) {
        mData = tryCatch(() -> ImageIO.read(new File(filepath.getValue())));
    }

    private boolean noData() {
        return mData == null;
    }

    public NumValue getWidth() {
        return new NumValue(noData() ? 0 : mData.getWidth());
    }

    public NumValue getHeight() {
        return new NumValue(noData() ? 0 : mData.getHeight());
    }

    private int getRGB(NumValue x, NumValue y) {
        return mData.getRGB((int) (double) x.getValue(), (int) (double) y.getValue());
    }

    public NumValue getPixel(NumValue x, NumValue y) {
        return new NumValue(getRGB(x, y));
    }

    public NumValue getRed(NumValue x, NumValue y) {
        return new NumValue((getRGB(x, y) >> 16) & 0xff);
    }

    public NumValue getGreen(NumValue x, NumValue y) {
        return new NumValue((getRGB(x, y) >> 8) & 0xff);
    }

    public NumValue getBlue(NumValue x, NumValue y) {
        return new NumValue(getRGB(x, y) & 0xff);
    }
}
