package yelm.io.yelm.old_version.user.history_orders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import yelm.io.yelm.R;
import yelm.io.yelm.old_version.user.UserOrderClass;

public class OrdersHistoryAdapter extends RecyclerView.Adapter<OrdersHistoryAdapter.OrderHistoryHolder> {

    private List<UserOrderClass> userOrdersHistory;
    private Context context;

    private OrdersHistoryProductsAdapter ordersHistoryProductsAdapter;

    public OrdersHistoryAdapter(Context context, List<UserOrderClass> userOrdersHistory) {
        this.context = context;
        this.userOrdersHistory = userOrdersHistory;
    }

    @NonNull
    @Override
    public OrdersHistoryAdapter.OrderHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.order_history_item, parent, false);
        return new OrderHistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrdersHistoryAdapter.OrderHistoryHolder holder, int position) {
        ArrayList<ProductItem> productItems = new ArrayList<>();
        String date = userOrdersHistory.get(position).getCreateDate();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            cal.setTime(calFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        holder.date.setText(calFormat.format(cal.getTime()));

        JSONObject json;
        try {
            json = new JSONObject(userOrdersHistory.get(position).getOrderItem());
            holder.address.setText(new StringBuilder().append("Адрес - ").append(json.get("Adress").toString()).toString());
            JSONArray jsonArray = json.getJSONArray("Items");
            for (int i = 0; i < jsonArray.length(); i++) {
                json = (JSONObject) jsonArray.get(i);
                productItems.add(new ProductItem(
                        Integer.valueOf(json.get("Item").toString()),
                        json.get("Name").toString(),
                        json.get("Type").toString(),
                        Integer.valueOf(json.get("Price").toString()),
                        Integer.valueOf(json.get("Count").toString()),
                        Integer.valueOf(json.get("FullPrice").toString()),
                        json.get("Image").toString()
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AlexDebug", "exception: " + e.toString());
        }
        Integer fullPrice = 0;
        Integer countProducts = 0;
        for (int i = 0; i < productItems.size(); i++) {
            fullPrice += productItems.get(i).getFullPrice();
            countProducts += productItems.get(i).getCount();
        }

        ordersHistoryProductsAdapter = new OrdersHistoryProductsAdapter(context, productItems);
        holder.recycler_history_orders.setLayoutManager(new LinearLayoutManager(context));
        holder.recycler_history_orders.setAdapter(ordersHistoryProductsAdapter);

        holder.hideShowItemsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.recycler_history_orders.getVisibility() == View.VISIBLE) {
                    holder.recycler_history_orders.setVisibility(View.GONE);
                } else {
                    holder.recycler_history_orders.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.fullPrice.setText(new StringBuilder().append("Сумма - ").append(fullPrice).toString());
        holder.countProducts.setText(new StringBuilder().append("Всего товаров - ").append(countProducts).toString());
    }

    @Override
    public int getItemCount() {
        return userOrdersHistory.size();
    }


    public static class OrderHistoryHolder extends RecyclerView.ViewHolder {
        public TextView date, fullPrice, countProducts, address;
        RecyclerView recycler_history_orders;
        LinearLayout hideShowItemsLayout;

        public OrderHistoryHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            fullPrice = itemView.findViewById(R.id.fullPrice);
            address = itemView.findViewById(R.id.userAddress);
            hideShowItemsLayout = itemView.findViewById(R.id.hideShowItemsLayout);
            countProducts = itemView.findViewById(R.id.countProducts);
            recycler_history_orders = itemView.findViewById(R.id.recycler_history_orders);
        }
    }
}
