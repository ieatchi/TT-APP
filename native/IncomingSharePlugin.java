package net.nexumc.stretch;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * "Share to Taffy": photos, clips or text shared from the gallery (or any app) are copied
 * into the app cache and handed to the web app as a "shared" event.
 * The event is kept until the app is listening, so it works on a cold start too.
 */
@CapacitorPlugin(name = "IncomingShare")
public class IncomingSharePlugin extends Plugin {
    private static final int MAX_FILES = 40;

    @Override
    public void load() {
        if (getActivity() != null) handle(getActivity().getIntent());
    }

    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);
        handle(intent);
    }

    private void handle(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) return;

        final ArrayList<Uri> uris = new ArrayList<>();
        try {
            if (Intent.ACTION_SEND.equals(action)) {
                Uri u = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (u != null) uris.add(u);
            } else {
                ArrayList<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (list != null) uris.addAll(list);
            }
        } catch (Exception ignored) { }
        ClipData clip = intent.getClipData();
        if (uris.isEmpty() && clip != null) {
            for (int k = 0; k < clip.getItemCount(); k++) {
                Uri u = clip.getItemAt(k).getUri();
                if (u != null) uris.add(u);
            }
        }
        final String type = intent.getType();
        CharSequence cs = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        final String text = cs == null ? "" : cs.toString();

        // Mark it handled so a screen rotation doesn't import the same files again.
        if (getActivity() != null && intent == getActivity().getIntent()) {
            getActivity().setIntent(new Intent(Intent.ACTION_MAIN));
        }

        new Thread(() -> {
            JSArray files = new JSArray();
            File dir = new File(getContext().getCacheDir(), "incoming");
            dir.mkdirs();
            ContentResolver cr = getContext().getContentResolver();
            int n = 0;
            for (Uri u : uris) {
                if (n >= MAX_FILES) break;
                try {
                    String mime = cr.getType(u);
                    if (mime == null) mime = type;
                    String name = displayName(cr, u);
                    if (name == null || name.isEmpty()) name = "shared_" + n;
                    name = name.replaceAll("[^A-Za-z0-9._-]", "_");
                    File f = new File(dir, System.currentTimeMillis() + "_" + n + "_" + name);
                    InputStream in = cr.openInputStream(u);
                    if (in == null) continue;
                    OutputStream out = new FileOutputStream(f);
                    byte[] buf = new byte[1 << 16];
                    int r;
                    while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
                    out.close();
                    in.close();
                    JSObject o = new JSObject();
                    o.put("path", Uri.fromFile(f).toString());
                    o.put("mime", mime == null ? "" : mime);
                    o.put("name", name);
                    files.put(o);
                    n++;
                } catch (Exception ignored) { }
            }
            final JSObject data = new JSObject();
            data.put("files", files);
            data.put("text", text);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> notifyListeners("shared", data, true));
            } else {
                notifyListeners("shared", data, true);
            }
        }).start();
    }

    private static String displayName(ContentResolver cr, Uri u) {
        Cursor c = null;
        try {
            c = cr.query(u, new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) return c.getString(i);
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        String last = u.getLastPathSegment();
        return last == null ? null : last;
    }
}
