package id.durianpay.android.utils;

import android.util.Log;
import io.sentry.Sentry;

public class Logger {
    public void debug(String tag, String message) {
        Log.d(tag, message);
        Sentry.capture(message);
    }
}
