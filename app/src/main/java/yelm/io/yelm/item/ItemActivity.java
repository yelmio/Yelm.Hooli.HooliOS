package yelm.io.yelm.item;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.databinding.ActivityItemBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.support_stuff.AlexTAG;

public class ItemActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    ActivityItemBinding binding;
    private int maxScrollSize;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 80;
    private boolean isImageHidden;

    ProductSpecificationsAdapter productSpecificationsAdapter;
    ProductModifierAdapter productModifierAdapter;

    HashMap<String, String> modifiers = new HashMap<>();

    private BigDecimal finalPrice = new BigDecimal("0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Item item = getIntent().getParcelableExtra("item");
        if (item != null) {
            finalPrice = new BigDecimal(item.getPrice());
            setPrice(item, finalPrice);
            binding(item);
            bindingAddSubtractProductCount();
            bindingAddProductToBasket(item);
        } else {
            Log.e(AlexTAG.error, "Method onCreate() in ProductNewActivity: by some reason product==null");
        }
    }

    private void setPrice(Item product, BigDecimal bd) {
        if (!product.getDiscount().equals("0")) {
            bd = new BigDecimal(product.getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(product.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(product.getPrice()).subtract(bd);
        }
        binding.cost.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
    }


    private void binding(Item item) {
        if (item.getModifier() != null && item.getModifier().size() == 0) {
            binding.modifierTitle.setVisibility(View.GONE);
        }
        if (item.getSpecification() != null && item.getSpecification().size() == 0) {
            binding.specificationsTitle.setVisibility(View.GONE);
        }

        productSpecificationsAdapter = new ProductSpecificationsAdapter(this, item.getSpecification());
        binding.recyclerSpecifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpecifications.setAdapter(productSpecificationsAdapter);

        productModifierAdapter = new ProductModifierAdapter(this, item.getModifier());
        productModifierAdapter.setListener((modifier, check) -> {
            if (check) {
                modifiers.put(modifier.getName(), modifier.getValue());
            } else {
                modifiers.remove(modifier.getName());
            }
            Log.d("AlexDebug", "modifiers: " + modifiers.toString());
            BigDecimal costCurrent = new BigDecimal(finalPrice.toString());
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
            }
            costCurrent = costCurrent.multiply(new BigDecimal(binding.countProducts.getText().toString()));
            binding.cost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        });
        binding.recyclerModifier.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerModifier.setAdapter(productModifierAdapter);

        binding.back.setOnClickListener(v -> finish());
        binding.collapsingToolbar.setTitle(item.getName());
        binding.name.setText(item.getName());
        binding.appbar.addOnOffsetChangedListener(this);
        binding.description.setText(item.getDescription());
        binding.discount.setText(String.format("%s %s %%", getText(R.string.product_discount), item.getDiscount()));
        binding.ratingBar.setRating(Float.parseFloat(item.getRating()));
        Picasso.get()
                .load(item.getImages().get(0))
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(binding.image);

    }

    private void bindingAddSubtractProductCount() {
        binding.addProduct.setOnClickListener(v -> {
            BigInteger counter = new BigInteger(binding.countProducts.getText().toString());
            counter = counter.add(new BigInteger("1"));
            binding.countProducts.setText(String.format("%s", counter.toString()));
            BigDecimal costCurrent = new BigDecimal(finalPrice.toString());
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
            }
            costCurrent = costCurrent.multiply(new BigDecimal(counter.toString()));
            binding.cost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        });

        binding.removeProduct.setOnClickListener(v -> {
            if (!binding.countProducts.getText().toString().equals("1")) {
                BigInteger counter = new BigInteger(binding.countProducts.getText().toString());
                counter = counter.subtract(new BigInteger("1"));
                binding.countProducts.setText(String.format("%s", counter.toString()));
                BigDecimal costCurrent = new BigDecimal(finalPrice.toString());
                for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                    costCurrent = costCurrent.add(new BigDecimal(modifierEntry.getValue()));
                }
                costCurrent = costCurrent.multiply(new BigDecimal(counter.toString()));
                binding.cost.setText(String.format("%s %s", costCurrent.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            }
        });
    }

    private void bindingAddProductToBasket(Item product) {

        binding.addToCart.setOnClickListener(v -> {

            List<BasketCart> listCartsByID = Common.basketCartRepository.getListBasketCartByItemID(product.getId());

            List<Modifier> listModifiers = new ArrayList<>();
            for (Map.Entry<String, String> modifierEntry : modifiers.entrySet()) {
                listModifiers.add(new Modifier(modifierEntry.getKey(), modifierEntry.getValue()));
            }

            if (listCartsByID != null && listCartsByID.size() != 0) {
                BigInteger countOfAllProducts = new BigInteger("0");
                for (BasketCart basketCart : listCartsByID) {
                    countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
                }
                for (BasketCart basketCart : listCartsByID) {
                    if (basketCart.modifier.equals(listModifiers)) {
                        basketCart.count = new BigInteger(basketCart.count).add(new BigInteger(binding.countProducts.getText().toString())).toString();
                        Common.basketCartRepository.updateBasketCart(basketCart);
                        Log.d(AlexTAG.debug, "Method update Product in Basket. listCartsByID !=null:  " + basketCart.toString());
                        return;
                    }
                }
            }
            BasketCart cartItem = new BasketCart();
            cartItem.itemID = product.getId();
            cartItem.name = product.getName();
            cartItem.discount = product.getDiscount();
            cartItem.startPrice = product.getPrice();
            cartItem.finalPrice = finalPrice.toString();
            cartItem.type = product.getType();
            cartItem.count = binding.countProducts.getText().toString();
            cartItem.imageUrl = product.getPreviewImage();
            //cartItem.quantity = current.getQuantity();
            cartItem.discount = product.getDiscount();
            cartItem.modifier = listModifiers;
            cartItem.isPromo = false;
            cartItem.isExist = true;
            cartItem.quantityType = product.getUnitType();
            Common.basketCartRepository.insertToBasketCart(cartItem);
            Log.d(AlexTAG.debug, "Method add Product to Basket. listCartsByID == null:  " + cartItem.toString());
        });


    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (maxScrollSize == 0)
            maxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100
                / maxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!isImageHidden) {
                isImageHidden = true;

                //ViewCompat.animate(mFab).scaleY((float) 0.9).scaleX((float) 0.9).start();
                //ViewCompat.animate(imageButton).scaleY((float) 0.2).scaleX((float) 0.2).setDuration(200).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (isImageHidden) {
                isImageHidden = false;

                //ViewCompat.animate(mFab).scaleY(1).scaleX(1).start();
                //ViewCompat.animate(imageButton).scaleY(1).scaleX(1).start();
            }
        }
    }
}