package yelm.io.raccoon.support_stuff;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

import yelm.io.raccoon.R;

public class GradientTransformation implements Transformation {

    int startColor;
    int endColor;

    public GradientTransformation(Context context) {
        startColor = context.getResources().getColor(R.color.colorGradient);
        endColor = Color.TRANSPARENT;
    }

    @Override public Bitmap transform(Bitmap source) {

        float x = source.getWidth();
        float y = source.getHeight();

        Bitmap gradientBitmap = source.copy(source.getConfig(), true);
        Canvas canvas = new Canvas(gradientBitmap);
        //left-top == (0,0) , right-bottom(x,y);
        LinearGradient grad =
                new LinearGradient(x/2, y, x/2, y/3, startColor, endColor, Shader.TileMode.CLAMP);
        Paint p = new Paint(Paint.DITHER_FLAG);
        p.setShader(null);
        p.setDither(true);
        p.setFilterBitmap(true);
        p.setShader(grad);
        canvas.drawPaint(p);
        source.recycle();
        return gradientBitmap;
    }

    @Override public String key() {
        return "Gradient";
    }
}
