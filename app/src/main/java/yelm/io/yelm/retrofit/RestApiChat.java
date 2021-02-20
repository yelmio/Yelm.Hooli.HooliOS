package yelm.io.yelm.retrofit;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import yelm.io.yelm.chat.model.ChatHistoryClass;

public interface RestApiChat {

    String URL_API_MAIN = "https://chat.yelm.io/api/message/";
    String PLATFORM_NUMBER = "5fd33466e17963.29052139";

    @GET("all?")
    Call<ArrayList<ChatHistoryClass>> getChatHistory(
            @Query("platform") String Platform,
            @Query("room_id") String RoomID
    );
}