
package id.durianpay.android.model;

import com.google.gson.annotations.SerializedName;

public class DPaymentFailed {

    @SerializedName("response")
    private Response mResponse;
    @SerializedName("type")
    private String mType;

    public Response getResponse() {
        return mResponse;
    }

    public void setResponse(Response response) {
        mResponse = response;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
