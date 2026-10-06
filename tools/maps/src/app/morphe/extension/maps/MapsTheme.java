package app.morphe.extension.maps;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.opengl.GLES20;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Retains Maps' shapes while correcting backgrounds supplied after inflation. */
public final class MapsTheme {
    private static final int BASE = 0xff24273a, MANTLE = 0xff1e2030;
    private static final int SURFACE = 0xff363a4f, TEXT = 0xffcad3f5, SUBTEXT = 0xffb8c0e0;
    private static final int MAUVE = 0xffc6a0f6;
    private static final ColorStateList TAB_TEXT = new ColorStateList(
        new int[][] { new int[] { android.R.attr.state_selected }, new int[0] },
        new int[] { MAUVE, SUBTEXT });
    private static final ColorStateList TAB_ICON = new ColorStateList(
        new int[][] { new int[] { android.R.attr.state_selected }, new int[0] },
        new int[] { BASE, SUBTEXT });
    private static volatile boolean dark;
    private static final WeakHashMap<View, Boolean> attached = new WeakHashMap<>();

    public static void attach(Activity activity) {
        View root = activity.getWindow().getDecorView();
        if (attached.containsKey(root)) return;
        attached.put(root, Boolean.TRUE);
        Refresh refresh = new Refresh(root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(refresh);
        root.post(refresh);
    }

    private static final class Refresh implements ViewTreeObserver.OnGlobalLayoutListener, Runnable {
        private final WeakReference<View> root;
        private final int search, input, content, stream, title, footer, appBar, indicator, icon, smallLabel, largeLabel, textColor;
        Refresh(View view) {
            root = new WeakReference<>(view);
            search = id(view, "mod_search_omnibox_layout");
            input = id(view, "search_omnibox_text_box");
            content = id(view, "content_container");
            stream = id(view, "scrollable_card_stream_container");
            title = id(view, "explore_tab_home_title_card");
            footer = id(view, "bottom_nav");
            appBar = id(view, "mod_app_bar");
            indicator = id(view, "navigation_bar_item_active_indicator_view");
            icon = id(view, "navigation_bar_item_icon_view");
            smallLabel = id(view, "navigation_bar_item_small_label_view");
            largeLabel = id(view, "navigation_bar_item_large_label_view");
            textColor = view.getResources().getIdentifier("geo_sys_color_on_surface", "color", view.getContext().getPackageName());
        }
        @Override public void onGlobalLayout() { run(); }
        @Override public void run() {
            View view = root.get();
            if (view == null || textColor == 0) return;
            dark = view.getResources().getColor(textColor, view.getContext().getTheme()) == TEXT;
            if (!dark) return;
            tint(find(view, search), SURFACE);
            labels(find(view, input));
            tint(find(view, content), BASE);
            tint(find(view, stream), BASE);
            tint(find(view, title), BASE);
            panels(view);
            tint(find(view, appBar), BASE);
            View navigation = find(view, footer);
            tint(navigation, MANTLE);
            tabs(navigation, indicator, icon, smallLabel, largeLabel);
        }
    }
    // Recolor the renderer's neutral empty canvas without changing map content.
    public static void clearMapColor(float r, float g, float b, float a) {
        if (dark && a > 0.99f && r >= 16f / 255f && r <= 19f / 255f &&
                Math.abs(g - r) < 0.001f && Math.abs(b - r) < 0.001f) {
            r = 36f / 255f; g = 39f / 255f; b = 58f / 255f;
        }
        GLES20.glClearColor(r, g, b, a);
    }
    private static void tabs(View view, int indicator, int icon, int smallLabel, int largeLabel) {
        if (view == null) return;
        int id = view.getId();
        if (id == indicator && indicator != 0) tint(view, MAUVE);
        if (id == icon && icon != 0 && view instanceof ImageView) {
            ImageView image = (ImageView) view;
            if (image.getImageTintList() != TAB_ICON) image.setImageTintList(TAB_ICON);
        }
        if ((id == smallLabel || id == largeLabel) && id != 0 && view instanceof TextView) {
            TextView label = (TextView) view;
            if (label.getTextColors() != TAB_TEXT) label.setTextColor(TAB_TEXT);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) tabs(group.getChildAt(i), indicator, icon, smallLabel, largeLabel);
        }
    }
    private static int id(View root, String name) {
        return root.getResources().getIdentifier(name, "id", root.getContext().getPackageName());
    }
    private static View find(View root, int id) { return id == 0 ? null : root.findViewById(id); }
    private static void tint(View view, int color) {
        if (view == null) return;
        // Avoid repeated invalidations and retain rounded corners and stateful drawables.
        Object previous = view.getTag(0x7e010001);
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            ColorDrawable fill = (ColorDrawable) background;
            if (fill.getColor() != color) fill.setColor(color);
            return;
        }
        if (background instanceof GradientDrawable) {
            GradientDrawable fill = (GradientDrawable) background;
            if (fill.getColor() == null || fill.getColor().getDefaultColor() != color) fill.setColor(color);
            return;
        }
        if (background != null && background == previous) return;
        if (background == null) view.setBackgroundColor(color);
        else background.mutate().setTint(color);
        view.setTag(0x7e010001, view.getBackground());
    }
    private static boolean pageNeutral(int color) {
        return color == 0xff131314 || color == 0xff121212 || color == 0xff111111 ||
            color == 0xff000000 || color == 0xff1f1f1f || color == 0xff202124;
    }
    private static void panels(View view) {
        if (view == null) return;
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            if (pageNeutral(color)) ((ColorDrawable) background).setColor(BASE);
        }
        if (background instanceof GradientDrawable) {
            GradientDrawable fill = (GradientDrawable) background;
            if (fill.getColor() != null && pageNeutral(fill.getColor().getDefaultColor())) fill.setColor(BASE);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) panels(group.getChildAt(i));
        }
    }
    private static void labels(View view) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            if (text.getCurrentTextColor() != TEXT) text.setTextColor(TEXT);
            if (text.getCurrentHintTextColor() != SUBTEXT) text.setHintTextColor(SUBTEXT);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) labels(group.getChildAt(i));
        }
    }
}
