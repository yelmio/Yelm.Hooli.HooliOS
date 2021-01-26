package yelm.io.yelm.fragments.basket_fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;

import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.R;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.loader.controller.LoaderActivity;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.OrderHolder> {

    private Context context;
    private List<Cart> products;


    private Listener listener;

    public interface Listener {
        void onClick(int id, Cart product);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public BasketAdapter(Context context, List<Cart> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public BasketAdapter.OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new BasketAdapter.OrderHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketAdapter.OrderHolder holder, final int position) {
        Cart currentProduct = products.get(position);
        if (currentProduct.isPromo) {
            holder.cardViewAdd.setEnabled(false);
        }
        Picasso.get().
                load(currentProduct.imageUrl).
                noPlaceholder().
                into(holder.imageProduct);

        holder.nameProduct.setText(currentProduct.name);
        holder.countProducts.setText(String.valueOf(currentProduct.count));

        holder.costOneProduct
                .setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                        .append(' ')
                        .append(currentProduct.price)
                        .append(" / ")
                        .append(currentProduct.quantity)
                        .append(' ')
                        .append(currentProduct.type));

        holder.costAllProduct
                .setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                        .append(' ')
                        .append(new BigDecimal(currentProduct.price).multiply(new BigDecimal(currentProduct.count))));


        holder.cardViewAdd.setOnClickListener(view -> {
            Cart tempProduct = products.get(position);
            BigDecimal temp = new BigDecimal(tempProduct.count);
            tempProduct.count = temp.add(new BigDecimal("1")).toString();
            Common.cartRepository.updateCart(tempProduct);
        });

        holder.cardViewRemove.setOnClickListener(view -> {
            Cart tempProduct = products.get(position);
            if (tempProduct.count.equals("1")) {
                Common.cartRepository.deleteCartItem(tempProduct);
            } else {
                BigDecimal temp = new BigDecimal(tempProduct.count);
                tempProduct.count = temp.subtract(new BigDecimal("1")).toString();
                Common.cartRepository.updateCart(tempProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class OrderHolder extends RecyclerView.ViewHolder {
        public TextView nameProduct, costOneProduct, costAllProduct, countProducts;
        public ImageView imageProduct;

        public ImageButton cardViewAdd, cardViewRemove;

        public OrderHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            nameProduct = itemView.findViewById(R.id.nameProduct);
            costOneProduct = itemView.findViewById(R.id.costOneProduct);
            costAllProduct = itemView.findViewById(R.id.costAllProduct);
            countProducts = itemView.findViewById(R.id.countProducts);

            cardViewAdd = itemView.findViewById(R.id.cardViewAdd);
            cardViewRemove = itemView.findViewById(R.id.cardViewRemove);
        }
    }
}
