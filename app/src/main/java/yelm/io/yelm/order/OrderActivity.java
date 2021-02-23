package yelm.io.yelm.order;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cloudpayments.sdk.three_ds.ThreeDSDialogListener;
import ru.cloudpayments.sdk.three_ds.ThreeDsDialogFragment;
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.order.promocode.PromoCode;
import yelm.io.yelm.order.promocode.PromoCodeClass;
import yelm.io.yelm.payment.PayApi;
import yelm.io.yelm.payment.PaymentActivity;
import yelm.io.yelm.payment.models.Transaction;
import yelm.io.yelm.payment.response.PayApiError;
import yelm.io.yelm.rest.rest_api.RestAPI;
import yelm.io.yelm.rest.client.RetrofitClient;
import yelm.io.yelm.support_stuff.Logging;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityOrderNewBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.payment.googleplay.PaymentsUtil;
import yelm.io.yelm.support_stuff.PhoneTextFormatter;

public class OrderActivity extends AppCompatActivity implements ThreeDSDialogListener {

    ActivityOrderNewBinding binding;

    private static final int PAYMENT_SUCCESS = 77;

    private PaymentsClient paymentsClient;

    private BigDecimal startCost = new BigDecimal("0");
    private BigDecimal finalCost = new BigDecimal("0");//without delivery cost
    private BigDecimal paymentCost = new BigDecimal("0");

    private BigDecimal deliveryCostStart = new BigDecimal("0");
    private BigDecimal deliveryCostFinal = new BigDecimal("0");
    private BigDecimal discountPromo = new BigDecimal("0");

    private String deliveryTime = "";
    UserAddress currentAddress;

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String transactionID = "0";
    private String order = "";
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    private String currency = LoaderActivity.settings.getString(LoaderActivity.CURRENCY, "");

    private static final String ENTRANCE = "ENTRANCE";
    private static final String FLOOR = "FLOOR";
    private static final String FLAT = "FLAT";
    private static final String PHONE = "PHONE";
    private static SharedPreferences userSettings;
    private static final String USER_PREFERENCES = "user_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userSettings = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            finalCost = new BigDecimal(args.getString("finalPrice"));
            startCost = finalCost;
            deliveryCostStart = new BigDecimal(args.getString("deliveryCost"));
            deliveryCostFinal = deliveryCostStart;

            paymentCost = finalCost.add(deliveryCostStart);

            deliveryTime = args.getString("deliveryTime");
            currentAddress = (UserAddress) args.getSerializable(UserAddress.class.getSimpleName());
            Log.d(Logging.debug, "startCost: " + startCost);
            Log.d(Logging.debug, "finalCost: " + finalCost);
            Log.d(Logging.debug, "paymentCost: " + paymentCost);
            Log.d(Logging.debug, "deliveryCost: " + discountPromo);
            Log.d(Logging.debug, "deliveryPrice: " + deliveryCostStart);
            Log.d(Logging.debug, "deliveryTime: " + deliveryTime);
            Log.d(Logging.debug, "currentAddress: " + currentAddress.toString());
        }

        binding();

        paymentsClient = PaymentsUtil.createPaymentsClient(this);
        checkIsReadyToPay();

        bindingChosePaymentType();

        binding.applyPromocode.setOnClickListener(v -> getPromoCode());
    }

    private void getPromoCode() {
        if (binding.promoCode.getText().toString().trim().isEmpty()) {
            showToast((String) getText(R.string.orderActivityEnterPromoCode));
        } else {
            RetrofitClient.
                    getClient(RestAPI.URL_API_MAIN)
                    .create(RestAPI.class)
                    .getPromoCode(binding.promoCode.getText().toString().trim(),
                            RestAPI.PLATFORM_NUMBER,
                            LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "")
                    ).enqueue(new Callback<PromoCodeClass>() {
                @Override
                public void onResponse(@NotNull Call<PromoCodeClass> call, @NotNull Response<PromoCodeClass> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.d(Logging.debug, " " + Constants.ShopID);
                            if (response.body().getStatus().equals("200")) {
                                setPromoCode(response.body().getPromocode());
                            }
                            showToast(response.body().getMessage());
                        } else {
                            Log.e(Logging.error, "Method getPromoCode() - by some reason response is null!");
                        }

                    } else {
                        Log.e(Logging.error, "Method getPromoCode() - response is not successful. " +
                                "Code: " + response.code() + "Message: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NotNull Call<PromoCodeClass> call, @NotNull Throwable t) {
                    Log.e(Logging.error, "Method getPromoCode() - failure: " + t.toString());
                }
            });
        }
    }

    private void setPromoCode(PromoCode promoCode) {
        binding.layoutDiscount.setVisibility(View.VISIBLE);
        finalCost = startCost;
        deliveryCostFinal = deliveryCostStart;
        discountPromo = new BigDecimal(promoCode.getAmount());
        Log.d(Logging.debug, "promoCode.getType(): " + promoCode.getType());
        switch (promoCode.getType()) {
            case "full":
                binding.discountPercent.setText(String.format("%s",
                        getText(R.string.orderDiscount)));
                binding.discountPrice.setText(String.format("%s %s", discountPromo,
                        LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                if (discountPromo.compareTo(finalCost) >= 0) {
                    finalCost = new BigDecimal("1");
                } else {
                    finalCost = finalCost.subtract(discountPromo);
                }
                break;
            case "delivery":
                if (deliveryCostFinal.compareTo(new BigDecimal("0")) == 0) {
                    binding.discountPercent.setText(String.format("%s - %s", getText(R.string.orderDiscountDelivery), getText(R.string.orderDiscountDeliveryAlreadyFree)));
                    break;
                }
                binding.discountPercent.setText(String.format("%s %s%%", getText(R.string.orderDiscountDelivery), discountPromo));
                BigDecimal discountDelivery = discountPromo.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                discountDelivery = discountDelivery.multiply(deliveryCostFinal).setScale(2, BigDecimal.ROUND_HALF_UP);
                binding.discountPrice.setText(String.format("%s %s", discountDelivery,
                        LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                deliveryCostFinal = deliveryCostFinal.subtract(discountDelivery);
                break;
            case "percent":
                binding.discountPercent.setText(String.format("%s %s%%", getText(R.string.orderDiscount), discountPromo));
                BigDecimal discount = discountPromo.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                discount = discount.multiply(finalCost).setScale(2, BigDecimal.ROUND_HALF_UP);
                binding.discountPrice.setText(String.format("%s", discount));
                finalCost = finalCost.subtract(discount);
                break;
        }
        paymentCost = finalCost.add(deliveryCostFinal);
        binding.finalPrice.setText(String.format("%s %s", finalCost.add(deliveryCostFinal), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        Log.d(Logging.debug, "finalCost: " + finalCost);
        Log.d(Logging.debug, "paymentCost: " + paymentCost);
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
        Log.d(Logging.debug, "jsonObjectItems: " + jsonObjectItems.toString());
        RetrofitClient.
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
                        "googlepay",
                        binding.floor.getText().toString(),
                        binding.entrance.getText().toString(),
                        finalCost.add(deliveryCostStart).toString(),
                        binding.phone.getText().toString(),
                        binding.flat.getText().toString(),
                        "delivery",
                        jsonObjectItems.toString(),
                        deliveryCostFinal.toString(),
                        currency
                ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(Logging.debug, "Method sendOrder() - response.code(): " + response.code());
                    Common.basketCartRepository.emptyBasketCart();
                    Intent intentGP = new Intent();
                    intentGP.putExtra("success", "googlePay");
                    setResult(RESULT_OK, intentGP);
                    finish();
                } else {
                    Log.e(Logging.error, "Method sendOrder() - response is not successful. " +
                            "Code: " + response.code() + "Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.e(Logging.error, "Method sendOrder() - failure: " + t.toString());
            }
        });
    }

    private boolean preparePayment() {
        String phone = binding.phone.getText().toString();
        Log.d(Logging.debug, "phone: " + phone);
        phone = phone.replaceAll("\\D", "");
        Log.d(Logging.debug, "phone after replacement: " + phone);
        if (phone.trim().equals("") || phone.length() != 11) {
            showToast(getText(R.string.orderActivityEnterCorrectPhone).toString());
            return false;
        }

        String floor = binding.floor.getText().toString();
        if (floor.trim().equals("")) {
            showToast(getText(R.string.orderActivityEnterFloor).toString());
            return false;
        }

        String entrance = binding.entrance.getText().toString();
        if (entrance.trim().equals("")) {
            showToast(getText(R.string.orderActivityEnterEntrance).toString());
            return false;
        }

        String flat = binding.flat.getText().toString();
        if (flat.trim().equals("")) {
            showToast(getText(R.string.orderActivityEnterFlat).toString());
            return false;
        }
        return true;
    }

    private void bindingChosePaymentType() {
        binding.cardPay.setOnClickListener(view -> {
            binding.cardPay.setCardBackgroundColor(getResources().getColor(R.color.mainThemeColor));
            binding.cardPayText.setTextColor(getResources().getColor(R.color.whiteColor));
            binding.googlepayPay.setCardBackgroundColor(Color.TRANSPARENT);
            binding.googlePayText.setTextColor(getResources().getColor(R.color.colorText));
            binding.paymentCard.setVisibility(View.VISIBLE);
            binding.googlePay.setVisibility(View.GONE);
        });
        binding.googlepayPay.setOnClickListener(view -> {
            binding.googlepayPay.setCardBackgroundColor(getResources().getColor(R.color.mainThemeColor));
            binding.googlePayText.setTextColor(getResources().getColor(R.color.whiteColor));
            binding.cardPay.setCardBackgroundColor(Color.TRANSPARENT);
            binding.cardPayText.setTextColor(getResources().getColor(R.color.colorText));
            binding.paymentCard.setVisibility(View.GONE);
            binding.googlePay.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case PAYMENT_SUCCESS:
                Intent intent = new Intent();
                intent.putExtra("success", "card");
                setResult(RESULT_OK, intent);
                finish();
                break;
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(Logging.debug, "Method payment with GooglePay(): RESULT_OK");
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(Logging.debug, "Method payment with GooglePay(): RESULT_CANCELED");
                        // Nothing to here normally - the user simply cancelled without selecting a payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Log.d(Logging.debug, "Method payment with GooglePay(): RESULT_ERROR");
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        if (status != null) {
                            handlePaymentError(status.getStatusCode());
                            Log.d(Logging.debug, "Method - status.getStatusMessage(): " + status.getStatusMessage());
                        } else {
                            Log.d(Logging.debug, "Method - status.getStatusMessage(): status is null");
                        }
                        break;
                }
                break;
        }
    }

    private void binding() {
        binding.startPrice.setText(String.format("%s %s", startCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.finalPrice.setText(String.format("%s %s", finalCost.add(deliveryCostStart), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.back.setOnClickListener(v -> finish());
        binding.deliveryPrice.setText(String.format("%s %s", deliveryCostStart, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.userAddress.setText(currentAddress.address);

        //set amount of products
        BigInteger productsAmount = new BigInteger("0");
        for (BasketCart basketCart : Common.basketCartRepository.getBasketCartsList()) {
            productsAmount = productsAmount.add(new BigInteger(basketCart.count));
        }
        binding.amountOfProducts.setText(String.format("%s %s %s", getText(R.string.inYourOrderProductsCount), productsAmount, getText(R.string.inYourOrderProductsPC)));

        binding.phone.addTextChangedListener(new PhoneTextFormatter(binding.phone, "+# (###) ###-##-##"));

        binding.paymentCard.setOnClickListener(v -> {
            if (preparePayment()) {
                Intent intent = new Intent(OrderActivity.this, PaymentActivity.class);
                intent.putExtra("startCost", startCost.toString());
                intent.putExtra("finalPrice", finalCost.toString());
                intent.putExtra("discountPromo", discountPromo.toString());
                intent.putExtra("deliveryCost", deliveryCostFinal.toString());
                intent.putExtra("deliveryTime", deliveryTime);
                intent.putExtra("order", "");
                intent.putExtra("floor", binding.floor.getText().toString());
                intent.putExtra("entrance", binding.entrance.getText().toString());
                intent.putExtra("phone", binding.phone.getText().toString());
                intent.putExtra("flat", binding.flat.getText().toString());
                intent.putExtra(UserAddress.class.getSimpleName(), currentAddress);
                startActivityForResult(intent, PAYMENT_SUCCESS);
            }
        });
    }

    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(paymentsClient).addOnCompleteListener(
                task -> {
                    try {
                        Log.d(Logging.debug, "isReadyToPay");
                        boolean result = task.getResult(ApiException.class);
                        setPwgAvailable(result);
                    } catch (ApiException exception) {
                        Log.d(Logging.debug, exception.toString());
                    }
                });
    }

    private void setPwgAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that Pay with Google is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        if (available) {
            bindingGooglePayButton();
        } else {
            binding.pwgStatus.setText(R.string.pwg_status_unavailable);
        }
    }

    private void bindingGooglePayButton() {
        binding.pwgStatus.setVisibility(View.GONE);
        binding.pwgButton.getRoot().setVisibility(View.VISIBLE);
        binding.pwgButton.getRoot().setOnClickListener(v -> {
            if (preparePayment()) {
//                //testing
//                sendOrder();
//                //testing
                if (Objects.equals(LoaderActivity.settings.getString(LoaderActivity.CURRENCY, ""), "RUB")) {
                    requestPayment(paymentsClient);
                } else {
                    convertPrice();
                }
            }
        });
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(PaymentsClient paymentsClient) {
        // Disables the button to prevent multiple clicks.
        //pwg_button.setClickable(false);
        Log.d(Logging.debug, "requestPayment");

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        String price = paymentCost.toString();

        TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(request);
        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, Objects.requireNonNull(this), LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    public void showLoading() {
        if (binding.progress.getVisibility() == View.VISIBLE) {
            return;
        }
        binding.progress.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (binding.progress.getVisibility() == View.GONE) {
            return;
        }
        binding.progress.setVisibility(View.GONE);
    }

    private void convertPrice() {
        Log.d(Logging.debug, "Method convertPrice()");
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN)
                .create(RestAPI.class)
                .convertPrice(
                        paymentCost.toString(),
                        LoaderActivity.settings.getString(LoaderActivity.CURRENCY, "")
                ).enqueue(new Callback<PriceConverterResponseClass>() {
            @Override
            public void onResponse(@NotNull Call<PriceConverterResponseClass> call, @NotNull Response<PriceConverterResponseClass> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(Logging.debug, "Method convertPrice() - paymentCost: " + response.body().getPrice());
                        paymentCost = new BigDecimal(response.body().getPrice());
                        requestPayment(paymentsClient);
                    } else {
                        Log.e(Logging.error, "Method convertPrice() - by some reason response is null!");
                    }
                } else {
                    Log.e(Logging.error, "Method convertPrice() - response is not successful. " +
                            "Code: " + response.code() + "Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<PriceConverterResponseClass> call, @NotNull Throwable t) {
                Log.e(Logging.error, "Method convertPrice() - failure: " + t.toString());
            }
        });
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();
        Log.d(Logging.debug, "token.toString()" + token.toString());

        // getPaymentMethodToken will only return null if Payment Method Tokenization Parameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();
            // Use token.getToken() to get the token string.
            Log.d(Logging.debug, "token.getToken()" + token.getToken());
            Log.d(Logging.debug, "Method handlePaymentSuccess() - paymentCost: " + paymentCost);
            charge(token.getToken(), "Google Pay", paymentCost, order);
        }
    }

    private void handlePaymentError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.d(Logging.debug, String.format("Error code: %d", statusCode));
    }

    // Запрос на проведение одностадийного платежа
    private void charge(String cardCryptogramPacket, String cardHolderName, BigDecimal
            paymentCost, String order) {
        compositeDisposable.add(PayApi
                .charge(cardCryptogramPacket, cardHolderName, paymentCost, order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showLoading())
                .doOnEach(notification -> hideLoading())
                .subscribe(transaction -> {
                    checkResponse(transaction);
                }, this::handleError));
    }

    // Проверяем необходимо ли подтверждение с использованием 3DS
    private void checkResponse(Transaction transaction) {
        if (transaction.getPaReq() != null && transaction.getAcsUrl() != null) {
            // Показываем 3DS форму
            Log.d(Logging.debug, "show3DS");
            show3DS(transaction);
        } else {
            // Показываем результат
            Log.d(Logging.debug, "transaction result: " + transaction.getCardHolderMessage());
            Log.d(Logging.debug, "transaction.getReasonCode(): " + transaction.getReasonCode());
            showToast(transaction.getCardHolderMessage());
            if (transaction.getReasonCode() == 0) {
                transactionID = transaction.getId();
                Log.d(Logging.debug, "transaction.getId(): " + transaction.getId());
                sendOrder();
            }
        }
    }

    @Override
    public void onAuthorizationCompleted(String md, String paRes) {
        post3ds(md, paRes);
    }

    @Override
    public void onAuthorizationFailed(String html) {
        Toast.makeText(this, "AuthorizationFailed: " + html, Toast.LENGTH_SHORT).show();
        Log.d(Logging.debug, "onAuthorizationFailed: " + html);
    }

    private void show3DS(Transaction transaction) {
        // Открываем 3ds форму
        ThreeDsDialogFragment.newInstance(transaction.getAcsUrl(),
                transaction.getId(),
                transaction.getPaReq())
                .show(this.getSupportFragmentManager(), "3DS");
    }

    // Завершаем транзакцию после прохождения 3DS формы
    private void post3ds(String md, String paRes) {
        compositeDisposable.add(PayApi
                .post3ds(md, paRes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showLoading())
                .doOnEach(notification -> hideLoading())
                .subscribe(transaction -> {
                    checkResponse(transaction);
                }, this::handleError));
    }

    public void handleError(Throwable throwable, Class... ignoreClasses) {
        if (ignoreClasses.length > 0) {
            List<Class> classList = Arrays.asList(ignoreClasses);
            if (classList.contains(throwable.getClass())) {
                return;
            }
        }
        if (throwable instanceof PayApiError) {
            PayApiError apiError = (PayApiError) throwable;
            String message = apiError.getMessage();
            Log.d(Logging.debug, "apiError.getMessage(): " + apiError.getMessage());
            showToast(message);
        } else if (throwable instanceof UnknownHostException) {
            Log.d(Logging.debug, "UnknownHostException: " + getString(R.string.common_no_internet_connection));
            showToast(getString(R.string.common_no_internet_connection));
        } else {
            Log.d(Logging.debug, "handleError: " + throwable.getMessage());
            showToast(throwable.getMessage());
        }
    }

    public void showToast(String message) {
        Log.d(Logging.debug, "message: " + message);

//        Snackbar snackbar = Snackbar.make(
//                findViewById(R.id.layout),
//                message,
//                Snackbar.LENGTH_SHORT);
//        snackbar.show();

        Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userSettings.contains(ENTRANCE)) {
            binding.entrance.setText(userSettings.getString(ENTRANCE, ""));
            binding.floor.setText(userSettings.getString(FLOOR, ""));
            binding.flat.setText(userSettings.getString(FLAT, ""));
            binding.phone.setText(userSettings.getString(PHONE, ""));
        }
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putString(ENTRANCE, binding.entrance.getText().toString());
        editor.putString(FLOOR, binding.floor.getText().toString());
        editor.putString(FLAT, binding.flat.getText().toString());
        editor.putString(PHONE, binding.phone.getText().toString());
        editor.apply();
        super.onStop();
    }
}