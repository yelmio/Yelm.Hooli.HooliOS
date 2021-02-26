package yelm.io.raccoon.main.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import yelm.io.raccoon.by_category.ProductsByCategoriesActivity;
import yelm.io.raccoon.databinding.SquareCategoryItemBinding;
import yelm.io.raccoon.main.categories.CategoriesPOJO;
import yelm.io.raccoon.main.news.NewsActivity;
import yelm.io.raccoon.rest.query.Statistic;
import yelm.io.raccoon.support_stuff.Logging;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ProductHolder> {
    private Context context;
    private List<CategoriesPOJO> categories;
    boolean isMoving = false;

    public CategoriesAdapter(Context context, List<CategoriesPOJO> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoriesAdapter.ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(SquareCategoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoriesAdapter.ProductHolder holder, final int position) {
        CategoriesPOJO current = categories.get(position);
        holder.binding.title.setText(current.getName());
        Picasso.get()
                .load(current.getImage())
                .noPlaceholder()
                .centerCrop()
                .resize(400, 0)
                .into(holder.binding.image);
//        holder.binding.image.setOnClickListener(v->{
//            Intent intent = new Intent(context, ProductsByCategoriesActivity.class);
//            intent.putExtra("catalogID", current.getId());
//            intent.putExtra("catalogName", current.getName());
//            context.startActivity(intent);
//        });


        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(holder.binding.root, "scaleY", 1f, 0.9f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(holder.binding.root, "scaleX", 1f, 0.9f);
        animationSet.setDuration(200).playTogether(scaleX, scaleY);
        holder.binding.root.setOnTouchListener((view, motionEvent) -> {
            Log.d(Logging.debug, "onTouch");
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(Logging.debug, "down");
                    animationSet.start();
                    isMoving = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(Logging.debug, "move");
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(Logging.debug, "up");
                    animationSet.cancel();
                    if (!isMoving) {
                        Intent intent = new Intent(context, ProductsByCategoriesActivity.class);
                        intent.putExtra("catalogID", current.getId());
                        intent.putExtra("catalogName", current.getName());
                        context.startActivity(intent);
                    }
                    AnimatorSet animationSet1 = new AnimatorSet();
                    ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(holder.binding.root, "scaleY", holder.binding.root.getScaleY(), 1.0f);
                    ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(holder.binding.root, "scaleX", holder.binding.root.getScaleX(), 1.0f);
                    animationSet1.setDuration(200).playTogether(scaleX1, scaleY1);
                    animationSet1.start();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d(Logging.debug, "cancel");
                    animationSet.cancel();
                    //if (!isMoving) {
//                        Intent intent = new Intent(context, ProductsByCategoriesActivity.class);
//                        intent.putExtra("catalogID", current.getId());
//                        intent.putExtra("catalogName", current.getName());
//                        context.startActivity(intent);
                    //}
                    AnimatorSet animationSet2 = new AnimatorSet();
                    ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(holder.binding.root, "scaleY", holder.binding.root.getScaleY(), 1.0f);
                    ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(holder.binding.root, "scaleX", holder.binding.root.getScaleX(), 1.0f);
                    animationSet2.setDuration(200).playTogether(scaleX2, scaleY2);
                    animationSet2.start();
                    break;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        private SquareCategoryItemBinding binding;

        public ProductHolder(SquareCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
