package yelm.io.yelm.main.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import yelm.io.yelm.R;
import yelm.io.yelm.by_category.ProductsByCategoriesActivity;
import yelm.io.yelm.databinding.FragmentCategoryBinding;
import yelm.io.yelm.main.adapter.ProductsNewMenuAdapter;
import yelm.io.yelm.main.model.CategoriesWithProductsClass;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;

public class CategoryFragment extends Fragment {

    CategoriesWithProductsClass catalogsWithProductsClass;

    private FragmentCategoryBinding binding;
    ProductsNewMenuAdapter productsAdapter;

    public CategoryFragment() {
    }

    public CategoryFragment(CategoriesWithProductsClass categoriesWithProductsClass) {
        this.catalogsWithProductsClass = categoriesWithProductsClass;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(getLayoutInflater(), container, false);
        binding.title.setText(catalogsWithProductsClass.getName());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recycler.setHasFixedSize(false);
        binding.recycler.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));
        productsAdapter = new ProductsNewMenuAdapter(getContext(), catalogsWithProductsClass.getItems());
        binding.recycler.setAdapter(productsAdapter);
        binding.categoryExpand.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProductsByCategoriesActivity.class);
            intent.putExtra("catalogID", catalogsWithProductsClass.getCategoryID());
            intent.putExtra("catalogName", catalogsWithProductsClass.getName());
            startActivity(intent);
        });
        return binding.getRoot();
    }
}