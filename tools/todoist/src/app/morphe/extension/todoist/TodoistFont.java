package app.morphe.extension.todoist;

import android.app.Application;
import android.graphics.Typeface;

/** Cached custom family used by Todoist and Compose's system font resolver. */
public final class TodoistFont {
    private static volatile Typeface font;
    public static void install(Application application) {
        font = Typeface.createFromAsset(application.getAssets(), "catppuccin/todoist-custom.ttf");
    }
    public static Typeface create(String family, int style) {
        Typeface selected = font;
        return selected == null ? Typeface.create(family, style) : Typeface.create(selected, style);
    }
    public static Typeface create(Typeface family, int style) {
        Typeface selected = font;
        return Typeface.create(selected == null ? family : selected, style);
    }
    public static Typeface create(Typeface family, int weight, boolean italic) {
        Typeface selected = font;
        return Typeface.create(selected == null ? family : selected, weight, italic);
    }
    public static Typeface defaultFont() { return font == null ? Typeface.DEFAULT : font; }
    public static Typeface defaultBoldFont() { return create(defaultFont(), Typeface.BOLD); }
}
