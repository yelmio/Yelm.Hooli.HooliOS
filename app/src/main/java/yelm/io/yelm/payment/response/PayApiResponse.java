package yelm.io.yelm.payment.response;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.reactivex.Observable;

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
        Log.d("AlexDebug", "getData");

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
            Log.d("AlexDebug", "handleError");
            return Observable.error(new PayApiError(message));
        }
    }
}
