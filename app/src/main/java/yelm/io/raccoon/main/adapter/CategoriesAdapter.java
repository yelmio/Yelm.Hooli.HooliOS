package yelm.io.raccoon.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import yelm.io.raccoon.by_category.ProductsByCategoriesActivity;
import yelm.io.raccoon.databinding.SquareCategoryItemBinding;
import yelm.io.raccoon.main.categories.CategoriesPOJO;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ProductHolder> {
    private Context context;
    private List<CategoriesPOJO> categories;

    public CategoriesAdapter(Context context, List<CategoriesPOJO> categories) {
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
        CategoriesPOJO current = categories.get(position);
        holder.binding.title.setText(current.getName());
        Picasso.get()
                .load(current.getImage())
                .noPlaceholder()
                .centerCrop()
                .resize(400, 0)
                .into(holder.binding.image);
        holder.binding.image.setOnClickListener(v->{
            Intent intent = new Intent(context, ProductsByCategoriesActivity.class);
            intent.putExtra("catalogID", current.getId());
            intent.putExtra("catalogName", current.getName());
            context.startActivity(intent);
        });
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
