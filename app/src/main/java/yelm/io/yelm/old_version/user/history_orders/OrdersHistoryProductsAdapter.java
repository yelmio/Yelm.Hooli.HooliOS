package yelm.io.yelm.old_version.user.history_orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import yelm.io.yelm.R;

public class OrdersHistoryProductsAdapter extends RecyclerView.Adapter<OrdersHistoryProductsAdapter.OrderHistoryProductHolder> {

    private Context context;
    ArrayList<ProductItem> productItems;
    private String countProduct = " / 1 шт.";
    private String ruble;

    public OrdersHistoryProductsAdapter(Context context, ArrayList<ProductItem> productItems) {
        this.context = context;
        this.productItems = productItems;
        ruble = context.getResources().getString(R.string.ruble_sign);

    }

    @NonNull
    @Override
    public OrdersHistoryProductsAdapter.OrderHistoryProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.order_history_product_item, parent, false);
        return new OrdersHistoryProductsAdapter.OrderHistoryProductHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersHistoryProductsAdapter.OrderHistoryProductHolder holder, int position) {
        ProductItem current= productItems.get(position);

        holder.nameProduct.setText(current.getName());
        holder.costOneProduct.setText(new StringBuilder(ruble).
                append(' ').
                append(String.valueOf(current.getPrice())).
                append(countProduct));
        holder.costAllProduct.setText(new StringBuilder(ruble).
                append(' ').
                append(String.valueOf(current.getFullPrice())));
        holder.countProducts.setText(String.valueOf(current.getCount().toString()));
        Picasso.get().load(current.getImage()).noPlaceholder().into(holder.imageProduct);
    }

    @Override
    public int getItemCount() {
        return productItems.size();
    }

    public static class OrderHistoryProductHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        public TextView nameProduct, costOneProduct, costAllProduct, countProducts;


        public OrderHistoryProductHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            nameProduct = itemView.findViewById(R.id.nameProduct);
            costOneProduct = itemView.findViewById(R.id.costOneProduct);
            costAllProduct = itemView.findViewById(R.id.costAllProduct);
            countProducts = itemView.findViewById(R.id.countProducts);
        }
    }


}
