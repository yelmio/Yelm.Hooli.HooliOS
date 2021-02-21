package yelm.io.yelm.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yelm.io.yelm.R;
import yelm.io.yelm.constants.Logging;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.databinding.NewMenuProductItemBinding;
import yelm.io.yelm.databinding.SquareCategoryItemBinding;
import yelm.io.yelm.item.ItemActivity;
import yelm.io.yelm.item.ProductModifierAdapter;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.CategoriesWithProductsClass;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.main.model.Modifier;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ProductHolder> {
    private Context context;
    private List<CategoriesWithProductsClass> categories;

    public CategoriesAdapter(Context context, List<CategoriesWithProductsClass> categories) {
        this.context = context;
        this.categories = categories;
    }


    @NonNull
    @Override
    public CategoriesAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(SquareCategoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoriesAdapter.ProductHolder holder, final int position) {
        CategoriesWithProductsClass current = categories.get(position);

        holder.binding.title.setText(current.getName());

//        Picasso.get()
//                .load(imageUrl)
//                .noPlaceholder()
//                .centerCrop()
//                .resize(600, 0)
//                .into(holder.binding.image);
    }


    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private SquareCategoryItemBinding binding;

        public ProductHolder(SquareCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
