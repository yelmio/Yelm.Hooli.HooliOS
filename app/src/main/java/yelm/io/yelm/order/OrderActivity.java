package yelm.io.yelm.order;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import yelm.io.yelm.R;
import yelm.io.yelm.old_version.cashback.CashBackViewModel;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.database_old.user.User;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.old_version.maps.MapActivity;
import yelm.io.yelm.old_version.maps.MapShopActivity;
import yelm.io.yelm.payment.pages.CardPayFragment;
import yelm.io.yelm.payment.pages.CashPayFragment;

public class OrderActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    LinearLayout extraData, bonusLayout;
    EditText userName, userPhone, userAddress;
    EditText userOffice, userIntercom, userEntrance, userFloor;
    TextView pickupAddress, textViewTotal, minOrderPrice, progressBonus, bonusButton, strikeThroughTotal;

    ImageButton openMap, addressPickupButton, deliveryButton;
    TabLayout tabLayout;
    ViewPager pager;

    SeekBar seekBar;

    Switch bonusSwitch;

    private String WillTakeSelf = "false";
    private String PayentType = "";

    private static final int LOCATION_REQUEST_CODE = 12;
    private static final int ADDRESS_PICKUP_REQUEST_CODE = 2;

    Button getAddressPickup, payButton;

    BigDecimal bigTotal = new BigDecimal("0");
    BigDecimal bigTotalAfterBonus = new BigDecimal("0");
    BigDecimal bigProductCount = new BigDecimal("0");

    private CashBackViewModel cashBackModel;

    private String order = "";
    private String md5 = "";
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    private String allowPayments = LoaderActivity.settings.getString(LoaderActivity.ALLOW_PAYMENTS, "");
    private BigDecimal deliveryPrice = new BigDecimal(LoaderActivity.settings.getString(LoaderActivity.MIN_DELIVERY_PRICE, ""));
    private String transactionID = "";

    private String status_WantToUseCashBack = "false";

    String latitude = "", longitude = "", companyID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        initViews();
        cashBackModel = new ViewModelProvider(this).get(CashBackViewModel.class);
        bonusLayout.setVisibility(cashBackModel.getCashBack().getValue().compareTo(new BigDecimal("0")) == 0 ? View.GONE : View.VISIBLE);

        List<Cart> products = Common.cartRepository.getCartItemsList();
        for (int i = 0; i < products.size(); i++) {
            BigDecimal bigCount = new BigDecimal(products.get(i).count);
            BigDecimal bigPrice = new BigDecimal(products.get(i).price);
            bigTotal = bigTotal.add(bigCount.multiply(bigPrice));
        }

        bigTotalAfterBonus = bigTotal;

//        if (bigTotalAfterBonus.compareTo(cashBackModel.getCashBack().getValue()) < 0) {
//            seekBar.setMax(bigTotalAfterBonus.intValue());
//        } else {
//            seekBar.setMax(cashBackModel.getCashBack().getValue().intValue());
//        }

        //set bonuses
        bonusSwitch.setText(new StringBuilder().append(getText(R.string.bonus_point))
                .append(" ")
                .append(cashBackModel.getCashBack().getValue().toString())
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        strikeThroughTotal.setText(bigTotal.add(deliveryPrice).toString());

//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                progressBonus.setText(String.valueOf(seekBar.getProgress()));
//                bigTotalAfterBonus = bigTotal;
//                if (WillTakeSelf.equals("false")) {
//                    bigTotalAfterBonus = bigTotalAfterBonus.add(new BigInteger(deliveryPrice));
//                }
//                bigTotalAfterBonus = bigTotalAfterBonus.subtract(new BigInteger(String.valueOf(seekBar.getProgress())));
//                //price.setText(fullPrice.toString());
//                //pay.setText(total.toString());
//                textViewTotal.setText(getResources().getText(R.string.payment2) + " " + bigTotalAfterBonus + " " + getResources().getText(R.string.ruble_sign));
//            }
//        });

        bigTotalAfterBonus = bigTotalAfterBonus.add(deliveryPrice);

        Log.d("AlexDebug", "bigTotalAfterBonus: " + bigTotalAfterBonus.toString());

        minOrderPrice.setText(new StringBuilder().append(deliveryPrice.toString())
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")).toString());

        if (allowPayments.equals("true")) {
            tabLayout.setupWithViewPager(pager);
            MyAdapter adapter = new MyAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            //pager.setCurrentItem(1); // выводим второй экран
        } else {
            payButton.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
        }
        textViewTotal.setText(new StringBuilder().append(bigTotalAfterBonus.toString())
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")).toString());
    }

    public class MyAdapter extends FragmentPagerAdapter {
        //private String tabTitles[] = new String[]{"Google Pay", "Карта", "Наличные"};

        private String tabTitles[] = new String[]{getText(R.string.card).toString(), getText(R.string.cash).toString()};

        MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
//                case 0:
//                    return new GooglePayFragment();
                case 0:
                    return new CardPayFragment();
                default:
                    return new CashPayFragment();
            }
        }
    }

    @Override
    protected void onStart() {
        User user = Common.userRepository.getUserById(0);
        userName.setText(user.userName);
        userPhone.setText(user.userPhone);
        userAddress.setText(user.userAddress);
        userOffice.setText(user.userOffice);
        userIntercom.setText(user.userIntercom);
        userEntrance.setText(user.userEntrance);
        userFloor.setText(user.userFloor);
        latitude = user.latitude;
        longitude = user.longitude;
        super.onStart();
    }

    @Override
    protected void onStop() {
        User user = new User(0,
                userName.getText().toString(),
                userPhone.getText().toString(),
                userAddress.getText().toString(),
                userOffice.getText().toString(),
                userIntercom.getText().toString(),
                userEntrance.getText().toString(),
                userFloor.getText().toString(),
                latitude,
                longitude);
        Common.userRepository.updateUser(user);
        super.onStop();
    }

    private void initViews() {
        userName = findViewById(R.id.userName);
        minOrderPrice = findViewById(R.id.minOrderPrice);
        userPhone = findViewById(R.id.userPhone);
        userAddress = findViewById(R.id.userAddress);
        userOffice = findViewById(R.id.userOffice);
        userIntercom = findViewById(R.id.userIntercom);
        userEntrance = findViewById(R.id.userEntrance);
        userFloor = findViewById(R.id.userFloor);
        extraData = findViewById(R.id.extraData);
        bonusLayout = findViewById(R.id.bonusLayout);
        openMap = findViewById(R.id.openMap);
        textViewTotal = findViewById(R.id.textViewTotal);
        strikeThroughTotal = findViewById(R.id.strikeThroughTotal);
        addressPickupButton = findViewById(R.id.addressButton);
        deliveryButton = findViewById(R.id.deliveryButton);
        getAddressPickup = findViewById(R.id.getAddressPickup);
        pickupAddress = findViewById(R.id.pickupAddress);
        payButton = findViewById(R.id.payButton);
        tabLayout = findViewById(R.id.tabLayout);
        pager = findViewById(R.id.pager);
        seekBar = findViewById(R.id.seekBar);
        progressBonus = findViewById(R.id.progressBonus);
        bonusButton = findViewById(R.id.bonusButton);
        bonusSwitch = findViewById(R.id.bonusSwitch);

        strikeThroughTotal.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);


//        bonusButton.setOnClickListener(v -> {
//            bonusLayout.setVisibility(bonusLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//        });

//        cancel.setOnClickListener(v -> {
//            bigTotalAfterBonus = WillTakeSelf.equals("false") ? bigTotal.add(new BigDecimal(deliveryPrice)) : bigTotal;
//            seekBar.setProgress(0);
//            textViewTotal.setText(getResources().getText(R.string.payment2) + " " + bigTotalAfterBonus + " " + getResources().getText(R.string.ruble_sign));
//            progressBonus.setText("0");
//        });

        getAddressPickup.setOnClickListener(v -> startActivityForResult(new Intent(OrderActivity.this, MapShopActivity.class), ADDRESS_PICKUP_REQUEST_CODE));

        payButton.setOnClickListener(v -> cashPayOrder());

        openMap.setOnClickListener(v -> startActivityForResult(new Intent(OrderActivity.this, MapActivity.class), LOCATION_REQUEST_CODE));

        addressPickupButton.setOnClickListener(v -> {
            WillTakeSelf = "true";
            extraData.setVisibility(View.GONE);
            addressPickupButton.setClickable(false);
            deliveryButton.setClickable(true);
            deliveryButton.setImageResource(R.drawable.ic_pin_gray_24);
            addressPickupButton.setImageResource(R.drawable.ic_pin_blue_24);

            strikeThroughTotal.setText(bigTotal.toString());

            bigTotalAfterBonus = bigTotal;
            getAddressPickup.setVisibility(View.VISIBLE);
            if (status_WantToUseCashBack.equals("true")) {
                if (cashBackModel.getCashBack().getValue().compareTo(bigTotalAfterBonus) >= 0) {
                    bigTotalAfterBonus = new BigDecimal("0");
                } else {
                    bigTotalAfterBonus = bigTotalAfterBonus.subtract(cashBackModel.getCashBack().getValue());
                }
            }
            textViewTotal.setText(new StringBuilder().append(bigTotalAfterBonus.toString())
                    .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")).toString());
        });

        deliveryButton.setOnClickListener(v -> {
            WillTakeSelf = "false";
            extraData.setVisibility(View.VISIBLE);
            addressPickupButton.setClickable(true);
            deliveryButton.setClickable(false);
            addressPickupButton.setImageResource(R.drawable.ic_pin_gray_24);
            deliveryButton.setImageResource(R.drawable.ic_pin_blue_24);

            strikeThroughTotal.setText(bigTotal.add(deliveryPrice).toString());

            bigTotalAfterBonus = bigTotal.add(deliveryPrice);
            getAddressPickup.setVisibility(View.GONE);
            if (status_WantToUseCashBack.equals("true")) {
                if (cashBackModel.getCashBack().getValue().compareTo(bigTotalAfterBonus) >= 0) {
                    bigTotalAfterBonus = new BigDecimal("0");
                } else {
                    bigTotalAfterBonus = bigTotalAfterBonus.subtract(cashBackModel.getCashBack().getValue());
                }
            }
            textViewTotal.setText(new StringBuilder().append(bigTotalAfterBonus.toString())
                    .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")).toString());
        });

        bonusSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                strikeThroughTotal.setVisibility(View.VISIBLE);
                status_WantToUseCashBack = "true";
                if (cashBackModel.getCashBack().getValue().compareTo(bigTotalAfterBonus) >= 0) {
                    bigTotalAfterBonus = new BigDecimal("0");
                } else {
                    bigTotalAfterBonus = bigTotalAfterBonus.subtract(cashBackModel.getCashBack().getValue());
                }
            } else {
                strikeThroughTotal.setVisibility(View.GONE);
                status_WantToUseCashBack = "false";
                bigTotalAfterBonus = bigTotal;
                if (WillTakeSelf.equals("false")) {
                    bigTotalAfterBonus = bigTotalAfterBonus.add(deliveryPrice);
                }
            }
            textViewTotal.setText(new StringBuilder().append(bigTotalAfterBonus.toString())
                    .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")).toString());
        });
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
                if (data.getStringExtra("latitude").equals("") || data.getStringExtra("longitude").equals("")) {
                } else {
                    latitude = data.getStringExtra("latitude");
                    longitude = data.getStringExtra("longitude");
                }
                Log.d("AlexDebug", "latitude: " + latitude);
                Log.d("AlexDebug", "longitude: " + longitude);
                userAddress.setText(addressLine);
                User user = Common.userRepository.getUserById(0);
                user.userAddress = addressLine;
                user.latitude = latitude;
                user.longitude = longitude;
                Common.userRepository.updateUser(user);
                break;
            case ADDRESS_PICKUP_REQUEST_CODE:
                companyID = data.getStringExtra("companyID");
                Log.d("AlexDebug", "companyID: " + companyID);
                pickupAddress.setText(data.getStringExtra("pickupAddress"));
                break;
        }
    }

    private boolean makeOrder() {
        JSONObject jsonData = new JSONObject();
        String FIO = userName.getText().toString();
        if (FIO.trim().equals("")) {
            showToast(getText(R.string.enter_full_name).toString());
            return false;
        }
        String Phone = userPhone.getText().toString();
        if (Phone.trim().equals("")) {
            showToast(getText(R.string.enter_phone).toString());
            return false;
        }
        String Address = userAddress.getText().toString();
        if (WillTakeSelf.equals("false")) {
            if (Address.trim().equals("")) {
                showToast(getText(R.string.enter_delivery_address).toString());
                return false;
            }
        }
        String CompanyAddress = pickupAddress.getText().toString();
        if (WillTakeSelf.equals("true")) {
            if (CompanyAddress.trim().equals("")) {
                showToast(getText(R.string.enter_pickup_point).toString());
                return false;
            }
        }
        try {
            jsonData.put("FIO", FIO);
            jsonData.put("Phone", Phone);
            jsonData.put("Adress", Address);
            jsonData.put("CompanyAdress", CompanyAddress);
            jsonData.put("WillTakeSelf", WillTakeSelf);
            jsonData.put("TextMore", "");
            jsonData.put("Status", "new");
            jsonData.put("PayentType", PayentType);
            jsonData.put("CompanyID", companyID);
            jsonData.put("status_WantToUseCashBack", status_WantToUseCashBack);
            jsonData.put("spentUserCashBack", cashBackModel.getCashBack().getValue().toString());
            jsonData.put("Office", userOffice.getText().toString());
            jsonData.put("Floor", userFloor.getText().toString());
            jsonData.put("Entrance", userEntrance.getText().toString());
            jsonData.put("Intercom", userIntercom.getText().toString());
            jsonData.put("Point", latitude + ";" + longitude);
            jsonData.put("FinalPrice", bigTotalAfterBonus);
            List<Cart> products = Common.cartRepository.getCartItemsList();
            JSONArray jsonObjectItems = new JSONArray();
            for (int i = 0; i < products.size(); i++) {
                BigDecimal fullPrice = new BigDecimal(products.get(i).price).multiply(new BigDecimal(products.get(i).count));
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem
                        .put("Item", products.get(i).item)
                        .put("Name", products.get(i).name)
                        .put("Quantity", products.get(i).quantity)
                        .put("Type", products.get(i).type)
                        .put("Price", products.get(i).price)
                        .put("Count", products.get(i).count)
                        .put("Image", products.get(i).imageUrl)
                        .put("FullPrice", fullPrice);
                jsonObjectItems.put(jsonObjectItem);
            }
            jsonData.put("Items", jsonObjectItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        order = jsonData.toString();
        Log.d("AlexDebug", "order:" + order);
        md5 = md5("45yu3d7h9Jca0ppq2l" + order);
        return true;
    }

    public void cashPayOrder() {
        PayentType = "Self";
        if (makeOrder()) {
            createDialogSettingsTable();
        }
    }

    private void createDialogSettingsTable() {
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialog = inflater.inflate(R.layout.dialog_confirm_cashpay, null);
        Button confirm = dialog.findViewById(R.id.confirm);
        Button cancel = dialog.findViewById(R.id.cancel);
        TextView totalDialogTextView = dialog.findViewById(R.id.totalDialogText);
        TextView deliveryText = dialog.findViewById(R.id.deliveryText);

        deliveryText.setText(WillTakeSelf.equals("false")
                ? (getText(R.string.delivery) + ": " + userAddress.getText().toString())
                : (getText(R.string.pickup_point) + ": " + pickupAddress.getText().toString()));

        totalDialogTextView.setText(new StringBuilder().append(getResources().getText(R.string.payment2))
                .append(" ")
                .append(bigTotalAfterBonus)
                .append(" ")
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogInfo);
        builder.setView(dialog);
        AlertDialog alert = builder.create();

        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        confirm.setOnClickListener(view -> {


        });
        cancel.setOnClickListener((v) -> alert.cancel());
        alert.show();
    }

    public void googlePayOrder(PaymentsClient paymentsClient) {
        PayentType = "Google Pay";
        if (makeOrder()) {

        }
    }

    public void cardPayOrder() {
        PayentType = "Card";
        if (makeOrder()) {

        }
    }

    private String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void showToast(String message) {
        Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}