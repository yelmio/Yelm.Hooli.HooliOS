package yelm.io.yelm.payment.response;

import android.util.Log;

import yelm.io.yelm.support_stuff.Logging;

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
        Log.d(Logging.debug, "message"+ message);
        this.message = message;
    }
}
