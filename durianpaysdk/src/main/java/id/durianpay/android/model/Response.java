
package id.durianpay.android.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Response {

    @SerializedName("amount")
    private String mAmount;
    @SerializedName("currency")
    private String mCurrency;
    @SerializedName("msg")
    private String mMsg;
    @SerializedName("order_id")
    private String mOrderId;
    @SerializedName("payment_id")
    private String mPaymentId;
    @SerializedName("payment_method_used")
    private String mPaymentMethodUsed;
    @SerializedName("success")
    private Boolean mSuccess;

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public String getPaymentId() {
        return mPaymentId;
    }

    public void setPaymentId(String paymentId) {
        mPaymentId = paymentId;
    }

    public String getPaymentMethodUsed() {
        return mPaymentMethodUsed;
    }

    public void setPaymentMethodUsed(String paymentMethodUsed) {
        mPaymentMethodUsed = paymentMethodUsed;
    }

    public Boolean getSuccess() {
        return mSuccess;
    }

    public void setSuccess(Boolean success) {
        mSuccess = success;
    }

}
