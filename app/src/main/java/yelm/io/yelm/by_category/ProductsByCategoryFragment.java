package yelm.io.yelm.by_category;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.FragmentProductsByCategoryBinding;
import yelm.io.yelm.item.ItemsOfOneCategoryActivity;
import yelm.io.yelm.main.adapter.ProductsNewMenuAdapter;
import yelm.io.yelm.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;

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
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) getResources().getDimension(R.dimen.dimens_8dp),
                    (int) getResources().getDimension(R.dimen.dimens_4dp),
                    (int) getResources().getDimension(R.dimen.dimens_8dp),
                    0);
            binding.recycler.setLayoutParams(lp);
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
            productsSquareAdapter = new ProductsNewMenuSquareImageAdapter(getContext(), productsByCategory.getItems());
            binding.recycler.setAdapter(productsSquareAdapter);
        }

        binding.categoryExpand.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ItemsOfOneCategoryActivity.class);
            intent.putParcelableArrayListExtra("items", (ArrayList<? extends Parcelable>) productsByCategory.getItems());
            intent.putExtra("title", productsByCategory.getName());
            startActivity(intent);
        });
        return binding.getRoot();
    }
}