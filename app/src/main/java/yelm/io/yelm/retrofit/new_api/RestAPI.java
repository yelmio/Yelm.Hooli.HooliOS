package yelm.io.yelm.retrofit.new_api;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import yelm.io.yelm.by_category.ProductsByCategoryClass;
import yelm.io.yelm.basket.model.BasketCheckResponse;
import yelm.io.yelm.loader.model.ApplicationSettings;
import yelm.io.yelm.loader.model.UserLoginResponse;
import yelm.io.yelm.main.model.CatalogsWithProductsClass;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.main.model.NewNews;

public interface RestAPI {

    String URL_API_MAIN = "https://dev.yelm.io/api/mobile/";
    String PLATFORM_NUMBER = "5fd33466e17963.29052139";

    @FormUrlEncoded
    @POST("user?")
    Call<UserLoginResponse> createUser(@Field("platform") String Platform,
                                       @Field("user_info") String userInfo);

    @GET("application?")
    Call<ApplicationSettings> getAppSettings(@Query("platform") String Platform,
                                             @Query("language_code") String LanguageCode,
                                             @Query("region_code") String RegionCode,
                                             @Query("lat") String LAT,
                                             @Query("lon") String LON
    );

    @GET("items?")
    Call<ArrayList<CatalogsWithProductsClass>> getCategoriesWithProducts(
            @Query("version") String Version,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("platform") String Platform,
            @Query("lat") String LAT,
            @Query("lon") String LON
    );

    @GET("search?")
    Call<ArrayList<Item>> getAllItems(
            @Query("version") String Version,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("platform") String Platform,
            @Query("shop_id") String ShopID
    );

    @GET("all-news?")
    Call<ArrayList<NewNews>> getNews(
            @Query("version") String Version,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("platform") String Platform
    );

    @GET("news-item?")
    Call<ArrayList<NewNews>> getItemByNewsID(
            @Query("id") String ID,
            @Query("version") String Version,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("platform") String Platform
    );


    //not accessible items!!
    @FormUrlEncoded
    @POST("basket?")
    Call<BasketCheckResponse> checkBasket(
            @Query("platform") String Platform,
            @Query("shop_id") String ShopID,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("lat") String LAT,
            @Query("lon") String LON,
            @Field("items") String Items
            );

    @GET("subcategories?")
    Call<ArrayList<ProductsByCategoryClass>> getProductsByCategory(
            @Query("version") String Version,
            @Query("language_code") String LanguageCode,
            @Query("region_code") String RegionCode,
            @Query("platform") String Platform,
            @Query("shop_id") String ShopID,
            @Query("id") String CategoryID,
            @Query("lat") String LAT,
            @Query("lon") String LON
    );

}
