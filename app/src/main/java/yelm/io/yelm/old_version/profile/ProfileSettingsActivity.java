package yelm.io.yelm.old_version.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import yelm.io.yelm.databinding.ActivityProfileSettingsBinding;

public class ProfileSettingsActivity extends AppCompatActivity {

    ActivityProfileSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}