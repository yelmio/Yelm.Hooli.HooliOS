package yelm.io.yelm.old_version.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import yelm.io.yelm.databinding.FrameLearnSecondBinding;


public class SecondLearnFragment extends Fragment {

    private FrameLearnSecondBinding binding;

    public SecondLearnFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FrameLearnSecondBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

}
