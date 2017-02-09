package net.neevek.android.lib.lightimagepicker.util;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Lilith Games
 * Created by JiaminXie on 09/02/2017.
 */

public class Util {
    public static boolean copyFile(File srcFile, File dstFile) {
        if (srcFile == null || !srcFile.exists()) {
            return false;
        }
        if (dstFile == null) {
            return false;
        }

        InputStream in = null;
        FileOutputStream fos = null;
        try {
            in = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            byte[] buffer = new byte[4096];

            int lenRead;
            while ((lenRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, lenRead);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safeClose(in);
            safeClose(fos);
        }

        return false;
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showToast(final Context context, final String msg) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else {
            Async.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
