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
        try (InputStream in = view.getContext().getAssets().open("catppuccin/fastmail/macchiato.css")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            String css = out.toString("UTF-8");
            // Idempotent on reload; root dark-mode selector follows Fastmail's own appearance setting.
            String script = "(()=>{if(window!==window.top||location.origin!=='https://app.fastmail.com')return;"
                + "let s=document.getElementById('morphe-catppuccin');"
                + "if(!s){s=document.createElement('style');s.id='morphe-catppuccin';document.head.appendChild(s);}"
                + "s.textContent=" + JSONObject.quote(css) + ";})()";
            view.evaluateJavascript(script, null);
        } catch (Exception error) {
            android.util.Log.w("CatppuccinFastmail", "Could not load theme stylesheet", error);
        }
    }
}
