package yelm.io.yelm.fragments.catalog_fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.R;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.fragments.catalog_fragment.adapter.SliderPictureAdapter;
import yelm.io.yelm.loader.controller.LoaderActivity;

public class ProductActivity extends AppCompatActivity {

    CardView cardDescription, cardParameters;
    TextView productName, productDescription, productPrice, addToCart, countProducts;

    private SliderPictureAdapter adapter;
    List<String> picturesList = new ArrayList<>();
    SliderView imageSlider;

    ImageButton cardViewAdd, cardViewRemove;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        initViews();


        Intent intent = getIntent();
        String imageUrlArray = intent.getStringExtra("image");

        String imageFirst = "";
        JSONArray jsonImageUrlArray = null;
        try {
            jsonImageUrlArray = new JSONArray(imageUrlArray);
            imageFirst = jsonImageUrlArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String imageUrl = imageFirst;

        for (int i = 0; i < jsonImageUrlArray.length(); i++) {
            try {
                picturesList.add((String) jsonImageUrlArray.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new SliderPictureAdapter(this, picturesList);
        imageSlider.setSliderAdapter(adapter);

        final String name = intent.getStringExtra("name");
        final String id = intent.getStringExtra("id");
        final String type = intent.getStringExtra("type");
        final String price = intent.getStringExtra("price");
        String text_about = intent.getStringExtra("text_about");
        String quantity = intent.getStringExtra("quantity");

        productName.setText(name);

        BigDecimal currentPrice = new BigDecimal(price);

        JSONArray jsonArray = null;
        String parameterName = "";
        String parameterValue = "";
        String parameters = "";
        try {
            jsonArray = new JSONArray(intent.getStringExtra("parameters"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                parameterName =  json.get("name").toString();
                parameterValue =  json.get("value").toString();
                parameters = parameters + (parameterName + ": " + parameterValue + "\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String finalParameters = parameters;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            productDescription.setText(Html.fromHtml(text_about, Html.FROM_HTML_MODE_COMPACT));
        } else {
            productDescription.setText(Html.fromHtml(text_about));
        }

//        Picasso.get().
//                load(imageUrl).
//                noPlaceholder().
//                into(productImage);
//        productName.setText(name);

        final Cart cart = Common.cartRepository.getCartItemById(id);

        if (cart == null) {
            addToCart.setVisibility(View.VISIBLE);
            Log.d("AlexDebug", "cart null");
            productPrice.setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                    .append(" ")
                    .append(currentPrice.toString()));
        } else {
            Log.d("AlexDebug", "cart.name" + cart.name);
            Log.d("AlexDebug", "cart.count" + cart.count);
            countProducts.setText(String.valueOf(cart.count));
            productPrice.setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                    .append(" ")
                    .append(currentPrice.multiply(new BigDecimal(cart.count))));
        }

        cardViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AlexDebug", "add cart:");
                Cart cartByID = Common.cartRepository.getCartItemById(id);
                if (cartByID == null) {
                    return;
                }
                BigDecimal temp = new BigDecimal(cartByID.count);
                cartByID.count = temp.add(new BigDecimal("1")).toString();
                productPrice.setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                        .append(" ")
                        .append(currentPrice.multiply(new BigDecimal (cartByID.count))));
                countProducts.setText(cartByID.count);
                Common.cartRepository.updateCart(cartByID);
                Log.d("AlexDebug", "cartByName.item" + cartByID.item);
                Log.d("AlexDebug", "cartByName.count" + cartByID.count);
            }
        });

        cardViewRemove.setOnClickListener(view -> {
            Log.d("AlexDebug", "remove cart:");
            Cart cartByID = Common.cartRepository.getCartItemById(id);
            if (cartByID == null) {
                return;
            }
            if (cartByID.count.equals("1")) {
                Common.cartRepository.deleteCartItem(cartByID);
                addToCart.setVisibility(View.VISIBLE);
                Log.d("AlexDebug", "finally remove cart");
                return;
            } else {
                BigDecimal temp = new BigDecimal(cartByID.count);
                cartByID.count = temp.subtract(new BigDecimal("1")).toString();
                productPrice.setText(new StringBuilder(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                        .append(" ")
                        .append(currentPrice.multiply(new BigDecimal(cartByID.count))));
                Common.cartRepository.updateCart(cartByID);
            }
            countProducts.setText(cartByID.count);
            Log.d("AlexDebug", "cartByName.item" + cartByID.item);
            Log.d("AlexDebug", "cartByName.count" + cartByID.count);
        });

        addToCart.setOnClickListener(view -> {
            Log.d("AlexDebug", "add cart by Blue Button:");
            Cart cartByID = Common.cartRepository.getCartItemById(id);
            if (cartByID == null) {
                Cart cartItem = new Cart();
                cartItem.count = "1";
                cartItem.item = id;
                cartItem.name = name;
                cartItem.type = type;
                cartItem.price = price;
                cartItem.quantity = quantity;
                cartItem.imageUrl = imageUrl;
                cartItem.isPromo = false;
                countProducts.setText("1");
                Common.cartRepository.insertToCart(cartItem);
                addToCart.setVisibility(View.GONE);
            }
        });

        cardDescription.setOnClickListener(view -> {
            cardParameters.setCardBackgroundColor(Color.TRANSPARENT);
            cardDescription.setCardBackgroundColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                productDescription.setText(Html.fromHtml(text_about, Html.FROM_HTML_MODE_COMPACT));
            } else {
                productDescription.setText(Html.fromHtml(text_about));
            }
        });

        cardParameters.setOnClickListener(view -> {
            cardParameters.setCardBackgroundColor(Color.WHITE);
            cardDescription.setCardBackgroundColor(Color.TRANSPARENT);
            productDescription.setText(finalParameters);
        });
    }

    private void initViews() {
        cardDescription = findViewById(R.id.cardDescription);
        cardParameters = findViewById(R.id.cardParameters);
        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);

        imageSlider = findViewById(R.id.imageSlider);


        productPrice = findViewById(R.id.productPrice);
        addToCart = findViewById(R.id.addToCart);

        countProducts = findViewById(R.id.countProducts);
        cardViewAdd = findViewById(R.id.cardViewAdd);
        cardViewRemove = findViewById(R.id.cardViewRemove);

        cardParameters.setCardBackgroundColor(Color.TRANSPARENT);
    }
}