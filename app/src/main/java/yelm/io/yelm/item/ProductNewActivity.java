package yelm.io.yelm.item;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yelm.io.yelm.search.ItemSearch;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.databinding.ActivityProductNewBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;

public class ProductNewActivity extends AppCompatActivity {

    ActivityProductNewBinding binding;
    ProductSpecificationsAdapter productSpecificationsAdapter;
    ProductModifierAdapter productModifierAdapter;

    HashMap<String, String> modifiers = new HashMap<>();

    private BigDecimal finalPrice = new BigDecimal("0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ItemSearch product = getIntent().getParcelableExtra("product");
        if (product != null) {
            finalPrice = new BigDecimal(product.getPrice());
            setPrice(product, finalPrice);
            bindingMainViews(product);
            bindingAddSubtractProductCount();
            bindingAddProductToBasket(product);
        } else {
            Log.e(AlexTAG.error, "Method onCreate() in ProductNewActivity: by some reason product==null");
        }
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

    private void bindingAddProductToBasket(ItemSearch product) {

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
            cartItem.count = "1";
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

    private void bindingMainViews(ItemSearch product) {
        if (product.getModifier() != null && product.getModifier().size() == 0) {
            binding.modifierTitle.setVisibility(View.GONE);
        }
        if (product.getSpecification() != null && product.getSpecification().size() == 0) {
            binding.specificationsTitle.setVisibility(View.GONE);
        }

        productSpecificationsAdapter = new ProductSpecificationsAdapter(this, product.getSpecification());
        binding.recyclerSpecifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpecifications.setAdapter(productSpecificationsAdapter);

        productModifierAdapter = new ProductModifierAdapter(this, product.getModifier());
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

        //not decided what use
        binding.backButton.setOnClickListener(v -> this.finish());
        binding.backButton2.setOnClickListener(v -> this.finish());

        binding.description.setText(product.getDescription());
        binding.discount.setText(String.format("%s - %s%%", getText(R.string.product_discount), product.getDiscount()));
        binding.nameProduct.setText(product.getName());
        binding.name.setText(product.getName());
        binding.ratingBar.setRating(Float.parseFloat(product.getRating()));
        Picasso.get()
                .load(product.getImages().get(0))
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(binding.image);

    }

    private void setPrice(ItemSearch product, BigDecimal bd) {
        if (!product.getDiscount().equals("0")) {
            bd = new BigDecimal(product.getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(product.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(product.getPrice()).subtract(bd);
        }
        binding.cost.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
    }
}