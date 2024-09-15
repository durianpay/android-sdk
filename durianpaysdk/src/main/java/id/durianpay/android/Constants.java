package id.durianpay.android;

public class Constants {

    public static final String API_BASE_URL = "https://api.durianpay.id";
    public static final String ORDER_URL = API_BASE_URL + "/orders";

    public static final String PLAYSTORE_BASE_URL = "market://details?id=";
    public static final String OVO_APP_ID = "com.ovo.merchant";
    public static final String SHOPEE_APP_ID = "com.shopee.id";
    public static final String GOJEK_APP_ID = "com.gojek.app";
    public static final String LINKAJA_APP_ID = "com.telkom.mwallet";

    public static final String CLIENT_IDENTIFIER = "Durianpay Android SDK";
    public static final String CLIENT_API_VERSION = "v1.0.0";
    public static final String CLIENT_TYPE = "SDK";
    public static final String SENTRY_DSN = "https://dd3653f0de5d4ab49d087cedf6ba00dc@o444548.ingest.sentry.io/5419554";

    public static final String CLICKS_SCHEMA = "iglu:id.durianpay/clicks/jsonschema/1-0-0";
    public static final String COUNTS_SCHEMA = "iglu:id.durianpay/counts/jsonschema/1-0-0";

    public static final String SNOWPLOW_NAMESPACE = "android-stats";
    public static final String SNOWPLOW_APP_ID = "dpay-android";
    public static final String CUSTOMER_ID_TAG = "customerID";
    public static final String ORDER_ID_TAG = "orderID";
    public static final String MERCHANT_ID_TAG = "merchantID";
    public static final String PAYMENT_AMOUNT_TAG = "finalPaymentAmount";
    public static final String CURRENCY_TAG = "currency";
    public static final String EVENT_NAME_TAG = "eventName";
    public static final String EVENT_COUNT_TAG = "num";
}
