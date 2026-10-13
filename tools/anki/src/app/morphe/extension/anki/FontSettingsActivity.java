// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.anki;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.*;
import android.view.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public final class FontSettingsActivity extends Activity {
    private static final int IMPORT = 7301;
    private LinearLayout content;
    @Override protected void attachBaseContext(Context c) { super.attachBaseContext(AnkiTheme.scaled(c)); }
    @Override public void onCreate(Bundle state) {
        int theme = getResources().getIdentifier(AnkiTheme.dark ? "Theme_Dark" : "Theme_Light", "style", getPackageName());
        setTheme(theme != 0 ? theme : (AnkiTheme.dark ? android.R.style.Theme_Material_NoActionBar : android.R.style.Theme_Material_Light_NoActionBar));
        super.onCreate(state); refresh();
    }
    private void refresh() {
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(24 * getResources().getDisplayMetrics().density); content.setPadding(padding,padding,padding,padding);
        scroll.setBackgroundColor(AnkiTheme.dark ? 0xff24273a : 0xffeff1f5);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.addView(content); setContentView(scroll);
        TextView title = new TextView(this); title.setText("Font and text size"); title.setTextSize(24); content.addView(title);
        TextView description = new TextView(this);
        description.setText("Selected fonts override card template fonts and apply throughout AnkiDroid. App default restores original fonts."); content.addView(description);
        Button font = button("Font: "+fontLabel());
        font.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Font").setSingleChoiceItems(
            new String[]{"App default", "JetBrains Mono", "Custom TTF"},
            Arrays.asList("default","jetbrains","custom").indexOf(AnkiTheme.selection(this)),
            (dialog,index) -> {
                dialog.dismiss();
                String selected = new String[]{"default","jetbrains","custom"}[index];
                if (index == 2 && !AnkiTheme.custom(this).isFile()) { choose(); return; }
                AnkiTheme.prefs(this).edit().putString(AnkiTheme.FONT, selected).apply(); recreate();
            }).show());
        Button choose = button("Choose TTF file"); choose.setOnClickListener(v -> choose());
        int percent = AnkiTheme.prefs(this).getInt(AnkiTheme.SIZE,100);
        Button size = button("Font size: "+(percent==100 ? "Default (100%)" : percent+"%"));
        size.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Font size").setSingleChoiceItems(
            new String[]{"Very small (70%)","Smaller (80%)","Small (90%)","Default (100%)","Larger (110%)","Large (120%)","Very large (130%)","Largest (150%)"},
            Arrays.asList(70,80,90,100,110,120,130,150).indexOf(percent),
            (dialog,index) -> {dialog.dismiss();AnkiTheme.prefs(this).edit().putInt(AnkiTheme.SIZE,new int[]{70,80,90,100,110,120,130,150}[index]).apply();recreate();}).show());
        Button back = button("Done"); back.setOnClickListener(v -> finish());
        Typeface face = AnkiTheme.selectedFont(this);
        for (int i=0;i<content.getChildCount();i++) if(content.getChildAt(i) instanceof TextView) {
            TextView text = (TextView)content.getChildAt(i);
            text.setTextColor(text instanceof Button ? (AnkiTheme.dark ? 0xff24273a : 0xffeff1f5) : (AnkiTheme.dark ? 0xffcad3f5 : 0xff4c4f69));
            if(face!=null) text.setTypeface(face);
        }
    }
    private Button button(String label) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(AnkiTheme.dark ? 0xffc6a0f6 : 0xff8839ef));
        content.addView(b);return b;
    }
    private String fontLabel() {
        String value = AnkiTheme.selection(this);
        return value.equals("jetbrains") ? "JetBrains Mono" : value.equals("custom") ? AnkiTheme.prefs(this).getString("catppuccin_anki_font_name","Custom TTF") : "App default";
    }
    private void choose() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, IMPORT);
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=IMPORT||result!=RESULT_OK||data==null||data.getData()==null) return;
        Uri uri=data.getData();
        new Thread(() -> {
            File temporary = null;
            try {
                String name = "custom.ttf";
                try(Cursor cursor=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
                    if(cursor!=null && cursor.moveToFirst()) name=cursor.getString(0);
                }
                if(name==null||!name.toLowerCase(Locale.ROOT).endsWith(".ttf")) throw new IOException("Choose a .ttf font file");
                byte[] bytes=AnkiTheme.read(getContentResolver().openInputStream(uri)); validate(bytes);
                temporary=File.createTempFile("catppuccin-font-",".ttf",getFilesDir());
                try(FileOutputStream out=new FileOutputStream(temporary)){out.write(bytes);}
                Typeface.createFromFile(temporary);
                if(!temporary.renameTo(AnkiTheme.custom(this))) throw new IOException("Cannot save font");
                AnkiTheme.prefs(this).edit().putString(AnkiTheme.FONT,"custom").putString("catppuccin_anki_font_name",name).apply();
                runOnUiThread(() -> {if(!isFinishing()) recreate();});
            } catch(Exception e) {runOnUiThread(() -> {if(!isFinishing()) new AlertDialog.Builder(this).setTitle("Cannot import font").setMessage(e.getMessage()).setPositiveButton("OK",null).show();});}
            finally {if(temporary!=null) temporary.delete();}
        },"Anki-font-import").start();
    }
    static void validate(byte[] bytes) throws IOException {
        if(bytes.length<12)throw new IOException("Invalid TTF");
        ByteBuffer buffer=ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        if(buffer.getInt()!=0x00010000)throw new IOException("Choose a TrueType .ttf font");
        int count=buffer.getShort()&65535;
        if(count<1||count>256||12+count*16>bytes.length)throw new IOException("Invalid TTF table directory");
        Set<String> tables=new HashSet<>();
        for(int i=0;i<count;i++){
            buffer.position(12+i*16); byte[] tag=new byte[4];buffer.get(tag);tables.add(new String(tag,java.nio.charset.StandardCharsets.US_ASCII));buffer.getInt();
            long offset=buffer.getInt()&0xffffffffL,length=buffer.getInt()&0xffffffffL;
            if(offset<12+count*16||offset+length>bytes.length)throw new IOException("Invalid TTF table bounds");
        }
        if(!tables.containsAll(Arrays.asList("cmap","head","hhea","hmtx","maxp","name","glyf","loca")))throw new IOException("Incomplete TrueType font");
    }
}
