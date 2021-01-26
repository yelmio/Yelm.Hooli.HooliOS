package yelm.io.yelm.old_version.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import yelm.io.yelm.databinding.FragmentProfileBinding;
import yelm.io.yelm.support_stuff.ScreenDimensions;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        ScreenDimensions screenDimensions = new ScreenDimensions((Activity) getContext());
        binding.pushCard.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 2) * screenDimensions.getScreenDensity() + 0.5f);
        binding.settingsCard.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 2) * screenDimensions.getScreenDensity() + 0.5f);
        binding.statisticsCard.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 2) * screenDimensions.getScreenDensity() + 0.5f);

        binding.pushCard.setOnClickListener(v-> startActivity(new Intent(getContext(), PushActivity.class)));
        binding.settingsCard.setOnClickListener(v->startActivity(new Intent(getContext(), ProfileSettingsActivity.class)));

        return binding.getRoot();
    }

    //Note: Fragments outlive their views.
    //Make sure you clean up any references to the binding class instance in the fragment's onDestroyView() method.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}