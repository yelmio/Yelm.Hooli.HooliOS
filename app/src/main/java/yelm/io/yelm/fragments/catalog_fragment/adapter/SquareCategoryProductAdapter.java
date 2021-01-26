package yelm.io.yelm.fragments.catalog_fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.model.CatalogClass;
import yelm.io.yelm.support_stuff.GradientTransformation;


public class SquareCategoryProductAdapter extends RecyclerView.Adapter<SquareCategoryProductAdapter.CategoryHolder> {

    private Context context;
    ArrayList<CatalogClass> catalog;

    private SquareCategoryProductAdapter.Listener listener;

    public interface Listener {
        void onClick(Integer index);
    }

    public void setListener(SquareCategoryProductAdapter.Listener listener) {
        this.listener = listener;
    }

    public SquareCategoryProductAdapter(Context context, ArrayList<CatalogClass> catalog) {
        this.context = context;
        this.catalog = catalog;
    }

    @NonNull
    @Override
    public SquareCategoryProductAdapter.CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.square_catalog_item, parent, false);
        return new SquareCategoryProductAdapter.CategoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SquareCategoryProductAdapter.CategoryHolder holder, final int position) {
        CatalogClass currentCategory = catalog.get(position);
        if (currentCategory.getItem().getName().isEmpty()){
            Picasso.get()
                    .load(currentCategory.getItem().getImage())
                    .noPlaceholder()
                    .resize(600,0)
                    .into(holder.imageCatalog);
        }else {
            holder.inscription.setText(currentCategory.getItem().getName());
            Picasso.get()
                    .load(currentCategory.getItem().getImage())
                    .noPlaceholder()
                    .resize(600,0)
                    .transform(new GradientTransformation(context))
                    .into(holder.imageCatalog);
        }

        holder.imageCatalog.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return catalog.size();
    }

    public static class CategoryHolder extends RecyclerView.ViewHolder {
        TextView inscription;
        ImageView imageCatalog;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            inscription = itemView.findViewById(R.id.inscription);
            imageCatalog = itemView.findViewById(R.id.imageCatalog);
        }
    }

}
