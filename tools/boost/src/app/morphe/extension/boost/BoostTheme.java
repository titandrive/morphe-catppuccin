// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.boost;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/** Helpers for the supported native 1.14.2 theme registry. No retained Activity. */
public final class BoostTheme {
    private BoostTheme() {}

    // Replaced by the patch with the verified native preference getter.
    public static int mode() { return 0; }

    private static int color(int original, int latte, int macchiato) {
        int mode = mode();
        return mode == 17 ? latte : mode == 18 ? macchiato : original;
    }
    public static int accent(int original) { return color(original, 0xff8839ef, 0xffc6a0f6); }
    public static int text(int original) { return color(original, 0xff4c4f69, 0xffcad3f5); }
    public static int title(int original) { return color(original, 0xff4c4f69, 0xff8aadf4); }
    public static int secondary(int original) { return color(original, 0xff6c6f85, 0xffa5adcb); }
    public static int header(int original) { return color(original, 0xffccd0da, 0xff1e2030); }
    public static int sticky(int original) { return color(original, 0xff40a02b, 0xffa6da95); }
    public static int surface(int original) { return color(original, 0xffe6e9ef, 0xff24273a); }
    public static boolean material(boolean original) { int m=mode(); return m==17 || m==18 || original; }
    public static boolean dynamic(boolean original) { int m=mode(); return m==17 || m==18 ? false : original; }
    public static boolean amoled(boolean original) { return dynamic(original); }
    public static int toolbarMode(int original) { return color(original, 100, 200); }
    public static int themeIndex(int original, int requested) { return requested==17 || requested==18 ? requested : original; }

    public static void tintMenu(Menu menu) {
        if (menu == null || (mode()!=17 && mode()!=18)) return;
        for (int i=0; i<menu.size(); i++) {
            MenuItem item=menu.getItem(i);
            Drawable icon=item.getIcon();
            if (icon!=null) { icon=icon.mutate(); icon.setTint(accent(0)); item.setIcon(icon); }
        }
    }

    public static void media(Activity activity) {
        if (mode()!=17 && mode()!=18) return;
        activity.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(surface(0)));
        activity.getWindow().setStatusBarColor(header(0));
        activity.getWindow().setNavigationBarColor(header(0));
        View root=activity.findViewById(android.R.id.content);
        if (root!=null) root.setBackgroundColor(surface(0));
    }
}
