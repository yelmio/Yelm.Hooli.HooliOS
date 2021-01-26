package yelm.io.yelm.old_version.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import yelm.io.yelm.databinding.ActivityPushBinding;

public class PushActivity extends AppCompatActivity {

    private ActivityPushBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityPushBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}