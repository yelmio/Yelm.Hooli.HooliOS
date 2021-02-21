package yelm.io.yelm.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yelm.io.yelm.databinding.SquareCategoryItemBinding;
import yelm.io.yelm.main.model.CategoriesWithProductsClass;

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
