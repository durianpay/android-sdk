package id.durianpay.android.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import id.durianpay.android.Constants;
import lombok.Data;

@Data
public class DCheckoutOptions extends DCustomerInfo implements Parcelable {

    private String accessToken;
    private String environment;
    private String locale;
    private String siteName;
    private String themeColor;
    private String accentColor;
    private boolean darkMode;
    private String sdkSource;

    private String currency;

    // order info
    private String orderId;
    private String paymentType;
    private String amount;

    //sandbox options
    private boolean forceFail;
    private Integer delayMs;

    public DCheckoutOptions() {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public DCheckoutOptions(Parcel parcel) {
        this.accessToken = parcel.readString();
        this.environment = parcel.readString();
        this.locale = parcel.readString();
        this.siteName = parcel.readString();
        this.themeColor = parcel.readString();
        this.accentColor = parcel.readString();
        this.currency = parcel.readString();
        this.orderId = parcel.readString();
        this.customerId = parcel.readString();
        this.customerGivenName = parcel.readString();
        this.customerEmail = parcel.readString();
        this.customerMobile = parcel.readString();
        this.customerAddressLine1 = parcel.readString();
        this.customerAddressLine2 = parcel.readString();
        this.customerCity = parcel.readString();
        this.customerRegion = parcel.readString();
        this.customerCountry = parcel.readString();
        this.customerPostalCode = parcel.readString();
        this.paymentType = parcel.readString();
        this.amount = parcel.readString();
        this.label = parcel.readString();
        this.receiverName = parcel.readString();
        this.receiverPhone = parcel.readString();
        this.landmark = parcel.readString();
        this.sdkSource = parcel.readString();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.darkMode = parcel.readBoolean();
            this.forceFail = parcel.readBoolean();
            this.delayMs = parcel.readInt();
        }
    }

    @Override
    public int describeContents() { return 0; }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(accessToken);
        parcel.writeString(environment);
        parcel.writeString(locale);
        parcel.writeString(siteName);
        parcel.writeString(themeColor);
        parcel.writeString(accentColor);
        parcel.writeString(currency);
        parcel.writeString(orderId);
        parcel.writeString(customerId);
        parcel.writeString(customerGivenName);
        parcel.writeString(customerEmail);
        parcel.writeString(customerMobile);
        parcel.writeString(customerAddressLine1);
        parcel.writeString(customerAddressLine2);
        parcel.writeString(customerCity);
        parcel.writeString(customerRegion);
        parcel.writeString(customerCountry);
        parcel.writeString(customerPostalCode);
        parcel.writeString(paymentType);
        parcel.writeString(amount);
        parcel.writeString(label);
        parcel.writeString(receiverName);
        parcel.writeString(receiverPhone);
        parcel.writeString(landmark);
        parcel.writeString(sdkSource);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(darkMode);
            parcel.writeBoolean(forceFail);
            if(delayMs != null) parcel.writeInt(delayMs);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public DCheckoutOptions createFromParcel(Parcel parcel) {
            return new DCheckoutOptions(parcel);
        }

        @Override
        public DCheckoutOptions[] newArray(int i) {
            return new DCheckoutOptions[i];
        }
    };

    public String toPostData() throws Exception {
        StringBuilder builder = new StringBuilder();
        if (accessToken != null && !accessToken.isEmpty()) builder.append("&access_key=").append(accessToken);
        else throw new Exception("Access token cannot be empty");

        if ((environment != null && !environment.isEmpty()) &&
                (environment.equals("production") || environment.equals("staging"))) builder.append("&environment=").append(environment);
        else throw new Exception("Please provide the correct environment");

        if ((locale != null && !locale.isEmpty()) &&
                (locale.equals("en") || locale.equals("id"))) builder.append("&locale=").append(locale);
        else throw new Exception("Please provide the correct locale");

        if (siteName != null && !siteName.isEmpty()) builder.append("&site_name=").append(siteName);
        else throw new Exception("Site name cannot be empty");

        if (themeColor != null) builder.append("&theme_color=").append(themeColor);
        if (accentColor != null) builder.append("&accent_color=").append(accentColor);

        // order info
        if (orderId != null && !orderId.isEmpty()) builder.append("&order_id=").append(orderId);
        else throw new Exception("Order ID cannot be empty");

        if (paymentType != null && !paymentType.isEmpty()) {
            builder.append("&type=").append(paymentType);
        }

        if (amount != null && !amount.isEmpty()) {
            builder.append("&amount=").append(amount);
        } else throw new Exception("Amount must not be empty");

        if (currency != null) {
            builder.append("&currency=").append(currency);
        } else throw new Exception("Currency must not be empty");

        // customer info
        if (customerId != null && !customerId.isEmpty()) {
            builder.append("&customer_id=").append(customerId);
        } else throw new Exception("Customer ID must not be empty");

        if (customerEmail != null && !customerEmail.isEmpty()) {
                builder.append("&customer_email=").append(customerEmail);
        } else throw new Exception("Customer email must not be empty");

        if (customerGivenName != null) builder.append("&customer_given_name=").append(customerGivenName);
        if (customerMobile != null) builder.append("&customer_phone=").append(customerMobile);
        if (customerAddressLine1 != null) builder.append("&customer_address_line1=").append(customerAddressLine1);
        if (customerAddressLine2 != null) builder.append("&customer_address_line2=").append(customerAddressLine2);
        if (customerCity != null) builder.append("&customer_city=").append(customerCity);
        if (customerRegion != null) builder.append("&customer_region=").append(customerRegion);
        if (customerCountry != null) builder.append("&customer_country=").append(customerCountry);
        if (customerPostalCode != null) builder.append("&customer_postal_code=").append(customerPostalCode);
        if (label != null) builder.append("&label=").append(label);
        if (receiverName != null) builder.append("&receiver_name=").append(receiverName);
        if (receiverPhone != null) builder.append("&receiver_phone=").append(receiverPhone);
        if (landmark != null) builder.append("&landmark=").append(landmark);
        if (darkMode) builder.append("&dark_mode=").append(darkMode);
        if (forceFail) builder.append("&force_fail=").append(forceFail);
        if (delayMs != null) builder.append("&delay_ms=").append(delayMs);
        if (sdkSource == null) builder.append("&sdk_source=").append(Constants.SNOWPLOW_APP_ID);
        else builder.append("&sdk_source=").append(sdkSource);
        return builder.toString();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public void setForceFail(boolean force_fail) { this.forceFail = force_fail; }
    public  void setDelayMs(Integer delay_ms) { this.delayMs = delay_ms; }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCustomerID(String customerID) {
        this.customerId = customerID;
    }

    public void setSdkSource(String sdkSource) { this.sdkSource = sdkSource; }

    public String  getSdkSource() {return sdkSource; }
    public String getCurrency() { return currency; }
    public String getOrderId() { return orderId; }
    public String getEnvironment() { return environment; }
    public String getLocale() { return locale; }
    public String getSiteName() { return siteName; }
    public String getAccentColor() { return accentColor; }
    public String getThemeColor() { return themeColor; }
    public String getAmount() { return amount; }
}

