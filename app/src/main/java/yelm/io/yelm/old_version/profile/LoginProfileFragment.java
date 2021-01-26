package yelm.io.yelm.old_version.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import yelm.io.yelm.databinding.FragmentLoginProfileBinding;

public class LoginProfileFragment extends Fragment {


    private FragmentLoginProfileBinding binding;


    public LoginProfileFragment() {
        // Required empty public constructor
    }

    public static LoginProfileFragment newInstance() {
        LoginProfileFragment fragment = new LoginProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginProfileBinding.inflate(inflater, container, false);

        String userCode = "";

        binding.sendCode.setOnClickListener(v -> {

            //Log.d("AlexDebug", "IS_LOGIN: " + LoaderActivity.settings.getBoolean(LoaderActivity.IS_LOGIN, false));

            //LoaderActivity.settings.edit().putBoolean(LoaderActivity.IS_LOGIN, true).apply();

            //Log.d("AlexDebug", "IS_LOGIN: " + LoaderActivity.settings.getBoolean(LoaderActivity.IS_LOGIN, false));


           // getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ProfileFragment.newInstance()).commit();

            binding.progress.setVisibility(View.VISIBLE);
            Random random = new Random();
            String code = String.format("%04d", random.nextInt(10000));
            Log.d("AlexDebug", "code: " + code);

            binding.sendCode.setVisibility(View.GONE);
            binding.layoutLogin.setVisibility(View.VISIBLE);

            if (userCode.equals(code)) {

            }

        });

        binding.repeatCode.setOnClickListener(v -> {
            binding.progress.setVisibility(View.VISIBLE);
            Random random = new Random();
            String code = String.format("%04d", random.nextInt(10000));
            Log.d("AlexDebug", "code: " + code);


            if (userCode.equals(code)) {

            }
        });


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