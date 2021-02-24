package yelm.io.raccoon.order.user_order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import yelm.io.raccoon.databinding.ProductItemOrderBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Item;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductHolder> {

    private Context context;
    private List<Item> products;
    private HashMap<String, String> itemsMap;

    public OrderProductAdapter(Context context, List<Item> products, HashMap<String, String> productsCount) {
        this.context = context;
        this.products = products;
        this.itemsMap = productsCount;
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderProductAdapter.ProductHolder holder, final int position) {
        Item current = products.get(position);
        String currentCount = itemsMap.get(current.getId());
        holder.binding.rating.setRating(Float.parseFloat(current.getRating()));
        holder.binding.rating.setRating(Float.parseFloat(current.getRating()));

        BigDecimal weight = new BigDecimal(currentCount).multiply(new BigDecimal(current.getUnitType()));
        holder.binding.weight.setText(String.format("%s / %s", weight, current.getType()));

        if (!current.getDiscount().equals("0")) {
            holder.binding.discountProcent.setText(String.format("- %s %%", current.getDiscount()));
            holder.binding.discountProcent.setVisibility(View.VISIBLE);
        }

        BigDecimal bd = new BigDecimal(current.getPrice());
        if (!current.getDiscount().equals("0")) {
            bd = new BigDecimal(current.getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(current.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(current.getPrice()).subtract(bd);
            //trim zeros if after comma there are only zeros: 45.00 -> 45
            if (bd.compareTo(new BigDecimal(String.valueOf(bd.setScale(0, BigDecimal.ROUND_HALF_UP)))) == 0) {
                bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            }
        }

        bd = bd.multiply(new BigDecimal(currentCount));

        holder.binding.price.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        holder.binding.description.setText(current.getName());
//
//        holder.binding.weight.setText(String.format("%s / %s", current.getUnitType(), current.getType()));
//
//        holder.binding.containerProduct.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ItemActivity.class);
//            intent.putExtra("item", current);
//            context.startActivity(intent);
//        });
//
        Picasso.get()
                .load(current.getPreviewImage())
                .noPlaceholder()
                .centerCrop()
                .resize(300, 300)
                .into(holder.binding.imageHolder);
    }

    @NonNull
    @Override
    public OrderProductAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderProductAdapter.ProductHolder(ProductItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return products == null ? 0 : products.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private ProductItemOrderBinding binding;

        public ProductHolder(ProductItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
