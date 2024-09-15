package id.durianpay.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.UUID;

import id.durianpay.android.Interfaces.CheckoutResultListener;
import id.durianpay.android.model.DCheckoutOptions;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

import static id.durianpay.android.Constants.SENTRY_DSN;

public class Durianpay {
    private static Durianpay _instance; //static instance to return Durianpay object
    private Context _context;
    private String _accessToken;
    private String _sessionId;
    private CheckoutResultListener _resultListener;

    private Durianpay(Context context){
        this._context = context;
        Sentry.init(SENTRY_DSN, new AndroidSentryClientFactory(context));
    }

    protected List<ResolveInfo> getInstalledApps()  {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return this._context.getPackageManager().queryIntentActivities( mainIntent, 0);
    }

    public String getSessionId() { return this._sessionId; } //return the session id
    private void setSessionId() { this._sessionId = UUID.randomUUID().toString(); } //set session id by generating a random UUID(universally unique identifier)

    public String getAccessToken() { return this._accessToken; }
    private void setAccessToken(String accessToken) { this._accessToken = accessToken; }

    public CheckoutResultListener getResultListener() { return this._resultListener; } //return checkout listener interface
    public void setResultListener(CheckoutResultListener listener) { this._resultListener = listener; } //set the resultListener to listener sent by merchant

    // return a singleton instance of Durianpay
    public static Durianpay getInstance(Context context) {
        if (Durianpay._instance != null) {
            Log.i("durianpay", "Durianpay SDK already initialized");
        } else {
            Durianpay._instance = new Durianpay(context);
            Log.i("durianpay", "Durianpay SDK initialized");
        }
        return Durianpay._instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkout(final DCheckoutOptions options, CheckoutResultListener listener) {
        if (options == null) return;
        if (this._context == null) return;
        this.setResultListener(listener);
        this.setAccessToken(options.getAccessToken());
        this.setSessionId();

        //start Checkout Activity class
        Intent intent = new Intent(this._context, CheckoutActivity.class);
        intent.setClass(this._context, CheckoutActivity.class);
        intent.setAction(CheckoutActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(CheckoutActivity.MERCHANT_CONFIG_KEY, options);

        this._context.startActivity(intent);
    }
}
