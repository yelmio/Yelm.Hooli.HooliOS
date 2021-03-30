package yelm.io.raccoon.item;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yelm.io.raccoon.R;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.databinding.ActivityItemBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Item;
import yelm.io.raccoon.main.model.Modifier;
import yelm.io.raccoon.rest.query.RestMethods;
import yelm.io.raccoon.support_stuff.Logging;

public class ItemActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    ActivityItemBinding binding;
    private int maxScrollSize;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 80;
    private boolean isImageHidden;
    Toast toast;
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
            finalPrice = getPrice(item, new BigDecimal(item.getPrice()));
            binding(item);
            bindingAddSubtractProductCount();
            bindingAddProductToBasket(item);
            binding.share.setOnClickListener(v -> {
                RestMethods.sendStatistic("share_item");
                String sharingLink = "https://yelm.io/item/" + item.getId();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
                intent.putExtra(Intent.EXTRA_TEXT, sharingLink);
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.newsActivityShare)));
            });
        } else {
            Log.e(Logging.error, "Method onCreate() in ItemActivity: by some reason item==null");
        }
    }

    private BigDecimal getPrice(Item product, BigDecimal bd) {
        if (!product.getDiscount().equals("0")) {
            bd = new BigDecimal(product.getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(product.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(product.getPrice()).subtract(bd);
            //trim zeros if after comma there are only zeros: 45.00 -> 45
            if (bd.compareTo(new BigDecimal(String.valueOf(bd.setScale(0, BigDecimal.ROUND_HALF_UP)))) == 0) {
                bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            }
        }
        binding.cost.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        return bd;
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
            Log.d(Logging.debug, "modifiers: " + modifiers.toString());
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.description.setText(Html.fromHtml(item.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            binding.description.setText(Html.fromHtml(item.getDescription()));
        }

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

            //cant add product if limit is over
            BigDecimal countOfProducts = new BigDecimal("0");
            if (listCartsByID != null && listCartsByID.size() != 0) {
                for (BasketCart basketCart : listCartsByID) {
                    countOfProducts = countOfProducts.add(new BigDecimal(basketCart.count));
                }
                if (new BigDecimal(binding.countProducts.getText().toString()).add(countOfProducts).compareTo(new BigDecimal(listCartsByID.get(0).quantity)) > 0) {
                    showToast(getString(R.string.productsNotAvailable) +
                            " " + listCartsByID.get(0).quantity + " " + getString(R.string.basketActivityPC));
                    return;
                }
            } else {
                if (new BigDecimal(binding.countProducts.getText().toString()).add(countOfProducts).compareTo(new BigDecimal(product.getQuantity())) > 0) {
                    showToast(getString(R.string.productsNotAvailable) +
                            " " + product.getQuantity() + " " + getString(R.string.basketActivityPC));
                    return;
                }
            }

            String added = (String) (binding.countProducts.getText().toString().equals("1") ? getText(R.string.productNewActivityAddedOne) : getText(R.string.productNewActivityAddedMulti));
            showToast("" +
                    product.getName() + " " +
                    binding.countProducts.getText().toString() + " " +
                    getText(R.string.productNewActivityPC) + " " +
                    added + " " +
                    getText(R.string.productNewActivityAddedToBasket));

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
                        Log.d(Logging.debug, "Method update Product in Basket. listCartsByID !=null:  " + basketCart.toString());
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
            cartItem.quantity = product.getQuantity();
            cartItem.discount = product.getDiscount();
            cartItem.modifier = listModifiers;
            cartItem.isPromo = false;
            cartItem.isExist = true;
            cartItem.quantityType = product.getUnitType();
            Common.basketCartRepository.insertToBasketCart(cartItem);
            Log.d(Logging.debug, "Method add Product to Basket. listCartsByID == null:  " + cartItem.toString());
        });
    }

    private void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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