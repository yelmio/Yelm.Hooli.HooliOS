package yelm.io.yelm.main.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityNewsFromNotificationBinding;
import yelm.io.yelm.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.Logging;

public class NewsFromNotificationActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, Html.ImageGetter {

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
            Log.d(Logging.debug, "NewsFromNotificationActivity - Notification data: " + args.getString("data"));
            String id = args.getString("id");
            getNewsById(id);
        }
    }

    private void getNewsById(String id) {
        RetrofitClientNew.
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
                                binding(response.body());
                                productsSquareAdapter = new ProductsNewMenuSquareImageAdapter(NewsFromNotificationActivity.this, response.body().getItems());
                                binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
                                binding.recycler.setAdapter(productsSquareAdapter);
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

    private void binding(NewNews news) {
        binding.back.setOnClickListener(v -> finish());
        binding.collapsingToolbar.setTitle(news.getTitle());
        binding.appbar.addOnOffsetChangedListener(this);
        Picasso.get()
                .load(news.getImage())
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(binding.image);

        Spanned spanned = Html.fromHtml(news.getDescription(), this, null);
        binding.description.setText(spanned);

        //clickable links
        binding.description.setMovementMethod(LinkMovementMethod.getInstance());
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

    @Override
    public Drawable getDrawable(String s) {
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = getResources().getDrawable(R.drawable.ic_add_24_white);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        new HtmlImageLoad().execute(s, d);
        return d;
    }


    class HtmlImageLoad extends AsyncTask<Object, Void, Bitmap> {
        public HtmlImageLoad() {
            super();
        }

        private LevelListDrawable drawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            drawable = (LevelListDrawable) params[1];
            Log.d("AlexDebug", "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(bitmap);
                drawable.addLevel(1, 1, d);
                int imageWidth = binding.description.getWidth();
                float ratio = (float) imageWidth / bitmap.getWidth();
                int imageHeight = (int) (ratio * bitmap.getHeight());
                drawable.setBounds(0, 0, imageWidth, imageHeight);
                drawable.setLevel(1);
                CharSequence t = binding.description.getText();
                binding.description.setText(t);
            }
        }
    }


}