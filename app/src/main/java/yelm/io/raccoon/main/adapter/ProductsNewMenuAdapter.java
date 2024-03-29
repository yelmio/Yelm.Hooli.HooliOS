package yelm.io.raccoon.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yelm.io.raccoon.item.ItemActivity;
import yelm.io.raccoon.rest.query.RestMethods;
import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.R;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.databinding.NewMenuProductItemBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Item;
import yelm.io.raccoon.main.model.Modifier;
import yelm.io.raccoon.item.ProductModifierAdapter;

public class ProductsNewMenuAdapter extends RecyclerView.Adapter<ProductsNewMenuAdapter.ProductHolder> implements Filterable {
    private Context context;
    private List<Item> products;
    private List<Item> productsSort;

    public ProductsNewMenuAdapter(Context context, List<Item> products) {
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


    @NonNull
    @Override
    public ProductsNewMenuAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(NewMenuProductItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductsNewMenuAdapter.ProductHolder holder, final int position) {
        Item current = productsSort.get(position);

        //set count of item in basket into layout
        List<BasketCart> listBasketCartByItemID = Common.basketCartRepository.getListBasketCartByItemID(current.getId());
        if (listBasketCartByItemID != null && listBasketCartByItemID.size() != 0) {
            BigInteger countOfAllProducts = new BigInteger("0");
            for (BasketCart basketCart : listBasketCartByItemID) {
                countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
            }

            holder.binding.countItemInCart.setText(String.format("%s", countOfAllProducts));
            holder.binding.removeProduct.setVisibility(View.VISIBLE);
            holder.binding.countItemsLayout.setVisibility(View.VISIBLE);
        }

        holder.binding.cardProduct.setOnClickListener(v -> {
            RestMethods.sendStatistic("open_item");
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("item", current);
            context.startActivity(intent);
        });

        //calculate final price depending on the discount
        BigDecimal bd = new BigDecimal(current.getPrice());
        if (current.getDiscount().equals("0")) {
            holder.binding.priceFinal.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.binding.priceStart.setVisibility(View.GONE);
        } else {
            holder.binding.discountProcent.setVisibility(View.VISIBLE);
            holder.binding.discountProcent.setText(String.format("- %s %%", current.getDiscount()));
            bd = new BigDecimal(current.getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(current.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(current.getPrice()).subtract(bd);
            //trim zeros if after comma there are only zeros: 45.00 -> 45
            if (bd.compareTo(new BigDecimal(String.valueOf(bd.setScale(0, BigDecimal.ROUND_HALF_UP)))) == 0) {
                bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            }
            holder.binding.priceFinal.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.binding.priceStart.setText(String.format("%s %s", current.getPrice(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.binding.priceStart.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.binding.description.setText(current.getName());
        holder.binding.weight.setText(String.format("%s %s", current.getUnitType(), current.getType()));

        //add product into basket
        BigDecimal finalBd = bd;
        holder.binding.addProduct.setOnClickListener(v -> {
            if (current.getModifier().size() != 0) {
                showBottomSheetDialog(holder, current, finalBd);
            } else {
                holder.binding.countItemsLayout.setVisibility(View.VISIBLE);
                List<BasketCart> listCartsByID = Common.basketCartRepository.getListBasketCartByItemID(current.getId());
                if (listCartsByID != null && listCartsByID.size() != 0) {
                    BigDecimal countOfAllProducts = new BigDecimal("0");
                    for (BasketCart basketCart : listCartsByID) {
                        countOfAllProducts = countOfAllProducts.add(new BigDecimal(basketCart.count));
                    }
                    if (countOfAllProducts.compareTo(new BigDecimal(listCartsByID.get(0).quantity)) >= 0) {
                        showToast((String) context.getText(R.string.productsNotAvailable) +
                                " " + listCartsByID.get(0).quantity +" "+(String) context.getText(R.string.basketActivityPC));
                        return;
                    }
                    for (BasketCart basketCart : listCartsByID) {
                        if (basketCart.modifier.equals(current.getModifier())) {
                            basketCart.count = new BigDecimal(basketCart.count).add(new BigDecimal("1")).toString();
                            holder.binding.countItemInCart.setText(String.format("%s", countOfAllProducts.add(new BigDecimal("1"))));
                            Common.basketCartRepository.updateBasketCart(basketCart);
                            Log.d(Logging.debug, "Method add BasketCart to Basket. No modifiers - listCartsByID !=null:  " + basketCart.toString());
                            return;
                        }
                    }
                }
                holder.binding.removeProduct.setVisibility(View.VISIBLE);
                holder.binding.countItemInCart.setText("1");
                BasketCart cartItem = new BasketCart();
                cartItem.itemID = current.getId();
                cartItem.name = current.getName();
                cartItem.discount = current.getDiscount();
                cartItem.startPrice = current.getPrice();
                cartItem.finalPrice = finalBd.toString();
                cartItem.type = current.getType();
                cartItem.count = "1";
                cartItem.imageUrl = current.getPreviewImage();
                cartItem.quantity = current.getQuantity();
                cartItem.discount = current.getDiscount();
                cartItem.modifier = current.getModifier();
                cartItem.isPromo = false;
                cartItem.isExist = true;
                cartItem.quantityType = current.getUnitType();
                Common.basketCartRepository.insertToBasketCart(cartItem);
                Log.d(Logging.debug, "Method add BasketCart to Basket. No modifiers - listCartsByID == null:  " + cartItem.toString());
            }
        });

        //remove product from basket
        holder.binding.removeProduct.setOnClickListener(v -> {
            List<BasketCart> listCartsByID = Common.basketCartRepository.getListBasketCartByItemID(current.getId());
            if (listCartsByID != null && listCartsByID.size() != 0) {
                if (listCartsByID.size() == 1) {
                    BasketCart cartItem = listCartsByID.get(0);
                    BigInteger countOfProduct = new BigInteger(cartItem.count);
                    if (countOfProduct.equals(new BigInteger("1"))) {
                        holder.binding.countItemsLayout.setVisibility(View.GONE);
                        holder.binding.removeProduct.setVisibility(View.GONE);
                        Common.basketCartRepository.deleteBasketCart(cartItem);
                    } else {
                        countOfProduct = countOfProduct.subtract(new BigInteger("1"));
                        cartItem.count = countOfProduct.toString();
                        holder.binding.countItemInCart.setText(cartItem.count);
                        Common.basketCartRepository.updateBasketCart(cartItem);
                    }
                } else {
                    BigInteger countOfAllProducts = new BigInteger("0");
                    for (BasketCart basketCart : listCartsByID) {
                        countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
                    }
                    BasketCart cartItem = listCartsByID.get(listCartsByID.size() - 1);
                    BigInteger countOfProduct = new BigInteger(cartItem.count);
                    if (countOfProduct.equals(new BigInteger("1"))) {
                        Common.basketCartRepository.deleteBasketCart(cartItem);
                    } else {
                        countOfProduct = countOfProduct.subtract(new BigInteger("1"));
                        cartItem.count = countOfProduct.toString();
                        holder.binding.countItemInCart.setText(String.format("%s", countOfAllProducts.subtract(new BigInteger("1"))));
                        Common.basketCartRepository.updateBasketCart(cartItem);
                    }
                }
            }
        });

        holder.binding.image.setAlpha(0f);
        String imageUrl = current.getPreviewImage();
        Picasso.get()
                .load(imageUrl)
                .noPlaceholder()
                .centerCrop()
                .resize(300, 0)
                .into(holder.binding.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.binding.image.animate().setDuration(300).alpha(1f).start();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    //if product have modifiers then we show bottomSheetDialog for its choice
    private void showBottomSheetDialog(ProductsNewMenuAdapter.ProductHolder holder, Item current, BigDecimal bd) {
        ProductModifierAdapter productModifierAdapter = new ProductModifierAdapter(context, current.getModifier());
        BottomSheetDialog productModifierSelectionBottomSheet = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.product_modifier_bottom_sheep_dialog, null);

        ImageView imageView = view.findViewById(R.id.image);
        Picasso.get()
                .load(current.getImages().get(0))
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(imageView);

        TextView textName = view.findViewById(R.id.name);
        textName.setText(current.getName());
        TextView textCost = view.findViewById(R.id.cost);
        textCost.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        TextView countProducts = view.findViewById(R.id.countProducts);
        countProducts.setText("1");

        //control the count of modifiers
        HashMap<String, String> modifiers = new HashMap<>();
        productModifierAdapter.setListener((modifier, check) -> {
            if (check) {
                modifiers.put(modifier.getName(), modifier.getValue());
            } else {
                modifiers.remove(modifier.getName());
            }
            Log.d(Logging.debug, "modifiers: " + modifiers.toString());

            BigDecimal costCurrent = new BigDecimal(bd.toString());
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
            }
            costCurrent = costCurrent.multiply(new BigDecimal(countProducts.getText().toString()));
            textCost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        });

        view.findViewById(R.id.addProduct).setOnClickListener(v -> {
            BigInteger counter = new BigInteger(countProducts.getText().toString()).add(new BigInteger("1"));
            countProducts.setText(String.format("%s", counter.toString()));

            BigDecimal costCurrent = new BigDecimal(bd.toString());
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
            }
            costCurrent = costCurrent.multiply(new BigDecimal(counter.toString()));
            textCost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        });

        view.findViewById(R.id.removeProduct).setOnClickListener(v -> {
            if (!countProducts.getText().toString().equals("1")) {
                BigInteger counter = new BigInteger(countProducts.getText().toString()).subtract(new BigInteger("1"));
                countProducts.setText(String.format("%s", counter.toString()));

                BigDecimal costCurrent = new BigDecimal(bd.toString());
                for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                    costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
                }
                costCurrent = costCurrent.multiply(new BigDecimal(counter.toString()));
                textCost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            }
        });

        RecyclerView recyclerModifier = view.findViewById(R.id.recyclerModifiers);
        recyclerModifier.setLayoutManager(new LinearLayoutManager(context));
        recyclerModifier.setAdapter(productModifierAdapter);

        view.findViewById(R.id.addToCart).setOnClickListener(v -> {

            holder.binding.countItemsLayout.setVisibility(View.VISIBLE);
            holder.binding.removeProduct.setVisibility(View.VISIBLE);

            List<BasketCart> listBasketCartByItemID = Common.basketCartRepository.getListBasketCartByItemID(current.getId());

            BigInteger countOfAllProducts = new BigInteger("0");
            List<Modifier> listModifiers = new ArrayList<>();
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                listModifiers.add(new Modifier(modifierEntry.getKey(), modifierEntry.getValue()));
            }
            if (listBasketCartByItemID != null && listBasketCartByItemID.size() != 0) {
                for (BasketCart basketCart : listBasketCartByItemID) {
                    countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
                }

                for (BasketCart basketCart : listBasketCartByItemID) {
                    if (basketCart.modifier.equals(listModifiers)) {
                        BigInteger countOfProductsToShow = new BigInteger(countProducts.getText().toString()).add(countOfAllProducts);
                        holder.binding.countItemInCart.setText(String.format("%s", countOfProductsToShow.toString()));
                        basketCart.count = new BigInteger(basketCart.count).add(new BigInteger(countProducts.getText().toString())).toString();
                        Common.basketCartRepository.updateBasketCart(basketCart);
                        Log.d(Logging.debug, "Method update Product in Basket - found similar!:  " + basketCart.toString());
                        productModifierSelectionBottomSheet.dismiss();
                        return;
                    }
                }
            }

            BasketCart cartItem = new BasketCart();
            cartItem.itemID = current.getId();
            cartItem.name = current.getName();
            cartItem.discount = current.getDiscount();
            cartItem.startPrice = current.getPrice();
            cartItem.finalPrice = bd.toString();
            cartItem.type = current.getType();
            cartItem.count = countProducts.getText().toString();
            cartItem.imageUrl = current.getPreviewImage();
            cartItem.quantity = current.getQuantity();
            cartItem.discount = current.getDiscount();
            cartItem.modifier = listModifiers;
            cartItem.isPromo = false;
            cartItem.isExist = true;
            cartItem.quantityType = current.getUnitType();
            Common.basketCartRepository.insertToBasketCart(cartItem);
            if (listBasketCartByItemID == null) {
                holder.binding.countItemInCart.setText(cartItem.count);
            } else {
                BigInteger countOfProductsToShow = new BigInteger(cartItem.count).add(countOfAllProducts);
                holder.binding.countItemInCart.setText(String.format("%s", countOfProductsToShow.toString()));
            }
            Log.d(Logging.debug, "Method add Product to Basket - not found similar!:  " + cartItem.toString());
            productModifierSelectionBottomSheet.dismiss();
        });

        productModifierSelectionBottomSheet.setContentView(view);
        productModifierSelectionBottomSheet.show();
    }

    @Override
    public int getItemCount() {
        return productsSort == null ? 0 : productsSort.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private NewMenuProductItemBinding binding;

        public ProductHolder(NewMenuProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    private void showToast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}
