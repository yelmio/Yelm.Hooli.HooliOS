package yelm.io.yelm.fragments.catalog_fragment.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.ProductActivity;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.support_stuff.ScreenDimensions;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductHolder> implements Filterable {

    private Context context;
    private List<Product> products;
    private List<Product> productsSort;

    private Listener listener;
    ScreenDimensions screenDimensions;

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        //run back
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Product> filtered = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filtered.addAll(products);
            } else {
                for (Product product : products) {
                    if (product.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        Log.d("AlexDebug", "charSequence: " + charSequence);
                        Log.d("AlexDebug", "product.getName().toLowerCase(): " + product.getName().toLowerCase());
                        filtered.add(product);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered;
            for (Product product : filtered) {
                Log.d("AlexDebug", "filtered: " + product.getName());
            }
            return filterResults;
        }

        //run ui
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            productsSort.clear();
            productsSort.addAll((Collection<? extends Product>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public void sortProducts(int position) {
        switch (position) {
            case 0:
                Log.d("AlexDebug", "shuffle");
                new Thread(() -> {
                    Collections.shuffle(productsSort);
                    Collections.shuffle(products);
                    ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                }).start();
                break;
            case 1:
                Log.d("AlexDebug", "BY_PRICE_DESC");
                new Thread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        productsSort.sort(Product.BY_PRICE_DESC);
                        products.sort(Product.BY_PRICE_DESC);
                    }else {
                        Collections.sort(productsSort, Product.BY_PRICE_DESC);
                        Collections.sort(products, Product.BY_PRICE_DESC);
                    }
                    ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                }).start();
                break;
            case 2:
                Log.d("AlexDebug", "BY_PRICE_ASC");
                new Thread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        productsSort.sort(Product.BY_PRICE_ASC);
                        products.sort(Product.BY_PRICE_ASC);
                    }else {
                        Collections.sort(productsSort, Product.BY_PRICE_ASC);
                        Collections.sort(products, Product.BY_PRICE_ASC);
                    }
                    ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                }).start();
                break;
            case 3:
                Log.d("AlexDebug", "BY_DATE_ASC");
                new Thread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        productsSort.sort(Product.BY_DATE_ASC);
                        products.sort(Product.BY_DATE_ASC);
                    } else {
                        Collections.sort(productsSort, Product.BY_DATE_ASC);
                        Collections.sort(products, Product.BY_DATE_ASC);
                    }
                    ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                }).start();
                break;
            case 4:
                Log.d("AlexDebug", "BY_DATE_DESC");
                new Thread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        productsSort.sort(Product.BY_DATE_DESC);
                        products.sort(Product.BY_DATE_DESC);
                    } else {
                        Collections.sort(productsSort, Product.BY_DATE_DESC);
                        Collections.sort(products, Product.BY_DATE_DESC);
                    }
                    ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                }).start();
                break;
        }
    }

    public interface Listener {
        void onClick(Product product);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ProductsAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        productsSort = new ArrayList<>(products);
        this.screenDimensions = new ScreenDimensions((Activity) context);
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductHolder holder, final int position) {
        final Product currentProduct = productsSort.get(position);
        final String finalName = currentProduct.getName();
        final String finalPrice = currentProduct.getPrice();
        final String finalText_about = currentProduct.getText_about();
        final String finalId = currentProduct.getItemID();
        final String finalType = currentProduct.getType();
        final String parameters = currentProduct.getParameters();
        final String type = currentProduct.getType();
        final String quantity = currentProduct.getQuantity();

        holder.image_product.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 2) * screenDimensions.getScreenDensity() + 0.5f);

        final String imageUrlArray = currentProduct.getImage();
        String imageFirst = "";
        try {
            JSONArray jsonImageUrlArray = new JSONArray(imageUrlArray);
            imageFirst = jsonImageUrlArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalUrl = imageFirst;

        holder.name.setText(finalName);
        holder.price.setText(new StringBuilder().append(finalPrice)
                .append(" ")
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""))
                .append(" / ")
                .append(quantity)
                .append(' ')
                .append(type).toString());

        Picasso.get()
                .load(finalUrl)
                .noPlaceholder()
                .resize(400,0)
                .into(holder.image_product);

        holder.addToBasket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(productsSort.get(position));
            }
        });

        holder.card_product.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("name", finalName);
            intent.putExtra("price", finalPrice);
            intent.putExtra("image", imageUrlArray);
            intent.putExtra("id", finalId);
            intent.putExtra("type", finalType);
            intent.putExtra("parameters", parameters);
            intent.putExtra("text_about", finalText_about);
            intent.putExtra("quantity", quantity);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productsSort.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView price;
        public ImageView image_product;
        public CardView card_product;
        public ImageButton addToBasket;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            image_product = itemView.findViewById(R.id.product_image);
            price = itemView.findViewById(R.id.productPrice);
            name = itemView.findViewById(R.id.name);
            card_product = itemView.findViewById(R.id.card_product);
            addToBasket = itemView.findViewById(R.id.addToBasket);
        }
    }
}
