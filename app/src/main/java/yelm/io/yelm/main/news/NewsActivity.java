package yelm.io.yelm.main.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityNewsBinding;
import yelm.io.yelm.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.constants.Logging;

public class NewsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    ActivityNewsBinding binding;
    private int maxScrollSize;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 80;
    private boolean isImageHidden;
    ProductsNewMenuSquareImageAdapter productsSquareAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NewNews news = getIntent().getParcelableExtra("news");
        if (news != null) {
            binding(news);
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
        } else {
            Log.e(Logging.error, "Method onCreate() in NewsActivity: by some reason news==null");
        }
    }

    private void binding(NewNews news) {
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
                .resize(600, 0)
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