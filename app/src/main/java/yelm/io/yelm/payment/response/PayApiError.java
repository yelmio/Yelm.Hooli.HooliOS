package yelm.io.yelm.payment.response;

import android.util.Log;

public class PayApiError extends Throwable {

    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PayApiError(String message) {
        Log.d("AlexDebug", "message"+ message);
        this.message = message;
    }
}
