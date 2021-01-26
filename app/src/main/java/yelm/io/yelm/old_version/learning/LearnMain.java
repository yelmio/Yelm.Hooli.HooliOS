package yelm.io.yelm.old_version.learning;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityLearnMainBinding;
import yelm.io.yelm.main.controller.MainActivity;
import yelm.io.yelm.retrofit.DynamicURL;

public class LearnMain extends AppCompatActivity {

    private static final TimeInterpolator GAUGE_ANIMATION_INTERPOLATOR = new DecelerateInterpolator(2);
    private static final int PROGRESS_LEVEL = 25;
    private static final long GAUGE_ANIMATION_DURATION = 1000;
    private static Integer indicator = 25;
    private Integer counter = 1;

    ActivityLearnMainBinding binding;
    List<Fragment> listFragments = new ArrayList<>(Arrays.asList(
            new FirstLearnFragment(),
            new SecondLearnFragment(),
            new ThirdLearnFragment(),
            new FourthLearnFragment()
            //new FifthLearnFragment()
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearnMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, listFragments.get(0))
                .commit();
        binding();
    }

    private void binding() {
        binding.nextButton.setOnClickListener(v -> {
            startProgress();
            if (counter == 3) {
                binding.nextButton.setText(getText(R.string.make_app));
                binding.nextButton.setOnClickListener(view -> {
                    DynamicURL.setPLATFORM("yelmio");
                    startActivity(new Intent(LearnMain.this, MainActivity.class));
                    finish();
                });
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, listFragments.get(counter))
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, listFragments.get(counter))
                        .commit();
                counter++;
            }
        });
    }

    //by click we animate progress line
    public void startProgress() {
//        ObjectAnimator animator = ObjectAnimator.ofInt(binding.progressBar, "progress", indicator, indicator+=20);
//        animator.setInterpolator(GAUGE_ANIMATION_INTERPOLATOR);
//        animator.setDuration(GAUGE_ANIMATION_DURATION);
//        animator.start();

//        ObjectAnimator animation1 = ObjectAnimator.ofInt(binding.progress,
//                "progress",
//                binding.progress.getProgress(),
//                indicator += PROGRESS_LEVEL);
//        animation1.setDuration(GAUGE_ANIMATION_DURATION);
//        animation1.setInterpolator(new DecelerateInterpolator());
//        animation1.start();
        ObjectAnimator animation2 = ObjectAnimator.ofInt(binding.progress,
                "progress",
                binding.progress.getProgress(),
                indicator += PROGRESS_LEVEL);
        animation2.setDuration(GAUGE_ANIMATION_DURATION);
        animation2.setInterpolator(new AccelerateInterpolator());
        animation2.start();
    }
}