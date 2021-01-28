package yelm.io.yelm.payment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.disposables.CompositeDisposable;
import ru.cloudpayments.sdk.cp_card.CPCard;
import ru.cloudpayments.sdk.cp_card.api.CPCardApi;
import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;


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
    BigDecimal bigTotal = new BigDecimal("0");
    private String order;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    private String transactionID;

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

        Intent intent = getIntent();
        order = intent.getStringExtra("Order");
        bigTotal = new BigDecimal(intent.getStringExtra("Price"));

        Log.d("AlexDebug", "Total: " + bigTotal);
        Log.d("AlexDebug", "order: " + order);

        textViewTotal.setText(new StringBuilder()
                .append(bigTotal)
                .append(" ")
                .append(LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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