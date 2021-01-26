package yelm.io.yelm.old_version.user.history_orders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.old_version.user.UserOrderClass;

public class OrdersHistoryActivity extends AppCompatActivity {

    private OrdersHistoryAdapter ordersHistoryAdapter;
    ArrayList<UserOrderClass> userOrdersHistory = new ArrayList<>();

    RecyclerView recycler_orders_history;
    TextView historyEmpty;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_history);
        recycler_orders_history = findViewById(R.id.recycler_orders_history);
        historyEmpty = findViewById(R.id.historyEmpty);
        recycler_orders_history.setLayoutManager(new LinearLayoutManager(this));
        RetrofitClient.
                getClient(API.URL_API_MAIN)
                .create(API.class)
                .getUserOrderHistory(userID)
                .enqueue(new Callback<ArrayList<UserOrderClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<UserOrderClass>> call, Response<ArrayList<UserOrderClass>> response) {
                        if (response.isSuccessful()) {
                            userOrdersHistory = response.body();
                            if (userOrdersHistory.size() != 0) {
                                historyEmpty.setVisibility(View.GONE);
                            }
                            Log.d("AlexDebug", "userOrdersHistory.size(): " + userOrdersHistory.size());
                            ordersHistoryAdapter = new OrdersHistoryAdapter(OrdersHistoryActivity.this, userOrdersHistory);
                            recycler_orders_history.setAdapter(ordersHistoryAdapter);
                        } else {
                            Log.d("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<UserOrderClass>> call, Throwable t) {
                        Log.d("AlexDebug", "Failer: " + t.toString());
                    }
                });
    }
}