package yelm.io.yelm.fragments.main_fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import yelm.io.yelm.fragments.catalog_fragment.ProductActivity;
import yelm.io.yelm.fragments.main_fragment.ArticleActivity;
import yelm.io.yelm.support_stuff.GradientTransformation;
import yelm.io.yelm.R;
import yelm.io.yelm.database_old.news.News;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    private Context context;
    private List<News> newsList;

    public NewsAdapter(Context context, List<News> news) {
        this.context = context;
        this.newsList = news;
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHolder holder, int position) {
        News current = newsList.get(position);
        holder.name.setText(current.getName());
        if (current.getName().trim().isEmpty()) {
            Picasso.get()
                    .load(current.getImage())
                    .noPlaceholder()
                    .into(holder.image);
        } else {
            Picasso.get()
                    .load(current.getImage())
                    .noPlaceholder()
                    .transform(new GradientTransformation(context))
                    .into(holder.image);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = null;
                JSONArray jsonArrayItems = null;
                JSONArray jsonArrayNews = null;
                String link = "";

                try {
                    json = new JSONObject(newsList.get(position).getAttachments());
                    link =  json.get("link").toString();
                    jsonArrayItems = json.getJSONArray("items");
                    jsonArrayNews = json.getJSONArray("news");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonArrayItems.length() != 0) {
                    String name = "";
                    String price = "0";
                    String url = "";
                    String text_about = "";
                    String id = "";
                    String type = "";
                    String quantity = "";
                    String parameters = "";
                    try {
                        JSONObject jsonItem = (JSONObject) jsonArrayItems.getJSONObject(0);
                        id =  jsonItem.get("id").toString();
                        name =  jsonItem.get("name").toString();
                        JSONObject item = (JSONObject) jsonItem.get("item");
                        quantity =  item.get("quantity").toString();
                        parameters = item.get("parameters").toString();
                        price = item.get("price").toString();
                        text_about =  item.get("text_about").toString();
                        type =  item.get("type").toString();
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

                if (jsonArrayNews.length() != 0) {
                    String image = "";
                    String title = "";
                    String text_about = "";
                    try {
                        JSONObject jsonNews = (JSONObject) jsonArrayNews.getJSONObject(0);
                        JSONArray jsonArray = jsonNews.getJSONArray("images");
                        image =  jsonArray.get(0).toString();
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

                if (!link.isEmpty()&&  URLUtil.isValidUrl(link)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;

        public NewsHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
        }
    }
}
