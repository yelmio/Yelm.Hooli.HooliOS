package yelm.io.raccoon.item;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.databinding.ModifierProductItemBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Modifier;

public class ProductModifierAdapter extends RecyclerView.Adapter<ProductModifierAdapter.ProductHolder> {

    private List<Modifier> modifiers;
    private Context context;
    private Listener listener;


    public interface Listener {
        void onChecked(Modifier product, boolean check);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }


    public ProductModifierAdapter(Context context, List<Modifier> modifiers) {
        this.context = context;
        this.modifiers = modifiers;
    }

    @NonNull
    @Override
    public ProductModifierAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(ModifierProductItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        Modifier current = modifiers.get(position);
        holder.binding.name.setText(current.getName());
        holder.binding.value.setText(String.format("+%s %s", current.getValue(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        holder.binding.selector.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (holder.binding.selector.isChecked()) {
                Log.d(Logging.debug, "isChecked: "+ current.getName());
                listener.onChecked(current, true);
            } else {
                Log.d(Logging.debug, "isNotChecked: "+ current.getName());
                listener.onChecked(current, false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modifiers == null ? 0 : modifiers.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private ModifierProductItemBinding binding;

        public ProductHolder(ModifierProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
