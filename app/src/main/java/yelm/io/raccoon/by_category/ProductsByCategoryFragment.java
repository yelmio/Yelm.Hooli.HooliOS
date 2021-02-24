package yelm.io.raccoon.by_category;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import yelm.io.raccoon.R;
import yelm.io.raccoon.databinding.FragmentProductsByCategoryBinding;
import yelm.io.raccoon.item.ItemsOfOneCategoryActivity;
import yelm.io.raccoon.main.adapter.ProductsNewMenuAdapter;
import yelm.io.raccoon.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.raccoon.support_stuff.ItemOffsetDecorationRight;

public class ProductsByCategoryFragment extends Fragment {

    ProductsByCategoryClass productsByCategory;

    private FragmentProductsByCategoryBinding binding;
    ProductsNewMenuAdapter productsAdapter;
    ProductsNewMenuSquareImageAdapter productsSquareAdapter;

    boolean horizontalLayout;

    public ProductsByCategoryFragment() {
    }

    public ProductsByCategoryFragment(ProductsByCategoryClass productsByCategory, boolean horizontalLayout) {
        this.productsByCategory = productsByCategory;
        this.horizontalLayout = horizontalLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void sortAdapter(String s) {
        if (horizontalLayout) {
            productsAdapter.getFilter().filter(s.toString());
        } else {
            productsSquareAdapter.getFilter().filter(s.toString());
        }
        binding.recycler.scrollToPosition(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductsByCategoryBinding.inflate(getLayoutInflater(), container, false);

        if (productsByCategory.getName().isEmpty()) {
            binding.title.setVisibility(View.GONE);
        } else {
            binding.title.setText(productsByCategory.getName());
        }

        if (horizontalLayout) {
            binding.recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.recycler.setHasFixedSize(false);
            binding.recycler.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));
            productsAdapter = new ProductsNewMenuAdapter(getContext(), productsByCategory.getItems());
            binding.recycler.setAdapter(productsAdapter);
            binding.categoryExpand.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ItemsOfOneCategoryActivity.class);
                intent.putParcelableArrayListExtra("items", (ArrayList<? extends Parcelable>) productsByCategory.getItems());
                intent.putExtra("title", productsByCategory.getName());
                startActivity(intent);
            });
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) getResources().getDimension(R.dimen.dimens_8dp),
                    0,
                    (int) getResources().getDimension(R.dimen.dimens_8dp),
                    0);
            binding.recycler.setLayoutParams(lp);
            binding.categoryExpand.setVisibility(View.GONE);
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
            productsSquareAdapter = new ProductsNewMenuSquareImageAdapter(getContext(), productsByCategory.getItems());
            binding.recycler.setAdapter(productsSquareAdapter);
        }
        return binding.getRoot();
    }
}