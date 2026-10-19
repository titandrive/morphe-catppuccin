// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.instagram;

/** Maps only colors read while constructing Instagram's semantic Compose palettes. */
public final class InstagramColors {
    private static final int[] NEUTRALS = {0x000000, 0x0c1014, 0x101473, 0x191c1f, 0x1a1a1a, 0x1c1d22, 0x1d2023, 0x1e1e1e, 0x212328, 0x233038, 0x25292e, 0x25292f, 0x262626, 0x292e33, 0x2b3036, 0x32353e, 0x363636, 0x48505b, 0x555555, 0x57606b, 0x5e646d, 0x686f78, 0x6a717a, 0x6f7680, 0x737373, 0x838993, 0x838a93, 0xa2a9b4, 0xa2aab4, 0xa8a8a8, 0xbcc0c4, 0xc7c7c7, 0xdbdbdb, 0xdbdfe4, 0xdce0e5, 0xe7eaee, 0xe9edf0, 0xebf7fe, 0xefefef, 0xf3f5f7, 0xf5f5f5, 0xf8f9f9, 0xffffff};
    private static final int[] INDIGO = {0x232e67, 0x2e429e, 0x3143e3, 0x3b52e7, 0x4150f7, 0x4a5df9, 0x5465fe, 0x576aff, 0x5c77ff, 0x708dff, 0x7595ff, 0x85a1ff, 0xa1bbff, 0xccddff, 0xdbdfff, 0xebf1ff, 0xeff1ff};
    private static final int[] BLUE = {0x0095f6, 0x016dcb, 0x023e8d, 0x0252a1, 0x063d75, 0x0855a4, 0x0b6ed3, 0x33afff, 0x47afff, 0x5cbfff, 0x70bcff, 0xaddfff, 0xb2dbff, 0xdbf1ff, 0xe0f1ff, 0xebf7ff, 0xf5fbff};
    private static boolean contains(int[] values, int value) {
        for (int candidate : values) if (candidate == value) return true;
        return false;
    }
    public static long dark(long color) { return map(color, true); }
    public static long light(long color) { return map(color, false); }
    private static long map(long color, boolean dark) {
        int argb = (int) (color >>> 32);
        int rgb = argb & 0xffffff;
        int mapped;
        if (contains(INDIGO, rgb)) mapped = dark ? 0xc6a0f6 : 0x8839ef;
        else if (contains(BLUE, rgb)) mapped = dark ? 0x8aadf4 : 0x1e66f5;
        else if (contains(NEUTRALS, rgb)) {
            int level = ((rgb >> 16) + ((rgb >> 8) & 255) + (rgb & 255)) / 3;
            if (dark) {
                mapped = level > 245 ? 0xcad3f5 : level > 230 ? 0xb8c0e0 :
                    level > 210 ? 0xa5adcb : level > 180 ? 0x939ab7 : level > 150 ? 0x8087a2 :
                    level > 110 ? 0x6e738d : level > 80 ? 0x5b6078 : level > 60 ? 0x494d64 :
                    level > 35 ? 0x363a4f : level > 20 ? 0x1e2030 : 0x24273a;
            } else {
                mapped = level > 245 ? 0xeff1f5 : level > 235 ? 0xe6e9ef : level > 220 ? 0xdce0e8 :
                    level > 200 ? 0xccd0da : level > 180 ? 0xbcc0cc : level > 160 ? 0xacb0be :
                    level > 140 ? 0x9ca0b0 : level > 120 ? 0x8c8fa1 : level > 100 ? 0x7c7f93 :
                    level > 80 ? 0x6c6f85 : level > 60 ? 0x5c5f77 : 0x4c4f69;
            }
        } else return color;
        return ((long) ((argb & 0xff000000) | mapped) << 32) | (color & 0xffffffffL);
    }
}
