package yelm.io.yelm.fragments.catalog_fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.model.CatalogClass;

public class CategoryProductAdapter extends RecyclerView.Adapter<CategoryProductAdapter.CategoryHolder>  {

    private Context context;
    ArrayList<CatalogClass> catalog;

    private Listener listener;

    public interface Listener {
        void onClick(Integer index);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public CategoryProductAdapter(Context context, ArrayList<CatalogClass> catalog) {
        this.context = context;
        this.catalog = catalog;
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryHolder holder, final int position) {
        CatalogClass currentCategory = catalog.get(position);
        holder.category.setText(currentCategory.getItem().getName());
        holder.count.setText("(" + currentCategory.getItemCount() + " шт.)");

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return catalog.size();
    }

    public static class CategoryHolder extends RecyclerView.ViewHolder {
        TextView category, count;
        LinearLayout layout;
        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.categoryExpand);
            count = itemView.findViewById(R.id.count);
            layout = itemView.findViewById(R.id.layout);
        }
    }


}
