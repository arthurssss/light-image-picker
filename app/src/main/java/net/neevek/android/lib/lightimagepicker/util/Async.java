package net.neevek.android.lib.lightimagepicker.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lilith Games
 * Created by JiaminXie on 15/01/2017.
 */

public class Async {
    private static ExecutorService sExecutorService = Executors.newSingleThreadExecutor();
    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void run(Runnable task) {
        sExecutorService.execute(task);
    }

    public static void runOnUiThread(Runnable task) {
        sMainHandler.post(task);
    }
}
