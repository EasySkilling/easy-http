package com.easyhttp.core.thread;

import android.os.Handler;
import android.os.Looper;

public class UiThread {

    private static UiThread sUiThread;
    private Handler handler = new Handler(Looper.getMainLooper());

    public static UiThread getInstance() {
        if (sUiThread == null) {
            synchronized (UiThread.class) {
                if (sUiThread == null) {
                    sUiThread = new UiThread();
                }
            }
        }
        return sUiThread;
    }

    public void runOnUiThread(Runnable action) {
        if (action != null) {
            handler.post(action);
        }
    }

}
