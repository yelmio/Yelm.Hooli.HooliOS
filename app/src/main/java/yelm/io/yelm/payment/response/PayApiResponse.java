package yelm.io.yelm.payment.response;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.reactivex.Observable;
import yelm.io.yelm.support_stuff.Logging;

public class PayApiResponse<T> {

    @SerializedName("Success")
    private boolean success;

    @SerializedName("Message")
    private String message;

    @Nullable
    @SerializedName("Model")
    private T data;

    @Nullable
    public T getData() {
        Log.d(Logging.debug, "getData");

        return data;
    }

    public boolean isSuccess() {
        if (success == false && data == null)
            return false;
        else if (success == false && data != null)
            return true;
        else
            return success;
    }

    public Observable<PayApiResponse<T>> handleError() {
        if (isSuccess()) {
            return Observable.just(this);
        } else {
            Log.d(Logging.debug, "handleError");
            return Observable.error(new PayApiError(message));
        }
    }
}
