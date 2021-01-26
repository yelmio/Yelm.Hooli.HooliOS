package yelm.io.yelm.fragments.catalog_fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.ProductActivity;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.loader.controller.LoaderActivity;

public class ProductsAdapterHorizontal extends RecyclerView.Adapter<ProductsAdapterHorizontal.ProductHolder> {

    private Context context;
    private List<Product> products;

    private Listener listener;

    public interface Listener {
        void onClick(Product product);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ProductsAdapterHorizontal(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.product_item_horizontal, parent, false);
        return new ProductHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductHolder holder, final int position) {
        final Product currentProduct = products.get(position);
        final String finalName = currentProduct.getName();
        final String finalPrice = currentProduct.getPrice();
        final String finalText_about = currentProduct.getText_about();
        final String finalId = currentProduct.getItemID();
        final String finalType = currentProduct.getType();
        final String parameters = currentProduct.getParameters();
        final String type = currentProduct.getType();
        final String quantity = currentProduct.getQuantity();

        final String imageUrlArray = currentProduct.getImage();
        String imageFirst = "";
        try {
            JSONArray jsonImageUrlArray = new JSONArray(imageUrlArray);
            imageFirst =  jsonImageUrlArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalUrl = imageFirst;

        holder.name.setText(finalName);
//        holder.price.setText(new StringBuilder().append("").append(finalPrice).append(" руб. / ").append(quantity).append(' ').append(type).toString());

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
                listener.onClick(products.get(position));
            }
        });

        holder.card_product.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("name", finalName);
            intent.putExtra("price", finalPrice);
            intent.putExtra("image", imageUrlArray);
            intent.putExtra("id", finalId);
            intent.putExtra("type", finalType);
            intent.putExtra("text_about", finalText_about);
            intent.putExtra("parameters", parameters);
            intent.putExtra("quantity", quantity);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
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
