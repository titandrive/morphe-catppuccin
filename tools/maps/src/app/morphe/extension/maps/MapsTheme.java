package app.morphe.extension.maps;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Retains Maps' shapes while correcting backgrounds supplied after inflation. */
public final class MapsTheme {
    private static final int BASE = 0xff24273a, MANTLE = 0xff1e2030;
    private static final int SURFACE = 0xff363a4f, TEXT = 0xffcad3f5, SUBTEXT = 0xffb8c0e0;
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
        private final int search, input, content, stream, title, footer, textColor;
        Refresh(View view) {
            root = new WeakReference<>(view);
            search = id(view, "mod_search_omnibox_layout");
            input = id(view, "search_omnibox_text_box");
            content = id(view, "content_container");
            stream = id(view, "scrollable_card_stream_container");
            title = id(view, "explore_tab_home_title_card");
            footer = id(view, "bottom_nav");
            textColor = view.getResources().getIdentifier("geo_sys_color_on_surface", "color", view.getContext().getPackageName());
        }
        @Override public void onGlobalLayout() { run(); }
        @Override public void run() {
            View view = root.get();
            if (view == null || textColor == 0 || view.getResources().getColor(textColor, view.getContext().getTheme()) != TEXT) return;
            tint(find(view, search), SURFACE);
            labels(find(view, input));
            tint(find(view, content), BASE);
            tint(find(view, stream), BASE);
            tint(find(view, title), BASE);
            panels(find(view, content));
            tint(find(view, footer), MANTLE);
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
    private static void panels(View view) {
        if (view == null) return;
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            if (color == 0xff131314 || color == 0xff121212 || color == 0xff000000 || color == 0xff1f1f1f)
                ((ColorDrawable) background).setColor(BASE);
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
