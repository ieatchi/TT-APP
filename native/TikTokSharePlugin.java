package net.nexumc.stretch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;

/** Opens TikTok directly with an image ready to post (falls back to the share sheet). */
@CapacitorPlugin(name = "TikTokShare")
public class TikTokSharePlugin extends Plugin {
    private static final String[] PACKAGES = {
        "com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.zhiliaoapp.musically.go"
    };

    private String installedTikTok() {
        PackageManager pm = getContext().getPackageManager();
        for (String p : PACKAGES) {
            try { pm.getPackageInfo(p, 0); return p; } catch (Exception ignored) { }
        }
        return null;
    }

    @PluginMethod
    public void isInstalled(PluginCall call) {
        JSObject r = new JSObject();
        r.put("installed", installedTikTok() != null);
        call.resolve(r);
    }

    @PluginMethod
    public void share(PluginCall call) {
        String data = call.getString("data");
        String text = call.getString("text", "");
        if (data == null) { call.reject("No image"); return; }
        try {
            if (data.startsWith("data:")) data = data.substring(data.indexOf(',') + 1);
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            File dir = new File(getContext().getCacheDir(), "share");
            dir.mkdirs();
            File f = new File(dir, "taffy_post_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(f);
            out.write(bytes);
            out.close();
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", f);

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("image/png");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            if (text != null && text.length() > 0) i.putExtra(Intent.EXTRA_TEXT, text);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            JSObject r = new JSObject();
            String pkg = installedTikTok();
            if (pkg != null) {
                i.setPackage(pkg);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                r.put("app", "tiktok");
            } else {
                Intent chooser = Intent.createChooser(i, "Post");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(chooser);
                r.put("app", "chooser");
            }
            call.resolve(r);
        } catch (Exception e) {
            call.reject(e.getMessage() != null ? e.getMessage() : "Share failed");
        }
    }
}
