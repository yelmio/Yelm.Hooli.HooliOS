package yelm.io.yelm.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import yelm.io.yelm.databinding.SquareCategoryItemBinding;
import yelm.io.yelm.main.categories.CategoriesPOJO;
import yelm.io.yelm.main.model.CategoriesWithProductsClass;

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
