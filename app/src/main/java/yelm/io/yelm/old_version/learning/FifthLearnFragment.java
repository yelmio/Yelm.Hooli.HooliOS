package yelm.io.yelm.old_version.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import yelm.io.yelm.databinding.FrameLearnFifthBinding;

public class FifthLearnFragment extends Fragment {


    private FrameLearnFifthBinding binding;

    public FifthLearnFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FrameLearnFifthBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }


}
