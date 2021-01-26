package yelm.io.yelm.old_version.learning;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import yelm.io.yelm.databinding.FrameLearnFourthBinding;
import yelm.io.yelm.main.controller.MainActivity;
import yelm.io.yelm.retrofit.DynamicURL;

public class FourthLearnFragment extends Fragment {

    private FrameLearnFourthBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FrameLearnFourthBinding.inflate(getLayoutInflater(), container, false);
        binding();
        return binding.getRoot();
    }

    private void binding() {
        binding.breadbasket.setOnClickListener(v -> {
            //((LearnMain) getActivity()).nextFragment();
            DynamicURL.setPLATFORM("5f771d465f4191.76733056");
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        binding.fallon.setOnClickListener(v -> {
            DynamicURL.setPLATFORM("5f8561895c51f7.73864076");
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        binding.bell.setOnClickListener(v -> {
            DynamicURL.setPLATFORM("5f5dfa9a7023c2.94067733");
            startActivity(new Intent(getActivity(), MainActivity.class));
        });
    }

}
