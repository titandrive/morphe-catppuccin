package app.morphe.extension.fastmail;

import android.net.Uri;
import android.webkit.WebView;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Styles only the trusted top-level Fastmail UI; never inspects message documents. */
public final class FastmailTheme {
    private FastmailTheme() {}
    public static void apply(WebView view, String url) {
        if (view == null || url == null) return;
        Uri uri = Uri.parse(url);
        if (!"https".equals(uri.getScheme()) || !"app.fastmail.com".equals(uri.getHost())) return;
        if (uri.getPort() != -1 && uri.getPort() != 443) return;
        if (!url.equals(view.getUrl())) return;
        applyColors(view);
        applyFont(view);
    }
    private static byte[] asset(WebView view, String path) throws java.io.IOException {
        try (InputStream in = view.getContext().getAssets().open(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toByteArray();
        }
    }
    private static String styleScript(String id, String css) {
        return "(()=>{if(window!==window.top||location.origin!=='https://app.fastmail.com')return;"
            + "let s=document.getElementById(" + JSONObject.quote(id) + ");"
            + "if(!s){s=document.createElement('style');s.id=" + JSONObject.quote(id) + ";document.head.appendChild(s);}"
            + "s.textContent=" + JSONObject.quote(css) + ";})()";
    }
    private static void applyColors(WebView view) {
        try {
            String css = new String(asset(view, "catppuccin/fastmail/macchiato.css"), "UTF-8");
            view.evaluateJavascript(styleScript("morphe-catppuccin", css), null);
        } catch (java.io.FileNotFoundException absent) {
            // Font-only patch: no color stylesheet is embedded.
        } catch (Exception error) {
            android.util.Log.w("CatppuccinFastmail", "Could not load theme stylesheet", error);
        }
    }
    private static volatile String encodedFont;
    private static void applyFont(WebView view) {
        try {
            String data = encodedFont;
            if (data == null) encodedFont = data = android.util.Base64.encodeToString(
                asset(view, "catppuccin/fastmail/custom.ttf"), android.util.Base64.NO_WRAP);
            String css = "body, .u-font-proxima, .u-font-system, button, input, select, textarea {"
                + "font-family: 'MorpheCustom', system-ui, sans-serif !important;}";
            // Binary FontFace avoids remote font requests and data-URL CSP restrictions.
            // Do not edit compose HTML or inject styles into received-message iframes.
            String settings = new String(asset(view, "catppuccin/fastmail/font-settings.js"), "UTF-8");
            String script = "(()=>{if(window!==window.top||location.origin!=='https://app.fastmail.com'||window.__morpheFont)return;"
                + "window.__morpheFont=true;const b=Uint8Array.from(atob(" + JSONObject.quote(data) + "),c=>c.charCodeAt(0));"
                + "new FontFace('MorpheCustom',b.buffer).load().then(f=>{document.fonts.add(f);"
                + styleScript("morphe-custom-font", css) + ";" + settings
                + "}).catch(()=>{window.__morpheFont=false;});})()";
            view.evaluateJavascript(script, null);
        } catch (java.io.FileNotFoundException absent) {
            // Theme-only patch: retain the stock font.
        } catch (Exception error) {
            android.util.Log.w("CatppuccinFastmail", "Could not load custom font", error);
        }
    }
}
