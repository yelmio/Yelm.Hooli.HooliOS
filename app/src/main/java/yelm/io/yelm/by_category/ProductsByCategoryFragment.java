package yelm.io.yelm.by_category;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.FragmentProductsByCategoryBinding;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductsByCategoryBinding.inflate(getLayoutInflater(), container, false);

        if (productsByCategory.getName().isEmpty()) {
            binding.title.setVisibility(View.GONE);
        } else {
            binding.title.setText(productsByCategory.getName());
        }

        Log.d(AlexTAG.debug, "productsByCategory.getItems().size: " + productsByCategory.getItems().size());


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

        return binding.getRoot();
    }

}