package yelm.io.raccoon.search;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import yelm.io.raccoon.item.ItemActivity;
import yelm.io.raccoon.main.model.Item;
import yelm.io.raccoon.rest.query.Statistic;
import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.databinding.ProductItemSearcheableBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductHolder> implements Filterable {

    private Context context;
    private List<Item> products;
    private List<Item> productsSort;

    public SearchProductAdapter(Context context, List<Item> products) {
        this.context = context;
        this.products = products;
        productsSort = new ArrayList<>(products);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        //run back
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Item> filtered = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filtered.addAll(products);
            } else {
                for (Item product : products) {
                    if (product.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        Log.d(Logging.debug, "Filter - request string: " + product.getName().toLowerCase());
                        filtered.add(product);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered;
            return filterResults;
        }

        //run ui
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            productsSort.clear();
            productsSort.addAll((Collection<? extends Item>) filterResults.values);
            notifyDataSetChanged();
        }
    };


    @Override
    public void onBindViewHolder(@NonNull final SearchProductAdapter.ProductHolder holder, final int position) {
        Item current = productsSort.get(position);
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

        if (!current.getDiscount().equals("0")){
            holder.binding.discountProcent.setText(String.format("- %s %%", current.getDiscount()));
            holder.binding.discountProcent.setVisibility(View.VISIBLE);
        }

        holder.binding.priceFinal.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        holder.binding.description.setText(current.getName());

        holder.binding.weight.setText(String.format("%s / %s", current.getUnitType(), current.getType()));

        holder.binding.containerProduct.setOnClickListener(v -> {
            Statistic.sendStatistic("open_item_search");
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("item", current);
            context.startActivity(intent);
        });

        Picasso.get()
                .load(current.getPreviewImage())
                .noPlaceholder()
                .centerCrop()
                .resize(300, 300)
                .into(holder.binding.imageHolder);
    }

    @NonNull
    @Override
    public SearchProductAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchProductAdapter.ProductHolder(ProductItemSearcheableBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return productsSort == null ? 0 : productsSort.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private ProductItemSearcheableBinding binding;

        public ProductHolder(ProductItemSearcheableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
