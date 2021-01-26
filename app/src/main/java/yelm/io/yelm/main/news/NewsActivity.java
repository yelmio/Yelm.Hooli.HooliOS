package yelm.io.yelm.main.news;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityNewsBinding;
import yelm.io.yelm.main.model.NewNews;

public class NewsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, Html.ImageGetter {

    ActivityNewsBinding binding;
    private int maxScrollSize;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 80;
    private boolean isImageHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NewNews news = getIntent().getParcelableExtra("news");
        if (news != null) {
            binding(news);
        }

//        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

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