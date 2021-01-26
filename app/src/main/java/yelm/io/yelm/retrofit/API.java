package yelm.io.yelm.retrofit;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import yelm.io.yelm.fragments.catalog_fragment.model.CatalogClass;
import yelm.io.yelm.loader.model.PrimarySettingsClass;
import yelm.io.yelm.loader.model.UserLoginResponse;
import yelm.io.yelm.fragments.catalog_fragment.model.ProductsClass;
import yelm.io.yelm.fragments.catalog_fragment.model.StockClass;
import yelm.io.yelm.old_version.user.UserOrderClass;
import yelm.io.yelm.fragments.main_fragment.model.ArticleClass;
import yelm.io.yelm.fragments.main_fragment.model.NewsClassOLD;
import yelm.io.yelm.old_version.maps.ShopClass;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClass;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClassBasic;

public interface API {

    public static String URL_API_MAIN = "https://api.yelm.io/";
    public static String PLATFORM = "platform=5f72f80cb2b0a7.27315146";
    public static String M_API = "m_api.php?";

    //dynamic api
    public static String URL_API_NEWS = "&get_all_news=true";
    public static String URL_API_SETTINGS = "&settings=true";

    public static String URL_API_CARD_TYPE_MAIN = "&get_card=true&type=main";
    public static String URL_API_CARD_TYPE_CATALOG = "&get_card=true&type=catalog";
    public static String URL_API_ANY_PRODUCTS = "&get_any_items=true";
    public static String URL_API_ALL_CATALOG = "&get_all_catalog=true";
    public static String URL_API_POPULAR_PRODUCTS = "&get_main_items=true";
    public static String URL_API_COMPANY_INFO = "&get_all_company=true";
    public static String URL_API_ALL_COMPANY_INFO_BASIC = "&get_all_company_basis=true";
    public static String URL_API_GET_SHOPS = "&get_shops=true";
    public static String URL_API_GET_PRODUCTS_BY_ID = "&get_all_items=";

    @GET
    Call<ArrayList<ProductsClass>> getProductsByID(@Url String url);

    @GET
    Call<ArrayList<PrimarySettingsClass>> getSettings(@Url String url);

    @GET
    Call<ArrayList<ArticleClass>> getArticles(@Url String url);

    @GET()
    Call<ArrayList<NewsClassOLD>> getNews(@Url String url);

    @GET()
    Call<ArrayList<StockClass>> getStock(@Url String url);

    @GET()
    Call<ArrayList<CatalogClass>> getCatalog(@Url String url);

    @GET()
    Call<ArrayList<ProductsClass>> getPopularProducts(@Url String url);

    @GET()
    Call<ArrayList<ProductsClass>> getProducts(@Url String url);

    @GET()
    Call<ArrayList<CompanyClass>> getCompany(@Url String url);

    @GET()
    Call<ArrayList<CompanyClassBasic>> getCompanyBasis(@Url String url);

    @GET()
    Call<ArrayList<ShopClass>> getShops(@Url String url);

    //static api
    public static String URL_API_POST_LOG = M_API + PLATFORM + "&log_android=true";
    public static String URL_API_CREATE_USER = "user.php?create=1&" + PLATFORM;
    public static String URL_API_GET_USER_ORDER_HISTORY = "api.php?get_user_orders&" + PLATFORM;
    public static String URL_API_SEND_FCM_TOKEN = "user.php";

    @FormUrlEncoded
    @POST(URL_API_POST_LOG)
    Call<String> postLog(
            @Field("log") String log,
            @Field("user") String user,
            @Field("more") String more
    );

    @FormUrlEncoded
    @POST(URL_API_SEND_FCM_TOKEN)
    Call<String> postFCM(
            @Field("update_token_android") String login,
            @Field("token") String token
    );

    @GET(URL_API_GET_USER_ORDER_HISTORY)
    Call<ArrayList<UserOrderClass>> getUserOrderHistory(
            @Query("user") String userId
    );

    @FormUrlEncoded
    @POST(URL_API_CREATE_USER)
    Call<UserLoginResponse> createUser(
            @Field("login") String login
    );


}
