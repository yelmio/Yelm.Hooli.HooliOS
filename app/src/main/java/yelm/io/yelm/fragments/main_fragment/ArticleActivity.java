package yelm.io.yelm.fragments.main_fragment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import yelm.io.yelm.R;

public class ArticleActivity extends AppCompatActivity implements Html.ImageGetter {

    TextView articleTitle, articleDescription;
    ImageView articleImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        articleImage = findViewById(R.id.articleImage);
        articleTitle = findViewById(R.id.articleTitle);
        articleDescription = findViewById(R.id.articleDescription);

        Intent intent = getIntent();
        String image = intent.getStringExtra("image");
        String title = intent.getStringExtra("title");
        String text_about = intent.getStringExtra("text_about");

        Spanned spanned = Html.fromHtml(text_about, this, null);

        articleDescription.setText(spanned);

        //clickable links
        articleDescription.setMovementMethod(LinkMovementMethod.getInstance());

        Picasso.get().
                load(image).
                noPlaceholder().
                into(articleImage);

        articleTitle.setText(title);
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

        private LevelListDrawable drawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            drawable = (LevelListDrawable) params[1];
            Log.d("AlexDebug", "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
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
                int imageWidth = articleDescription.getWidth();
                float ratio = (float) imageWidth / bitmap.getWidth();
                int imageHeight = (int) (ratio * bitmap.getHeight());
                drawable.setBounds(0, 0, imageWidth, imageHeight);
                drawable.setLevel(1);
                CharSequence t = articleDescription.getText();
                articleDescription.setText(t);
            }
        }
    }
}