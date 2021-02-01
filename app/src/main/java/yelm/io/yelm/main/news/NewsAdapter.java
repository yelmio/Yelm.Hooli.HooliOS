package yelm.io.yelm.main.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yelm.io.yelm.databinding.NewsItemBinding;
import yelm.io.yelm.fragments.catalog_fragment.ProductActivity;
import yelm.io.yelm.fragments.main_fragment.ArticleActivity;
import yelm.io.yelm.item.ItemActivity;
import yelm.io.yelm.item.ItemsOfOneCategoryActivity;
import yelm.io.yelm.support_stuff.GradientTransformation;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    private Context context;
    private List<NewNews> news;

    public NewsAdapter(Context context, List<NewNews> news) {
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsAdapter.NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsHolder(NewsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.NewsHolder holder, int position) {
        NewNews currentNews = news.get(position);
        holder.binding.name.setText(currentNews.getTitle());
        if (currentNews.getTitle().trim().isEmpty()) {
            Picasso.get()
                    .load(currentNews.getImage())
                    .noPlaceholder()
                    .centerCrop()
                    .resize(600, 0)
                    .into(holder.binding.image);
        } else {
            Picasso.get()
                    .load(currentNews.getImage())
                    .noPlaceholder()
                    .centerCrop()
                    .resize(600, 0)
                    .transform(new GradientTransformation(context))
                    .into(holder.binding.image);
        }

        holder.binding.image.setOnClickListener(v -> {
            // if (currentNews.getItems().size() == 0) {
            Intent intent = new Intent(context, NewsActivity.class);
            intent.putExtra("news", currentNews);
            intent.putParcelableArrayListExtra("items", (ArrayList<? extends Parcelable>) currentNews.getItems());
            context.startActivity(intent);
//            } else if (currentNews.getItems().size() == 1) {
//                Intent intentItem = new Intent(context, ItemActivity.class);
//                intentItem.putExtra("item", currentNews.getItems().get(0));
//                context.startActivity(intentItem);
//            } else {
//                Intent intentItem = new Intent(context, ItemsOfOneCategoryActivity.class);
//                intentItem.putParcelableArrayListExtra("items", (ArrayList<? extends Parcelable>) currentNews.getItems());
//                intentItem.putExtra("title", currentNews.getTitle());
//                context.startActivity(intentItem);
//            }
        });
        //holder.image.setOnClickListener(view -> setLinks(currentNews.getAttachments()));
    }

    private void setLinks(String attachments) {
        JSONObject json;
        JSONArray jsonArrayItems = null;
        JSONArray jsonArrayNews = null;
        String link = "";

        try {
            json = new JSONObject(attachments);
            link = json.get("link").toString();
            jsonArrayItems = json.getJSONArray("items");
            jsonArrayNews = json.getJSONArray("news");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!link.isEmpty() && URLUtil.isValidUrl(link)) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        }

        if (jsonArrayItems != null && jsonArrayItems.length() != 0) {
            String name = "";
            String price = "0";
            String url = "";
            String text_about = "";
            String id = "";
            String type = "";
            String quantity = "";
            String parameters = "";
            try {
                JSONObject jsonItem = jsonArrayItems.getJSONObject(0);
                id = jsonItem.get("id").toString();
                name = jsonItem.get("name").toString();
                JSONObject item = (JSONObject) jsonItem.get("item");
                quantity = item.get("quantity").toString();
                parameters = item.get("parameters").toString();
                price = item.get("price").toString();
                text_about = item.get("text_about").toString();
                type = item.get("type").toString();
                JSONArray urls = item.getJSONArray("images");
                url = urls.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("price", price);
            intent.putExtra("image", url);
            intent.putExtra("id", id);
            intent.putExtra("parameters", parameters);
            intent.putExtra("type", type);
            intent.putExtra("quantity", quantity);
            intent.putExtra("text_about", text_about);
            context.startActivity(intent);
        }

        if (jsonArrayNews != null && jsonArrayNews.length() != 0) {
            String image = "";
            String title = "";
            String text_about = "";
            try {
                JSONObject jsonNews = jsonArrayNews.getJSONObject(0);
                JSONArray jsonArray = jsonNews.getJSONArray("images");
                image = jsonArray.get(0).toString();
                title = jsonNews.get("title").toString();
                JSONObject text = (JSONObject) jsonNews.get("item");
                text_about = text.get("text_about").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, ArticleActivity.class);
            intent.putExtra("image", image);
            intent.putExtra("title", title);
            intent.putExtra("text_about", text_about);
            context.startActivity(intent);
        }

    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public static class NewsHolder extends RecyclerView.ViewHolder {
        private NewsItemBinding binding;

        public NewsHolder(NewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
