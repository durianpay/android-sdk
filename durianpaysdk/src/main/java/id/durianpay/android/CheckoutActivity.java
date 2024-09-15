package id.durianpay.android;

import android.app.AlertDialog;
import android.app.Dialog;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import android.view.KeyEvent;
import android.view.View;

import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.snowplowanalytics.snowplow.tracker.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import id.durianpay.android.model.DCheckoutOptions;
import id.durianpay.android.model.DPaymentFailed;
import id.durianpay.android.model.DPaymentSuccess;
import id.durianpay.android.utils.AppHandlerList;
import id.durianpay.android.utils.PeriodicResourceDownload;
import id.durianpay.android.utils.SnowplowTracker;
import id.durianpay.android.utils.WebResourceHelper;

import static id.durianpay.android.BuildConfig.CHECKOUT_URL;
import static id.durianpay.android.Constants.GOJEK_APP_ID;
import static id.durianpay.android.Constants.LINKAJA_APP_ID;
import static id.durianpay.android.Constants.OVO_APP_ID;
import static id.durianpay.android.Constants.PLAYSTORE_BASE_URL;
import static id.durianpay.android.Constants.SHOPEE_APP_ID;
import static id.durianpay.android.utils.SnowplowTracker.trackCompletion;
import static id.durianpay.android.utils.SnowplowTracker.trackWebview;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CheckoutActivity extends AppCompatActivity {

    public static final String MERCHANT_CONFIG_KEY = "merchant_config";
    public static final String TAG = CheckoutActivity.class.getSimpleName();

    private Durianpay _dpayInstance;
    private WebView _webView;
    private ProgressBar _progressBar;

    private DCheckoutOptions _options;

    private Tracker tracker;

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout);
        _options = getIntent().getParcelableExtra(MERCHANT_CONFIG_KEY); //checkout options sent by the merchant to Durianpay class

        this._dpayInstance = Durianpay.getInstance(this);

        this._progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (_progressBar != null) this._progressBar.setVisibility(View.VISIBLE); //show a circular progress

        this._webView = (WebView) findViewById(R.id.checkoutWebView);
        tracker = SnowplowTracker.getTracker(getApplicationContext()); //get the snowplow tracker instance from SnowplowTracker class to track events

        //Work Manager uses background thread to periodically download resources i.e., images, css and js files to cache in the local
        //where the time period is one day.
        PeriodicWorkRequest.Builder downloadResources =
                new PeriodicWorkRequest.Builder(PeriodicResourceDownload.class, 1, TimeUnit.DAYS);
        PeriodicWorkRequest request = downloadResources.build();
        WorkManager.getInstance(this).enqueue(request);

        _renderWebView(); //function to load dpay checkout in webview
    }

    @Override
    public void onBackPressed() {
        //clear and destroy the webview whenever this activity is closed
        if (_webView != null){
            _webView.clearHistory();
            _webView.removeAllViews();
            _webView.destroy();
        }
        finish();
        super.onBackPressed();
    }

    //initialize webview settings and return false if settings cannot be enabled
    private boolean _initializeWebViewSettings(WebView webView) {
        if (webView == null) return false;

        WebSettings webSettings = webView.getSettings();
        if (webSettings != null) {
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //remove default webview cache to implement our caching
            webSettings.setSupportZoom(true); //zooming functionality in webview
            webSettings.setBuiltInZoomControls(true);
            webSettings.setPluginState(WebSettings.PluginState.ON); //load the content always to make webview faster
            webSettings.setAllowFileAccess(true); //allow webview to access files from local file system
            webSettings.setMixedContentMode(0); //load all content even if the protocol is insecure like http
            webSettings.setJavaScriptEnabled(true); //enable webview to handle js
            webSettings.setDomStorageEnabled(true);  // Open DOM storage function
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //allow to open new window when required(if a page is redirected to open in a new tab) with a child webview
            webSettings.setSupportMultipleWindows(true); //support to handle child webviews
            webSettings.setLoadWithOverviewMode(true); //zoom out the content to fit the screen by width
            webSettings.setUseWideViewPort(true); //to use the value of width according to the viewport tag in HTML
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH); //high priority for hardware to load the webview
            webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); //allow secure and insecure content
            webSettings.setDatabaseEnabled(true); //use the database for webview
            webSettings.setDatabasePath(this.getFilesDir().getParentFile().getPath()+"/databases/"); //path to use the database for webview
        }

        //hide the scrollbars inside webview
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        return true;
    }

    /* dialog builder to show dialogs when error occured or if confirmation needed to close child webview
    params:
        webview - close the webview and destroy when closed the dialog with confirmation
        dialog - check and destroy an existing dialog i.e., the child webview is opened inside an dialog
        title - title of the dialog to be opened
        message - message inside the dialog
        positiveBtn - text for dialog's positive button
        negativeBtn - text for dialog's negative button
    */
    private void _dialogBuilder(final WebView webView,
                                final Dialog dialog,
                                final String title,
                                String message,
                                String positiveBtn,
                                String negativeBtn) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CheckoutActivity.this);
        if (dialogBuilder == null) return;

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (webView == null || dialog == null) {
                    onBackPressed();
                } else {
                    //destroy the webview and existing dialog(child webview)
                    webView.removeAllViews();
                    webView.clearHistory();
                    webView.destroy();
                    dialog.dismiss();
                }
            }
        });
        dialogBuilder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.show();
    }

    //load webview to open checkout page
    private void _renderWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        boolean retVal = _initializeWebViewSettings(this._webView);
        if (!retVal) {
            System.out.println("webview couldnt be initialized");
            return;
        }

        //configure the android back button according to the webview back functionality
        this._webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //key event is used to check the button press
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    //check if back button is pressed
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //go back if webview can go back else close the webview and activity
                        //and return true to handle the key press event
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return true;
                        } else {
                            onBackPressed();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        try {
            this._webView.addJavascriptInterface(new JavascriptInterface(), "android"); //set a channel/bridge between native and webview js with an event name for interface
            this._webView.setWebViewClient(new CustomWebViewClient() {}); //handle all redirects, errors and other webview activity
            this._webView.setWebChromeClient(new CustomWebChromeClient() {}); //handle popups, windows other activity supported by browser

            try {
                Log.d("checkout_url",  CHECKOUT_URL + "?" + _options.toPostData() + "&session_id=" + _dpayInstance.getSessionId());
//                //load checkout page with options as query parameters
                String url = CHECKOUT_URL + "?" + _options.toPostData() + "&session_id=" + _dpayInstance.getSessionId();
                this._webView.loadUrl(url);
                this._webView.setDownloadListener(new DownloadListener() {
                    @Override
                    public void onDownloadStart(String downloadUrl, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                        Toast.makeText(CheckoutActivity.this, "Downloading", Toast.LENGTH_SHORT).show();
                        String path = createAndSaveFileFromBase64Url(downloadUrl);
                        Log.d("download_path",path);

                        Toast.makeText(CheckoutActivity.this, "Downloaded "+path, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            Log.e("ERROR:Exception caught ", e.toString());
        }


    }

    public String createAndSaveFileFromBase64Url(String url) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = getString(R.string.qris_download) + System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);
        try {
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            ((FileOutputStream) os).write(decodedBytes);
            os.close();

            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_VIEW);
            PendingIntent pIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            }

            createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this, "dpay_channel_id")
                    .setSmallIcon(R.drawable.ic_dpay)
                    .setContentText(getString(R.string.msg_qris_file_downloaded))
                    .setContentTitle(filename)
                    .setContentIntent(pIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(R.string.qris_notification, notification);
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(getApplicationContext(),R.string.error_download, Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DurianPay";
            String description ="DurianPay";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("dpay_channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //class to handle the callbacks from the channel/bridge created between js and native
    public class JavascriptInterface {
        //callback function triggered when the payment is successful
        @android.webkit.JavascriptInterface
        public void onSuccess(String value) {
            try {
                JSONObject successResponse = new JSONObject(value);
                //track the completion of payment for success event
                trackCompletion(tracker,
                        _options.customerId,
                        successResponse.getJSONObject("response").getString("order_id"),
                        successResponse.getJSONObject("response").getString("currency"),
                        "payment_" + successResponse.getString("type"),
                        successResponse.getJSONObject("response").getString("payment_method_used"),
                        1,
                        Double.parseDouble(successResponse.getJSONObject("response").getString("amount")),
                        _dpayInstance.getSessionId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            DPaymentSuccess paymentSuccess = gson.fromJson(value, DPaymentSuccess.class);
            _dpayInstance.getResultListener().onSuccess(paymentSuccess); //callback to the merchant's successful transaction function
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed(); }
            });
        }

        //callback function triggered when the payment is failed
        @android.webkit.JavascriptInterface
        public void onFailure(String value) {
            try {
                JSONObject failureResponse = new JSONObject(value);
                //track the completion of payment for failed event
                trackCompletion(tracker,
                        _options.customerId,
                        failureResponse.getJSONObject("response").getString("order_id"),
                        failureResponse.getJSONObject("response").getString("currency"),
                        "payment_" + failureResponse.getString("type"),
                        failureResponse.getJSONObject("response").getString("payment_method_used"),
                        1,
                        Double.parseDouble(failureResponse.getJSONObject("response").getString("amount")), _dpayInstance.getSessionId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            DPaymentFailed paymentFailed = gson.fromJson(value, DPaymentFailed.class);
            _dpayInstance.getResultListener().onFailure(paymentFailed); //callback to the merchant's failed transaction function
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed(); }
            });
        }

        //callback function triggered when the transaction is closed
        @android.webkit.JavascriptInterface
        public void onClose(String value) {
            try {
                JSONObject closeResponse = new JSONObject(value);
                //track the completion of payment for close event
                trackCompletion(tracker,
                        _options.customerId,
                        closeResponse.getJSONObject("response").getString("order_id"),
                        closeResponse.getJSONObject("response").getString("currency"),
                        "payment_" + closeResponse.getString("type"),
                        closeResponse.getJSONObject("response").getString("payment_method_used"),
                        1,
                        Double.parseDouble(closeResponse.getJSONObject("response").getString("amount")), _dpayInstance.getSessionId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            _dpayInstance.getResultListener().onClose(value); //callback to the merchant's close transaction function
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            });
        }
    }

    //open child webviews when required
    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            if (resultMsg == null) return false;

            Log.d("new window", view.getUrl());
            //initialize the new webview settings as the main webview
            final WebView newWebView = new WebView(CheckoutActivity.this);
            boolean retVal = _initializeWebViewSettings(newWebView);
            if (!retVal) return false;

            // dialog is to keep the webview inside it and dialog builder below is used to warn the user if he presses back button in middle of payment
            // but when clicked on Ok, this dialog has to be closed along with dialogbuilder
            final Dialog dialog = new Dialog(CheckoutActivity.this, R.style.Theme_AppCompat_Light_NoActionBar_FullScreen);
            dialog.setContentView(newWebView);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            newWebView.setWebViewClient(new CustomWebViewClient() {});
            newWebView.setWebChromeClient(new WebChromeClient() {
                //close the dialog when the webview is closed
                @Override
                public void onCloseWindow(WebView window) {
                    super.onCloseWindow(window);
                    dialog.dismiss();
                }
            });

            //configure the android back button according to the webview back functionality
            newWebView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //key event is used to check the button press
                    if(event.getAction() == KeyEvent.ACTION_DOWN) {
                        final WebView webView = (WebView) v;
                        //check if back button is pressed and webview can go back
                        //open a confirmation dialog if the webview can go back
                        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                            _dialogBuilder(
                                    webView,
                                    dialog,
                                    "Warning",
                                    "Are you sure you want to cancel the ongoing payment?",
                                    "Yes", "No");
                            return false;
                        }
                    }
                    return false;
                }
            });

            //returns webview across all threads
            WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) resultMsg.obj;
            if (webViewTransport == null) return false;

            webViewTransport.setWebView(newWebView);
            resultMsg.sendToTarget();

            return true;
        }
    }

    //handle redirects, urls, errors and implement local cache etc.
    private class CustomWebViewClient extends WebViewClient {
        //same functionality as the below function but depreceated from Android 7.0
        public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
            Log.d("redirect_url", url);
            return false;
        }

        //intercept and perform any operation on the URLs about to be loaded inside the webview if returned true
        //webview will automatically assign a proper handler for URL if returned false
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("redirect_url_request", request.getUrl().toString());
            String url = request.getUrl().toString();
            final Uri uri = request.getUrl();

            //check if the url starts with shopeeid:// and allow the url to be loaded inside an intent as uri
            // if the application is installed else, open play store to download the application
            //This will open the shopeepay application's payment page with defined amount
            if (url.startsWith("shopeeid://")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                ResolveInfo shopeePresent = getApplicationContext().getPackageManager().resolveActivity(intent, 0);
                if (shopeePresent != null)
                    startActivity(intent);
                else
                    startNewActivity(getApplicationContext(), SHOPEE_APP_ID);
                return true;
            }

            if (url.startsWith("https://gojek.link")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                ResolveInfo gopayPresent = getApplicationContext().getPackageManager().resolveActivity(intent, 0);
                if (gopayPresent != null)
                    startActivity(intent);
                else
                    startNewActivity(getApplicationContext(), GOJEK_APP_ID);
                return true;
            }

            if (url.startsWith("https://linkaja.id")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                ResolveInfo linkajaPresent = getApplicationContext().getPackageManager().resolveActivity(intent, 0);
                if (linkajaPresent != null)
                    startActivity(intent);
                else
                    startNewActivity(getApplicationContext(), LINKAJA_APP_ID);
                return true;
            }

            //check if the url starts with app:// and use regex to get the package name of the application
            // open the application it is installed else, open play store to download the application
            //implemented now for OVO, BCA, BNI
            if (url.startsWith("app://")) {
                String[] urlParts = url.split("\\:\\/\\/");
                String packageName = urlParts[1];
                startNewActivity(getApplicationContext(), packageName);
                return true;
            }
            return false;
        }

        //intercept all the resource requests and load them from local if they are available
        //resources are downloaded in the PeriodicResourceDownload class
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if(request.getUrl() == null){
                return super.shouldInterceptRequest(view, request); //allow to webview to handle the request
            }

            //use the WebResourceHelper class to load the files downloaded in local
            WebResourceHelper webResourceHelper = new WebResourceHelper();
            String filePath = webResourceHelper.getLocalFilePath(request.getUrl().toString(), getApplicationContext());
            String fileName = WebResourceHelper.getLocalFileNameForUrl(request.getUrl().toString());
            String fileExt = WebResourceHelper.getFileExt(fileName);
            String mimeType = webResourceHelper.getMimeType(fileExt);

            //check if a file is present to load from local else, allow the webview to handle itself
            try {
                InputStream data = new FileInputStream(filePath);
                Log.d("returning", "local" + filePath);
                return new WebResourceResponse(mimeType, "UTF-8", data); //load from local as a WebResourceResponse object
            } catch (FileNotFoundException e) {
                Log.d("returning", "actual_request" + request.getUrl().toString());
                return super.shouldInterceptRequest(view, request);
            }
        }

        //check if the application is installed on the device to open from the webview else, open playstore to download
        public void startNewActivity(Context context, String packageName) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent == null) {
                // open playstore in a new window to download the app
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(PLAYSTORE_BASE_URL + packageName));
            }
            // The following flags launch the app outside the current app
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        }

        //catch all the http errors in the webview requests
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            //handle 401 and 500 error response by showing a dialog and closing the webview
            if (errorResponse.getStatusCode() == 500 || (errorResponse.getStatusCode() == 401)) {
                _dialogBuilder(
                    null,
                    null,
                    "Error",
                    errorResponse.getStatusCode() + " error "+
                    errorResponse.getReasonPhrase() + ". Please try again later",
                    "OK",
                    "");
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        //handle all the initializations before the webview has loaded the url
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.setVisibility(View.GONE); //make the webview invisible to show a circular progress bar

            //track the opening of webview using tracker returned from SnowplowTracker class
            trackWebview(tracker,
                    _options.customerId,
                    _options.getOrderId(),
                    _options.getCurrency(),
                    "webview opened",
                    _dpayInstance.getSessionId(),
                    Double.parseDouble(_options.getAmount()));

            if (_progressBar != null) CheckoutActivity.this._progressBar.setVisibility(View.VISIBLE); //make progress bar visible
            writeLocalStorage(view); //add to the local storage of webview/browser
        }

        //handle anything after the webview is loaded
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //remove the progress bar after laoding and show the webview
            if (view.getProgress() == 100) {
                if (_progressBar != null) CheckoutActivity.this._progressBar.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
            }
        }

        //store and track the installed applications on the device into the webview/browser local storage
        private void writeLocalStorage(WebView view) {
            List<ResolveInfo> packages = _dpayInstance.getInstalledApps(); //get all the installed apps on device
            Map<String, String> bankAppList= AppHandlerList.getBankAppList(); //list of bank apps mentioned in AppHandlerList
            StringBuilder sb = new StringBuilder(); //string of all the installed apps
            StringBuilder installedBankAppSb = new StringBuilder(); //string of all the installed bank apps
            String packageNameList = "";
            String installedBankApp = "";
            boolean ovoPresent = false; //boolean to check if OVO is installed

            //add all the installed bank apps to the string buffer with ',' as delimiter and check if OVO is installed
            for (ResolveInfo pkg: packages) {
                sb.append(pkg.activityInfo.packageName).append(',');
                if (bankAppList.containsValue(pkg.activityInfo.packageName)) {
                    installedBankAppSb.append(pkg.activityInfo.packageName).append(',');
                }
                if (pkg.activityInfo.packageName.equals(OVO_APP_ID)){
                    ovoPresent = true;
                }
            }

            //delete the last delimiter
            if (sb.length() > 0) {
                packageNameList = sb.deleteCharAt(sb.length() - 1).toString();
            }

            //delete the last delimiter
            if (installedBankAppSb.length() > 0) {
                installedBankApp = installedBankAppSb.deleteCharAt(installedBankAppSb.length() - 1).toString();
            }

            //clear the local storage on apps tracked if already present because, the result might be
            // cached and few apps might be installed or uninstalled
            String removeItems = "window.localStorage.removeItem('installed_apps');" +
                    "window.localStorage.removeItem('installed_bank_apps');" +
                    "window.localStorage.removeItem('ovo_app');";

            //same as above for versions of android below Android 5.0
            String removeItemsLowerSdk = "javascript:localStorage.removeItem('installed_apps');" +
                    "javascript:localStorage.removeItem('installed_bank_apps');" +
                    "javascript:localStorage.removeItem('ovo_app');";

            //add the string buffer i.e., list of applications to local storage if installed to the local storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript(removeItems, null);
                view.evaluateJavascript("window.localStorage.setItem('installed_apps','" + packageNameList + "');", null);
                view.evaluateJavascript("window.localStorage.setItem('installed_bank_apps','" + installedBankApp + "');", null);
                if (ovoPresent) {
                    view.evaluateJavascript("window.localStorage.setItem('ovo_app','ovo');", null);
                }
            } else {
                view.loadUrl(removeItemsLowerSdk);
                view.loadUrl("javascript:localStorage.setItem('installed_apps','" + packageNameList + "');");
                view.loadUrl("javascript:localStorage.setItem('installed_bank_apps','" + installedBankApp + "');");
                if (ovoPresent) {
                    view.loadUrl("javascript:localStorage.setItem('ovo_app','ovo');");
                }
            }
        }
    }

}



