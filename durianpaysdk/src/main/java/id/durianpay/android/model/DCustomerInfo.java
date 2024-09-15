package id.durianpay.android.model;

import android.os.Parcel;

import lombok.Data;

@Data
public class DCustomerInfo {
    // customer info
    public String customerId;
    public String customerEmail;
    public String customerGivenName;
    public String customerMobile;
    public String customerAddressLine1;
    public String customerAddressLine2;
    public String customerCity;
    public String customerRegion;
    public String customerCountry;
    public String customerPostalCode;
    public String label;
    public String receiverName;
    public String receiverPhone;
    public String landmark;

    public void setLabel(String label) {
        this.label = label;
    }
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setCustomerGivenName(String customerGivenName) {
        this.customerGivenName = customerGivenName;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }
    public void setCustomerAddressLine1(String addressLine1) {
        this.customerAddressLine1 = addressLine1;
    }
    public void setCustomerAddressLine2(String addressLine2) {
        this.customerAddressLine2 = addressLine2;
    }
    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }
    public void setCustomerRegion(String customerRegion) {
        this.customerRegion = customerRegion;
    }
    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }
    public void setCustomerPostalCode(String customerPostalCode) {
        this.customerPostalCode = customerPostalCode;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
