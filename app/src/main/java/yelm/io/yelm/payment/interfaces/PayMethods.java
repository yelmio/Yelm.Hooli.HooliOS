package yelm.io.yelm.payment.interfaces;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import yelm.io.yelm.payment.models.PayRequestArgs;
import yelm.io.yelm.payment.models.Post3dsRequestArgs;
import yelm.io.yelm.payment.models.Transaction;
import yelm.io.yelm.payment.response.PayApiResponse;


public interface PayMethods {

    @POST("cp_charge.php?platform=5ee93e25691869.14796049")
    Observable<PayApiResponse<Transaction>> charge(@Header("Content-Type") String contentType, @Body PayRequestArgs args);

    @POST("cp_auth.php?platform=5ee93e25691869.14796049")
    Observable<PayApiResponse<Transaction>> auth(@Header("Content-Type") String contentType, @Body PayRequestArgs args);

    @POST("cp_post3ds.php?platform=5ee93e25691869.14796049")
    Observable<PayApiResponse<Transaction>> post3ds(@Header("Content-Type") String contentType, @Body Post3dsRequestArgs args);
}
