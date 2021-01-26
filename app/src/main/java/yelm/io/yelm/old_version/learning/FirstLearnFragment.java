package yelm.io.yelm.old_version.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import yelm.io.yelm.databinding.FrameLearnFirstBinding;


public class FirstLearnFragment extends Fragment {

    private FrameLearnFirstBinding binding;

    public FirstLearnFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FrameLearnFirstBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

}
