package yelm.io.raccoon.payment.interfaces;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import yelm.io.raccoon.payment.models.PayRequestArgs;
import yelm.io.raccoon.payment.models.Post3dsRequestArgs;
import yelm.io.raccoon.payment.models.Transaction;
import yelm.io.raccoon.payment.response.PayApiResponse;

public interface PayMethods {
    @POST("charge?platform=5fd33466e17963.29052139")
    Observable<PayApiResponse<Transaction>> charge(@Header("Content-Type") String contentType, @Body PayRequestArgs args);

    @POST("cryptogram?platform=5fd33466e17963.29052139")
    Observable<PayApiResponse<Transaction>> auth(@Header("Content-Type") String contentType, @Body PayRequestArgs args);

    @POST("processing?platform=5fd33466e17963.29052139")
    Observable<PayApiResponse<Transaction>> post3ds(@Header("Content-Type") String contentType, @Body Post3dsRequestArgs args);
}