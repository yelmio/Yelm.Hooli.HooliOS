package yelm.io.raccoon.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import yelm.io.raccoon.databinding.SpecificationsProductItemBinding;
import yelm.io.raccoon.main.model.Specification;

public class ProductSpecificationsAdapter extends RecyclerView.Adapter<ProductSpecificationsAdapter.ProductHolder> {

    private List<Specification> specifications;
    private Context context;

    public ProductSpecificationsAdapter(Context context, List<Specification> specifications) {
        this.context = context;
        this.specifications = specifications;
    }

    @NonNull
    @Override
    public ProductSpecificationsAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(SpecificationsProductItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        Specification current = specifications.get(position);
        holder.binding.name.setText(current.getName());
        holder.binding.value.setText(current.getValue());





    }

    @Override
    public int getItemCount() {
        return specifications == null ? 0 : specifications.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private SpecificationsProductItemBinding binding;

        public ProductHolder(SpecificationsProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
