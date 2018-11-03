package gen;

public final class GenUtils {
    public static int byteAlign(int size) {
        return (size + 3) / 4 * 4;
    }
}
