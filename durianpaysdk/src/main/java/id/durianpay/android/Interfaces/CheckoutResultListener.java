package id.durianpay.android.Interfaces;

import id.durianpay.android.model.DPaymentFailed;
import id.durianpay.android.model.DPaymentSuccess;

public interface CheckoutResultListener {
    void onSuccess(DPaymentSuccess paymentSuccess);
    void onFailure(DPaymentFailed paymentFailed);
    void onClose(String orderRefId);
}
