import app.morphe.extension.instagram.InstagramColors;
public final class ComposePaletteChecks {
    private static long color(int argb) { return ((long) argb) << 32; }
    private static void equal(long actual, long expected) {
        if (actual != expected) throw new AssertionError(Long.toHexString(actual) + " != " + Long.toHexString(expected));
    }
    public static void main(String[] args) {
        equal(InstagramColors.dark(color(0xff0c1014)), color(0xff24273a));
        equal(InstagramColors.light(color(0xff0c1014)), color(0xff4c4f69));
        equal(InstagramColors.dark(color(0x80ffffff)), color(0x80cad3f5));
        equal(InstagramColors.light(color(0xffffffff)), color(0xffeff1f5));
        equal(InstagramColors.dark(color(0xff4a5df9)), color(0xffc6a0f6));
        equal(InstagramColors.light(color(0xff4a5df9)), color(0xff8839ef));
        equal(InstagramColors.dark(color(0xffe12345)), color(0xffe12345));
        equal(InstagramColors.dark(color(0x000c1014)), color(0x0024273a));
        System.out.println("Compose light/dark colors, mauve accents, alpha and unmapped colors passed");
    }
}
