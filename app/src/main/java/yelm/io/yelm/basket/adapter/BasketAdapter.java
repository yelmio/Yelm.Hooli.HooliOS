package yelm.io.yelm.basket.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.databinding.BasketCartItemBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketHolder> {

    private Context context;
    private List<BasketCart> basket;

    public BasketAdapter(Context context, List<BasketCart> basket) {
        this.context = context;
        this.basket = basket;
    }

    @Override
    public void onBindViewHolder(@NonNull final BasketAdapter.BasketHolder holder, final int position) {
        BasketCart current = basket.get(position);

        holder.binding.description.setText(current.name);
        holder.binding.countProducts.setText(current.count);
        BigDecimal weight = new BigDecimal(current.count).multiply(new BigDecimal(current.quantityType));
        holder.binding.weight.setText(String.format("%s %s", weight.toString(), current.type));

        if (current.modifier.size() != 0) {
            holder.binding.modifiers.setVisibility(View.VISIBLE);
            StringBuilder modifiers = getModifiers(current.modifier);
            holder.binding.modifiers.setText(modifiers.toString());
        }

        Picasso.get()
                .load(current.imageUrl)
                .noPlaceholder()
                .centerCrop()
                .resize(300, 300)
                .into(holder.binding.imageHolder);

        BigDecimal currentStartFinal = new BigDecimal(current.finalPrice);
        for (Modifier modifier : current.modifier) {
            currentStartFinal = currentStartFinal.add(new BigDecimal(modifier.getValue()));
        }
        currentStartFinal = currentStartFinal.multiply(new BigDecimal(current.count));
        holder.binding.priceFinal.setText(String.format("%s %s", currentStartFinal, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        holder.binding.addProduct.setOnClickListener(view -> {
            BigDecimal tempBD = new BigDecimal(current.count);
            current.count = tempBD.add(new BigDecimal("1")).toString();
            Common.basketCartRepository.updateBasketCart(current);
        });

        holder.binding.removeProduct.setOnClickListener(view -> {
            if (current.count.equals("1")) {
                Common.basketCartRepository.deleteBasketCart(current);
            } else {
                BigDecimal tempBD = new BigDecimal(current.count);
                current.count = tempBD.subtract(new BigDecimal("1")).toString();
                Common.basketCartRepository.updateBasketCart(current);
            }
        });

        if (new BigDecimal(current.count).compareTo(new BigDecimal(current.quantity)) == 0) {
            holder.binding.addProduct.setEnabled(false);
        }

        if (new BigDecimal(current.count).compareTo(new BigDecimal(current.quantity)) > 0) {
            holder.binding.textProductIsOver.setVisibility(View.VISIBLE);
            holder.binding.addProduct.setEnabled(false);
            holder.binding.textProductIsOver.setText(String.format("%s: %s %s", context.getText(R.string.basketActivityProductIsOver), current.quantity, context.getText(R.string.basketActivityPC)));
        }
    }

    private StringBuilder getModifiers(List<Modifier> modifiersList) {
        StringBuilder modifiersString = new StringBuilder();
        for (Modifier modifier : modifiersList) {
            modifiersString = modifiersString.length() > 0 ?
                    modifiersString.append('\n').append(modifier.getName()) : modifiersString.append(modifier.getName());
        }
        return modifiersString;
    }

    @NonNull
    @Override
    public BasketAdapter.BasketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BasketAdapter.BasketHolder(BasketCartItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return basket == null ? 0 : basket.size();
    }

    public static class BasketHolder extends RecyclerView.ViewHolder {
        private BasketCartItemBinding binding;

        public BasketHolder(BasketCartItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
