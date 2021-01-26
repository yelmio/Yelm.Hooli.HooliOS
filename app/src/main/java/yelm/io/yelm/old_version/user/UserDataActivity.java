package yelm.io.yelm.old_version.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_old.user.User;
import yelm.io.yelm.databinding.ActivityUserDataBinding;
import yelm.io.yelm.old_version.maps.MapActivity;

public class UserDataActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 5;

    EditText userName, userPhone, userAddress;
    EditText userOffice, userIntercom, userEntrance, userFloor;
    ImageButton openMap;
    Button saveUserData;

    ActivityUserDataBinding binding;

    String latitude = "", longitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
    }

    private void initViews() {
        User user = Common.userRepository.getUserById(0);
        binding.userName.setText(user.userName);
        binding.userPhone.setText(user.userPhone);
        binding.userAddress.setText(user.userAddress);
        binding.userOffice.setText(user.userOffice);
        binding.userIntercom.setText(user.userIntercom);
        binding.userEntrance.setText(user.userEntrance);
        binding.userFloor.setText(user.userFloor);
        latitude = user.latitude;
        longitude = user.longitude;

        binding.saveUserData.setOnClickListener(view -> {
            User user1 = new User(0,
                    binding.userName.getText().toString(),
                    binding.userPhone.getText().toString(),
                    binding.userAddress.getText().toString(),
                    binding.userOffice.getText().toString(),
                    binding.userIntercom.getText().toString(),
                    binding.userEntrance.getText().toString(),
                    binding.userFloor.getText().toString(),
                    latitude,
                    longitude);
            Common.userRepository.updateUser(user1);
            Log.d("AlexDebug", "user:" + user1.toString());
            Toast.makeText(UserDataActivity.this, getText(R.string.details_have_been_updated), Toast.LENGTH_SHORT).show();
        });

        binding.openMap.setOnClickListener(view -> startActivityForResult(new Intent(UserDataActivity.this, MapActivity.class), LOCATION_REQUEST_CODE));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                String addressLine = data.getStringExtra("addressLine");
                binding.userAddress.setText(addressLine);
                break;
        }
    }
}