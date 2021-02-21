package yelm.io.yelm.rest.rest_api;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import yelm.io.yelm.basket.model.BasketCheckPOJO;
import yelm.io.yelm.by_category.ProductsByCategoryClass;
import yelm.io.yelm.loader.model.ApplicationSettings;
import yelm.io.yelm.loader.model.ChatSettingsClass;
import yelm.io.yelm.loader.model.UserLoginResponse;
import yelm.io.yelm.main.model.CategoriesWithProductsClass;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.main.news.NewNews;
import yelm.io.yelm.order.PriceConverterResponseClass;
import yelm.io.yelm.order.promocode.PromoCodeClass;
import yelm.io.yelm.order.user_order.model.UserOrderPOJO;

public interface RestAPI {

    String URL_API_MAIN = "https://rest.yelm.io/api/mobile/";
    String PLATFORM_NUMBER = "5fd33466e17963.29052139";

    @FormUrlEncoded
    @POST("user?")
    Call<UserLoginResponse> createUser(
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Field("platform") String platform,
            @Field("user_info") JSONObject userInfo);

    @GET("application?")
    Call<ApplicationSettings> getAppSettings(@Query("platform") String platform,
                                             @Query("language_code") String languageCode,
                                             @Query("region_code") String regionCode
    );

    @GET("order?")
    Call<UserOrderPOJO> getOrderByID(@Query("platform") String platform,
                                     @Query("version") String version,
                                     @Query("language_code") String languageCode,
                                     @Query("region_code") String regionCode,
                                     @Query("id") String id
    );

    @GET("categories?")
    Call<ArrayList<CategoriesWithProductsClass>> getCategories(@Query("platform") String platform,
                                     @Query("language_code") String languageCode,
                                     @Query("region_code") String regionCode
    );

    @GET("items?")
    Call<ArrayList<CategoriesWithProductsClass>> getCategoriesWithChosenProducts(
            @Query("version") String version,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("platform") String platform,
            @Query("lat") String lat,
            @Query("lon") String lon
    );

    @FormUrlEncoded
    @PUT("user?")
    Call<ResponseBody> putFCM(
            @Field("platform") String platform,
            @Field("login") String login,
            @Field("push") String push);

    @FormUrlEncoded
    @POST("promocode?")
    Call<PromoCodeClass> getPromoCode(
            @Field("promocode") String promoCode,
            @Field("platform") String platform,
            @Field("login") String login);




    @GET("news?")
    Call<NewNews> getNewsByID(
            @Query("version") String version,
            @Query("platform") String platform,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("id") String id
    );

    //need correct later
    @GET("item?")
    Call<Item> getIemByID(
            @Query("version") String version,
            @Query("platform") String platform,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("id") String id
    );


    @FormUrlEncoded
    @GET("converter?")
    Call<PriceConverterResponseClass> convertPrice(
            @Field("price") String price,
            @Field("currency") String currency
    );

    @GET("search?")
    Call<ArrayList<Item>> getAllItems(
            @Query("version") String version,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("platform") String platform,
            @Query("shop_id") String shopID
    );

    @GET("all-news?")
    Call<ArrayList<NewNews>> getNews(
            @Query("version") String version,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("platform") String platform
    );

    @GET("news-item?")
    Call<ArrayList<NewNews>> getItemByNewsID(
            @Query("id") String id,
            @Query("version") String version,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("platform") String platform
    );

    //not accessible items!!
    @FormUrlEncoded
    @POST("basket?")
    Call<BasketCheckPOJO> checkBasket(
            @Query("platform") String platform,
            @Query("shop_id") String shopID,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Field("items") String items
    );

    @GET("subcategories?")
    Call<ArrayList<ProductsByCategoryClass>> getProductsByCategory(
            @Query("version") String version,
            @Query("language_code") String languageCode,
            @Query("region_code") String regionCode,
            @Query("platform") String platform,
            @Query("shop_id") String shopID,
            @Query("id") String categoryID,
            @Query("lat") String lat,
            @Query("lon") String lon
    );

    @FormUrlEncoded
    @POST("order?")
    Call<ResponseBody> sendOrder(
            @Query("version") String version,
            @Query("region_code") String regionCode,
            @Query("language_code") String languageCode,
            @Query("platform") String platform,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("comment") String comment,
            @Query("start_total") String startTotal,
            @Query("discount") String discount,
            @Query("transaction_id") String transactionID,
            @Query("login") String login,
            @Query("address") String address,
            @Query("payment") String payment,
            @Query("floor") String floor,
            @Query("entrance") String entrance,
            @Query("end_total") String endTotal,
            @Query("phone") String phone,
            @Query("flat") String flat,
            @Query("delivery") String delivery,
            @Field("items") String items,
            @Query("delivery_price") String deliveryPrice,
            @Query("currency") String currency
    );

    @FormUrlEncoded
    @POST("chat?")
    Call<ChatSettingsClass> getChatSettings(
            @Field("login") String login
    );


    @FormUrlEncoded
    @POST("statistic?")
    Call<ResponseBody> sendStatistic(
            @Field("platform") String platform,
            @Field("login") String login,
            @Field("type") String type
           );

}