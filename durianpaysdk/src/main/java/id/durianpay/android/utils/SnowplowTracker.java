package id.durianpay.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import java.util.HashMap;
import java.util.Map;

import id.durianpay.android.Constants;

import static id.durianpay.android.Constants.CURRENCY_TAG;
import static id.durianpay.android.Constants.CUSTOMER_ID_TAG;
import static id.durianpay.android.Constants.EVENT_COUNT_TAG;
import static id.durianpay.android.Constants.EVENT_NAME_TAG;
import static id.durianpay.android.Constants.MERCHANT_ID_TAG;
import static id.durianpay.android.Constants.ORDER_ID_TAG;
import static id.durianpay.android.Constants.PAYMENT_AMOUNT_TAG;
import static id.durianpay.android.BuildConfig.SNOWPLOW_URL;
import static id.durianpay.android.Constants.SNOWPLOW_APP_ID;
import static id.durianpay.android.Constants.SNOWPLOW_NAMESPACE;

//Snowplow Tracker is used to track the activity of events
public class SnowplowTracker {

    //return a tracker instance of the class which contains all the events to be tracked
    public static Tracker getTracker(Context context) {
        Emitter emitter = getEmitter(context);
        Subject subject = getSubject(context);

        return Tracker.init(new Tracker.TrackerBuilder(emitter, SNOWPLOW_NAMESPACE, SNOWPLOW_APP_ID, context)
                .subject(subject)
                .trackerDiagnostic(true)
                .build()
        );
    }

    //Configure the endpoints for collection of data along with other configurations for the tracker
    private static Emitter getEmitter(Context context) {
        Emitter emitter = new Emitter.EmitterBuilder(SNOWPLOW_URL, context)
                .security(RequestSecurity.HTTPS)
                .tick(3)
                .callback(getCallback())
                .build();
        emitter.flush();
        return emitter;
    }

    //check if the events sent to the database are successful
    @SuppressLint("LongLogTag")
    private static RequestCallback getCallback() {
        return new RequestCallback() {

            @Override
            public void onSuccess(int successCount) {
                Log.d("Emitter Send Success - Events sent: ", String.valueOf(successCount));
            }
            @Override
            public void onFailure(int successCount, int failureCount) {
                Log.d("Emitter Send Failure: - Events sent: " + successCount + "- Events failed: ", String.valueOf(failureCount));
            }
        };
    }

    //return a subject(context) for tracker
    private static Subject getSubject(Context context) {
        return new Subject.SubjectBuilder()
                .context(context)
                .build();
    }

    /*
    track the opening of webview
    params:
    tracker - instance of tracker class
    customerId - customer id of user sent by the merchant
    orderId - order id of user sent by the merchant
    currency - buying curreny to pay for the order
    event_name - name of the event to be tracked
    sessionId - unique UUID generated by the Durianpay class
    amount - the order amount
     */
    public static void trackWebview(Tracker tracker,
                                    String customerId,
                                    String orderId,
                                    String currency,
                                    String event_name,
                                    String sessionId,
                                    Double amount) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("param1", "webview_android");
        dataMap.put(CUSTOMER_ID_TAG, customerId);
        dataMap.put(ORDER_ID_TAG, orderId);
        dataMap.put(MERCHANT_ID_TAG, "");
        dataMap.put(PAYMENT_AMOUNT_TAG, Math.round(amount));
        dataMap.put(CURRENCY_TAG, currency);
        dataMap.put(EVENT_NAME_TAG, event_name);
        dataMap.put("sessionID", sessionId);

        //tracking a custom event with the Clicks schems and the above hash map as object data
        SelfDescribingJson sdj = new SelfDescribingJson(Constants.CLICKS_SCHEMA, dataMap);
        tracker.track(SelfDescribing.builder()
                .eventData(sdj)
                .build()
        );
    }


    /*
    track the closing of webview
    params:
    tracker - instance of tracker class
    customerId - customer id of user sent by the merchant
    orderId - order id of user sent by the merchant
    currency - buying curreny to pay for the order
    event_name - name of the event to be tracked
    sessionId - unique UUID generated by the Durianpay class
    amount - the order amount
    payment_method_use - method used for paying the amount
    count - number of times the event is to be tracked
     */
    public static void trackCompletion(Tracker tracker,
                                       String customerId,
                                       String orderId,
                                       String currency,
                                       String event_name,
                                       String payment_method_use,
                                       int count,
                                       double amount,
                                       String session_id) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("param1", payment_method_use);
        dataMap.put(CUSTOMER_ID_TAG, customerId);
        dataMap.put(ORDER_ID_TAG, orderId);
        dataMap.put(MERCHANT_ID_TAG, "");
        dataMap.put(PAYMENT_AMOUNT_TAG, Math.round(amount));
        dataMap.put(CURRENCY_TAG, currency);
        dataMap.put(EVENT_COUNT_TAG, count);
        dataMap.put(EVENT_NAME_TAG, event_name);
        dataMap.put("sessionID", session_id);

        //tracking a custom event with the Clicks schems and the above hash map as object data
        SelfDescribingJson sdj = new SelfDescribingJson(Constants.COUNTS_SCHEMA, dataMap);
        tracker.track(SelfDescribing.builder()
                .eventData(sdj)
                .build()
        );
    }
}
