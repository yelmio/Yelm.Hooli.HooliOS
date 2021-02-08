package yelm.io.yelm.payment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cloudpayments.sdk.cp_card.CPCard;
import ru.cloudpayments.sdk.cp_card.api.CPCardApi;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.AlexTAG;


public class PaymentActivity extends AppCompatActivity {

    private static final int CARD_NUMBER_TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
    private static final int CARD_NUMBER_TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
    private static final int CARD_NUMBER_DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
    private static final int CARD_NUMBER_DIVIDER_POSITION = CARD_NUMBER_DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
    private static final char CARD_NUMBER_DIVIDER = ' ';

    private static final int CARD_DATE_TOTAL_SYMBOLS = 5; // size of pattern MM/YY
    private static final int CARD_DATE_TOTAL_DIGITS = 4; // max numbers of digits in pattern: MM + YY
    private static final int CARD_DATE_DIVIDER_MODULO = 3; // means divider position is every 3rd symbol beginning with 1
    private static final int CARD_DATE_DIVIDER_POSITION = CARD_DATE_DIVIDER_MODULO - 1; // means divider position is every 2nd symbol beginning with 0
    private static final char CARD_DATE_DIVIDER = '/';

    private static final int CARD_CVC_TOTAL_SYMBOLS = 3;

    //order dara
    private BigDecimal finalCost = new BigDecimal("0");
    private BigDecimal deliveryCost = new BigDecimal("0");
    private BigDecimal startCost = new BigDecimal("0");
    private BigDecimal discountPromo = new BigDecimal("0");
    private String order = "";
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    private String transactionID;
    private String currency = LoaderActivity.settings.getString(LoaderActivity.CURRENCY, "");
    UserAddress currentAddress;
    //private String deliveryTime = "";
    private String entrance = "";
    private String floor = "";
    private String flat = "";
    private String phone = "";

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String deliveryPrice = LoaderActivity.settings.getString(LoaderActivity.MIN_DELIVERY_PRICE, "");


    @BindView(R.id.text_total)
    TextView textViewTotal;

    @BindView(R.id.edit_card_number)
    EditText editTextCardNumber;

    @OnTextChanged(value = R.id.edit_card_number, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardNumberTextChanged(Editable s) {
        if (!isInputCorrect(s, CARD_NUMBER_TOTAL_SYMBOLS, CARD_NUMBER_DIVIDER_MODULO, CARD_NUMBER_DIVIDER)) {
            s.replace(0, s.length(), concatString(getDigitArray(s, CARD_NUMBER_TOTAL_DIGITS), CARD_NUMBER_DIVIDER_POSITION, CARD_NUMBER_DIVIDER));
        }
    }

    @BindView(R.id.edit_card_date)
    EditText editTextCardDate;

    @OnTextChanged(value = R.id.edit_card_date, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardDateTextChanged(Editable s) {
        if (!isInputCorrect(s, CARD_DATE_TOTAL_SYMBOLS, CARD_DATE_DIVIDER_MODULO, CARD_DATE_DIVIDER)) {
            s.replace(0, s.length(), concatString(getDigitArray(s, CARD_DATE_TOTAL_DIGITS), CARD_DATE_DIVIDER_POSITION, CARD_DATE_DIVIDER));
        }
    }

    @BindView(R.id.edit_card_cvc)
    EditText editTextCardCVC;

    @OnTextChanged(value = R.id.edit_card_cvc, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardCVCTextChanged(Editable s) {
        if (s.length() > CARD_CVC_TOTAL_SYMBOLS) {
            s.delete(CARD_CVC_TOTAL_SYMBOLS, s.length());
        }
    }

    @BindView(R.id.edit_card_holder_name)
    EditText editTextCardHolderName;

    @OnClick(R.id.back)
    void onBackClick() {
        finish();
    }

    @OnClick(R.id.button_payment)
    void onPaymentClick() {
        String cardNumber = editTextCardNumber.getText().toString().replace(" ", "");
        String cardDate = editTextCardDate.getText().toString().replace("/", "");
        String cardCVC = editTextCardCVC.getText().toString();
        String cardHolderName = editTextCardHolderName.getText().toString();

        CPCardApi api = new CPCardApi(this);

        // Проверям номер карты.
        if (!CPCard.isValidNumber(cardNumber)) {
            showToast(getString(R.string.checkout_error_card_number));
            return;
        }

        // Проверям срок действия карты.
        if (!CPCard.isValidExpDate(cardDate)) {
            showToast(getString(R.string.checkout_error_card_date));
            return;
        }

        // Проверям cvc код карты.
        if (cardCVC.length() != 3) {
            showToast(getString(R.string.checkout_error_card_cvc));
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_main);
        ButterKnife.bind(this);


        Bundle args = getIntent().getExtras();
        if (args != null) {
            finalCost = new BigDecimal(args.getString("finalPrice"));
            startCost = finalCost;
            deliveryCost = new BigDecimal(args.getString("deliveryCost"));
            discountPromo = new BigDecimal(args.getString("discountPromo"));
            floor = args.getString("floor");
            entrance = args.getString("entrance");
            phone = args.getString("phone");
            flat = args.getString("flat");
            //deliveryTime = args.getString("deliveryTime");
            currentAddress = (UserAddress) args.getSerializable(UserAddress.class.getSimpleName());

            Log.d(AlexTAG.debug, "finalCost: " + finalCost);
            Log.d(AlexTAG.debug, "startCost: " + startCost);
            Log.d(AlexTAG.debug, "deliveryCost: " + discountPromo);
            Log.d(AlexTAG.debug, "deliveryPrice: " + deliveryCost);
            Log.d(AlexTAG.debug, "floor: " + floor);
            Log.d(AlexTAG.debug, "entrance: " + entrance);
            Log.d(AlexTAG.debug, "phone: " + phone);
            Log.d(AlexTAG.debug, "flat: " + flat);
            //Log.d(AlexTAG.debug, "deliveryTime: " + deliveryTime);
            Log.d(AlexTAG.debug, "currentAddress: " + currentAddress.toString());
        }

        textViewTotal.setText(new StringBuilder()
                .append(finalCost)
                .append(" ")
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendOrder() {
        List<BasketCart> basketCarts = Common.basketCartRepository.getBasketCartsList();
        JSONArray jsonObjectItems = new JSONArray();
        try {
            for (int i = 0; i < basketCarts.size(); i++) {
                BigDecimal fullPrice = new BigDecimal(basketCarts.get(i).finalPrice).multiply(new BigDecimal(basketCarts.get(i).count));
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem
                        .put("id", basketCarts.get(i).itemID)
                        .put("count", basketCarts.get(i).count);
                jsonObjectItems.put(jsonObjectItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(AlexTAG.debug, "jsonObjectItems: " + jsonObjectItems.toString());
        RetrofitClientNew.
                getClient(RestAPI.URL_API_MAIN)
                .create(RestAPI.class)
                .sendOrder("3",
                        getResources().getConfiguration().locale.getCountry(),
                        getResources().getConfiguration().locale.getLanguage(),
                        RestAPI.PLATFORM_NUMBER,
                        currentAddress.latitude,
                        currentAddress.longitude,
                        "test",
                        startCost.toString(),
                        discountPromo.toString(),
                        transactionID,
                        userID,
                        currentAddress.address,
                        "Card",
                        floor,
                        entrance,
                        finalCost.toString(),
                        phone,
                        flat,
                        "delivery",
                        jsonObjectItems.toString(),
                        deliveryCost.toString(),
                        currency
                ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(AlexTAG.debug, "Method sendOrder() - response.code(): " + response.code());
                } else {
                    Log.e(AlexTAG.error, "Method sendOrder() - response is not successful. " +
                            "Code: " + response.code() + "Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.e(AlexTAG.error, "Method sendOrder() - failure: " + t.toString());
            }
        });
    }


    private boolean isInputCorrect(Editable s, int size, int dividerPosition, char divider) {
        boolean isCorrect = s.length() <= size;
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (i + 1) % dividerPosition == 0) {
                isCorrect &= divider == s.charAt(i);
            } else {
                isCorrect &= Character.isDigit(s.charAt(i));
            }
        }
        return isCorrect;
    }

    private String concatString(char[] digits, int dividerPosition, char divider) {
        final StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length; i++) {
            if (digits[i] != 0) {
                formatted.append(digits[i]);
                if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                    formatted.append(divider);
                }
            }
        }

        return formatted.toString();
    }

    private char[] getDigitArray(final Editable s, final int size) {
        char[] digits = new char[size];
        int index = 0;
        for (int i = 0; i < s.length() && index < size; i++) {
            char current = s.charAt(i);
            if (Character.isDigit(current)) {
                digits[index] = current;
                index++;
            }
        }
        return digits;
    }
}