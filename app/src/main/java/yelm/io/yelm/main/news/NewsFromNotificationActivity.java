package yelm.io.yelm.main.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityNewsFromNotificationBinding;
import yelm.io.yelm.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.retrofit.RestAPI;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.constants.Logging;

public class NewsFromNotificationActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    ActivityNewsFromNotificationBinding binding;
    ProductsNewMenuSquareImageAdapter productsSquareAdapter;
    private int maxScrollSize;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 80;
    private boolean isImageHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsFromNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle args = getIntent().getExtras();
        if (args != null) {
            Log.d(Logging.debug, "NewsFromNotificationActivity - id: " + args.getString("id"));
            String id = args.getString("id");
            getNewsById(id);
        }
    }

    private void getNewsById(String id) {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getNewsByID(
                        "3",
                        RestAPI.PLATFORM_NUMBER,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        id).
                enqueue(new Callback<NewNews>() {
                    @Override
                    public void onResponse(@NotNull Call<NewNews> call, @NotNull final Response<NewNews> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                seNews(response.body());
                            } else {
                                Log.e(Logging.error, "Method getNewsById() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getNewsById() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<NewNews> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getNewsById() - failure: " + t.toString());
                    }
                });
    }

    private void seNews(NewNews news) {
        List<Item> products = news.getItems();
        if (products.size() != 0) {
            binding.titleProducts.setVisibility(View.VISIBLE);
        }
        productsSquareAdapter = new ProductsNewMenuSquareImageAdapter(this, products);
        binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        binding.recycler.setAdapter(productsSquareAdapter);
        binding.share.setOnClickListener(v -> {
            String sharingLink = "https://yelm.io/news/" + news.getId();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            intent.putExtra(Intent.EXTRA_TEXT, sharingLink);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.newsActivityShare)));
        });

        String body = news.getDescription();
        Log.d(Logging.debug, "body" + body);
        String data = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title></title>" +
                "<style>" +
//                "    img {" +
//                "      border-radius: 16px;" +
//                "      width: 100% !important;" +
//                "    }" +
//                "" +
                ".img-overlay {" +
                "  width: 100%;" +
                "  height: auto;" +
                "  border-radius: 16px;" +
                "  overflow: hidden;" +
                "}" +
                "" +
                ".img-overlay img {" +
                "  min-width: 100%;" +
                "  max-width: 100%;" +
                "  width: 100%;" +
                "  display: block;" +
                "  -o-object-fit: cover;" +
                "  object-fit: cover;" +
                "}" +
                "    iframe {" +
                "      width: 100% !important;" +
                "    }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                body +
                "<script> Array.prototype.forEach.call(document.getElementsByTagName('img'), (element) => {" +
                "  const parent = element.parentNode;" +
                "  const wrapper = document.createElement('div');" +
                "  wrapper.classList.add('img-overlay');" +
                "  parent.replaceChild(wrapper, element);" +
                "  wrapper.appendChild(element);" +
                "}); </script>" +
                "</body>" +
                "</html>";
        binding.web.getSettings().setJavaScriptEnabled(true);
        binding.web.loadData(data, "text/html", "utf-8");
        binding.back.setOnClickListener(v -> finish());
        binding.collapsingToolbar.setTitle(news.getTitle());
        binding.appbar.addOnOffsetChangedListener(this);
        Picasso.get()
                .load(news.getImage())
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(binding.image);
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
            }
        }
        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (isImageHidden) {
                isImageHidden = false;
            }
        }
    }
}