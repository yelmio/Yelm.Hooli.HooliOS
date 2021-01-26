package yelm.io.yelm.fragments.main_fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mmin18.widget.RealtimeBlurView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.database_old.articles.Articles;
import yelm.io.yelm.fragments.main_fragment.ArticleActivity;
import yelm.io.yelm.support_stuff.GradientTransformation;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleHolder> {

    private Context context;
    private List<Articles> articlesList;

    public ArticlesAdapter(Context context, List<Articles> articles) {
        this.context = context;
        this.articlesList = articles;
    }

    @NonNull
    @Override
    public ArticleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false);
        return new ArticleHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleHolder holder, int position) {
        Articles currentArticle = articlesList.get(position);

        switch (currentArticle.getViewscreen()) {
            case "1":
                if (currentArticle.getTitle().isEmpty()) {
                    holder.title1.setVisibility(View.GONE);
                } else {
                    holder.title1.setText(currentArticle.getTitle());
                }
                if (currentArticle.getTheme().isEmpty()) {
                    holder.theme1.setVisibility(View.GONE);
                } else {
                    holder.theme1.setText(currentArticle.getTheme());
                }
                if (currentArticle.getSubtitle().isEmpty()) {
                    holder.subtitle1.setVisibility(View.GONE);
                } else {
                    holder.subtitle1.setText(currentArticle.getSubtitle());
                }
                holder.layout1.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(currentArticle.getImage())
                        .noPlaceholder()
                        .resize(800,0)
                        .into(holder.news_image);
                break;

            case "2":

                holder.blur.setVisibility(View.VISIBLE);

                if (currentArticle.getTitle().isEmpty()) {
                    holder.title2.setVisibility(View.GONE);
                } else {
                    holder.title2.setText(currentArticle.getTitle());
                }
                if (currentArticle.getSubtitle().isEmpty()) {
                    holder.subtitle2.setVisibility(View.GONE);
                } else {
                    holder.subtitle2.setText(currentArticle.getSubtitle());
                }
                if (currentArticle.getTheme().isEmpty()) {
                    holder.theme2.setVisibility(View.GONE);
                } else {
                    holder.theme2.setText(currentArticle.getTheme());
                }

                Picasso.get()
                        .load(currentArticle.getImage())
                        .noPlaceholder()
                        .resize(800,0)
                        .into(holder.news_image, new Callback() {
                            @Override
                            public void onSuccess() {
                                ViewTreeObserver vto = holder.layout2.getViewTreeObserver();
                                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        holder.layout2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                        holder.realtimeBlur.getLayoutParams().height = holder.layout2.getMeasuredHeight();
                                        holder.realtimeBlur.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            @Override
                            public void onError(Exception e) {

                            }
                        });
                break;
            case "3":
                if (currentArticle.getTitle().isEmpty()) {
                    holder.title3.setVisibility(View.GONE);
                } else {
                    holder.title3.setText(currentArticle.getTitle());
                }
                if (currentArticle.getTheme().isEmpty()) {
                    holder.theme3.setVisibility(View.GONE);
                } else {
                    holder.theme3.setText(currentArticle.getTheme());
                }
                if (currentArticle.getSubtitle().isEmpty()) {
                    holder.subtitle3.setVisibility(View.GONE);
                } else {
                    holder.subtitle3.setText(currentArticle.getSubtitle());
                }

                holder.layout3.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(currentArticle.getImage())
                        .noPlaceholder()
                        .resize(800,0)
                        .transform(new GradientTransformation(context))
                        .into(holder.news_image);
                break;
            default:

                Picasso.get()
                        .load(currentArticle.getImage())
                        .noPlaceholder()
                        .resize(800,0)
                        .into(holder.news_image);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }

    public class ArticleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title1, title3, title2;
        public TextView theme1, theme2, theme3;
        public TextView subtitle1, subtitle3, subtitle2;
        public TextView singleText;
        public ImageView news_image;
        LinearLayout layout1, layout2, layout3;
        CardView blur, articleView;
        RealtimeBlurView realtimeBlur;

        public ArticleHolder(@NonNull View itemView) {
            super(itemView);
            articleView = itemView.findViewById(R.id.articleView);

            news_image = itemView.findViewById(R.id.article_image);
            realtimeBlur = itemView.findViewById(R.id.realtimeBlur);

            theme1 = itemView.findViewById(R.id.theme1);
            title1 = itemView.findViewById(R.id.title1);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            layout1 = itemView.findViewById(R.id.layout1);

            blur = itemView.findViewById(R.id.blur);
            layout2 = itemView.findViewById(R.id.layout2);
            theme2 = itemView.findViewById(R.id.theme2);
            title2 = itemView.findViewById(R.id.title2);
            subtitle2 = itemView.findViewById(R.id.subtitle2);


            theme3 = itemView.findViewById(R.id.theme3);
            title3 = itemView.findViewById(R.id.title3);
            subtitle3 = itemView.findViewById(R.id.subtitle3);
            layout3 = itemView.findViewById(R.id.layout3);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, ArticleActivity.class);
                Articles currentArticle = articlesList.get(position);
                intent.putExtra("image", currentArticle.getImage());
                intent.putExtra("title", currentArticle.getTitle());
                intent.putExtra("text_about", currentArticle.getText_about());
                context.startActivity(intent);
            }
        }
    }
}
