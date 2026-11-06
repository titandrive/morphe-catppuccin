// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.anki;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.*;
import android.content.res.Configuration;
import android.widget.RemoteViews;

/** RemoteViews must be themed in their provider process before the launcher receives them. */
public final class WidgetTheme {
    static final String OPACITY="catppuccin_widget_opacity";
    private static Context app;
    private static final android.content.SharedPreferences.OnSharedPreferenceChangeListener CHANGE=(prefs,key)-> {
        if(OPACITY.equals(key) || "appTheme".equals(key) || "dayTheme".equals(key) || "nightTheme".equals(key)) refreshAll();
    };
    private WidgetTheme() {}
    static void install(Application application) {
        app=application;
        AnkiTheme.prefs(app).registerOnSharedPreferenceChangeListener(CHANGE);
        application.registerComponentCallbacks(new ComponentCallbacks() {
            public void onConfigurationChanged(Configuration config) { refreshAll(); }
            public void onLowMemory() {}
        });
    }
    static boolean isDark(Context context) {
        String theme=AnkiTheme.prefs(context).getString("appTheme","0");
        return "2".equals(theme) || (!"1".equals(theme) &&
            (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES);
    }
    static int opacity(Context context) {
        return Math.max(0,Math.min(100,AnkiTheme.prefs(context).getInt(OPACITY,100)));
    }
    private static int id(String name) { return app.getResources().getIdentifier(name,"id",app.getPackageName()); }
    private static void text(RemoteViews views,String name,int color) {
        int id=id(name);if(id!=0)views.setTextColor(id,color);
    }
    private static void clear(RemoteViews views,String name) {
        int id=id(name);if(id!=0)views.setInt(id,"setBackgroundResource",0);
    }
    private static void background(RemoteViews views,String name,boolean dark) {
        int id=id(name);
        int percent=Math.round(opacity(app)/10f)*10;
        int drawable=app.getResources().getIdentifier("catppuccin_widget_"+(dark?"macchiato_":"latte_")+percent,"drawable",app.getPackageName());
        if(id!=0 && drawable!=0)views.setInt(id,"setBackgroundResource",drawable);
    }
    public static RemoteViews theme(RemoteViews views) {
        if(app==null || views==null)return views;
        final String layout;
        try { layout=app.getResources().getResourceEntryName(views.getLayoutId()); }
        catch(android.content.res.Resources.NotFoundException e) { return views; }
        boolean dark=isDark(app);
        int text=dark?0xffcad3f5:0xff4c4f69, blue=dark?0xff8aadf4:0xff1e66f5;
        int red=dark?0xffed8796:0xffd20f39,green=dark?0xffa6da95:0xff40a02b;
        if(layout.equals("widget_card_analysis")) {
            background(views,"widget_deck_picker",dark);clear(views,"cardAnalysisDataHolder");
            text(views,"deckNameCardAnalysis",text);text(views,"empty_widget",text);
            text(views,"deckNew_card_analysis_widget",blue);text(views,"deckLearn_card_analysis_widget",red);text(views,"deckDue_card_analysis_widget",green);
        } else if(layout.equals("widget_deck_picker_large")) {
            background(views,"widget_deck_picker",dark);clear(views,"deckCollection");text(views,"empty_widget",text);
        } else if(layout.equals("widget_item_deck_main")) {
            text(views,"deckName",text);text(views,"deckNew",blue);text(views,"deckLearn",red);text(views,"deckDue",green);
        } else if(layout.equals("widget_small") || layout.equals("widget_small_unthemed")) {
            background(views,"ankidroid_widget_small_layout",dark);
            text(views,"widget_due",blue);text(views,"widget_eta",text);clear(views,"widget_due");clear(views,"widget_eta");
            int button=id("ankidroid_widget_small_button");if(button!=0)views.setInt(button,"setColorFilter",blue);
            int finish=id("ankidroid_widget_small_finish_layout");if(finish!=0)views.setInt(finish,"setColorFilter",green);
        } else if(layout.equals("widget_add_note")) {
            background(views,"widget_add_note_root",dark);
            int button=id("widget_add_note_button");if(button!=0)views.setInt(button,"setColorFilter",dark?0xffc6a0f6:0xff8839ef);
        }
        return views;
    }
    public static void addView(RemoteViews parent,int id,RemoteViews child) { parent.addView(id,theme(child)); }
    public static void addStableView(RemoteViews parent,int id,RemoteViews child,int stableId) { parent.addStableView(id,theme(child),stableId); }
    public static void updateAppWidget(AppWidgetManager manager,int id,RemoteViews views) { manager.updateAppWidget(id,theme(views)); }
    public static void updateAppWidget(AppWidgetManager manager,int[] ids,RemoteViews views) { manager.updateAppWidget(ids,theme(views)); }
    public static void updateAppWidget(AppWidgetManager manager,ComponentName component,RemoteViews views) { manager.updateAppWidget(component,theme(views)); }
    public static void partiallyUpdateAppWidget(AppWidgetManager manager,int id,RemoteViews views) { manager.partiallyUpdateAppWidget(id,theme(views)); }
    public static void partiallyUpdateAppWidget(AppWidgetManager manager,int[] ids,RemoteViews views) { manager.partiallyUpdateAppWidget(ids,theme(views)); }
    static void refreshAll() {
        if(app==null)return;
        AppWidgetManager manager=AppWidgetManager.getInstance(app);
        for(AppWidgetProviderInfo provider:manager.getInstalledProviders()) {
            if(!provider.provider.getPackageName().equals(app.getPackageName()))continue;
            int[] ids=manager.getAppWidgetIds(provider.provider);if(ids.length==0)continue;
            app.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setComponent(provider.provider)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids));
        }
    }
}
