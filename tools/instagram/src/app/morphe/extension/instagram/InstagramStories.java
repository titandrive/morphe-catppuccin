// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.instagram;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public final class InstagramStories {
    public static void prepare(final View root) {
        final int color = background(root.getContext());
        root.setBackgroundColor(color);
        root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                root.getViewTreeObserver().removeOnPreDrawListener(this);
                replaceLoadingBackgrounds(root, color);
                return true;
            }
        });
    }
    private static void replaceLoadingBackgrounds(View view, int color) {
        if (view.getBackground() instanceof ColorDrawable &&
                ((ColorDrawable) view.getBackground()).getColor() == 0xff000000) view.setBackgroundColor(color);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) replaceLoadingBackgrounds(group.getChildAt(i), color);
        }
    }
    public static int background(Context context) {
        Resources resources = context.getResources();
        int attribute = resources.getIdentifier("igds_color_primary_background", "attr", context.getPackageName());
        TypedValue value = new TypedValue();
        if (attribute != 0 && context.getTheme().resolveAttribute(attribute, value, true)) {
            if (value.resourceId != 0) return resources.getColor(value.resourceId, context.getTheme());
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
                return value.data;
        }
        return (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            ? 0xff24273a : 0xffeff1f5;
    }
}
