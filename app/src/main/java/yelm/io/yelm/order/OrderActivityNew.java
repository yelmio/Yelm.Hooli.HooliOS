package yelm.io.yelm.order;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import ru.cloudpayments.sdk.three_ds.ThreeDsDialogFragment;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.loader.model.UserLoginResponse;
import yelm.io.yelm.old_version.maps.MapActivity;
import yelm.io.yelm.payment.PayApi;
import yelm.io.yelm.payment.models.Transaction;
import yelm.io.yelm.payment.response.PayApiError;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityOrderNewBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.payment.PaymentActivity;
import yelm.io.yelm.payment.googleplay.PaymentsUtil;
import yelm.io.yelm.support_stuff.PhoneTextFormatter;

public class OrderActivityNew extends AppCompatActivity {

    ActivityOrderNewBinding binding;

    private static final int PAYMENT_SUCCESS = 77;

    private PaymentsClient paymentsClient;
    BigDecimal bigTotal = new BigDecimal("0");
    private BigDecimal deliveryCost = new BigDecimal("0");
    private String deliveryTime = "";

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String transactionID = "";
    private String order = "";
    private String md5 = "";
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");

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

        bigTotal = new BigDecimal(getIntent().getStringExtra("finalPrice"));
        deliveryCost = new BigDecimal(getIntent().getStringExtra("deliveryCost"));
        deliveryTime = getIntent().getStringExtra("deliveryTime");
        binding();

        paymentsClient = PaymentsUtil.createPaymentsClient(this);
        checkIsReadyToPay();

        bindingChosePaymentType();

        testOrder();
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

    private void testOrder() {
        //payment = ['Card', 'GooglePay', 'ApplePay]
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
                .sendOrder("+79856185757",
                        "test",
                        "Moscow",
                        "45",
                        "4",
                        "45",
                        "4",
                        "546.50",
                        "0",
                        jsonObjectItems.toString(),
                        "delivery",
                        "Card",
                        RestAPI.PLATFORM_NUMBER,
                        "3",
                        "4353363463"
                )

                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                            } else {
                                Log.e(AlexTAG.error, "Method checkUser() - by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method checkUser() - response is not successful. " +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method checkUser() - failure: " + t.toString());
                    }
                });
    }


    private boolean makeOrder(String paymentType) {
        String phone = binding.phone.getText().toString();
        Log.d("AlexDebug", "phone: " + phone);
        phone = phone.replaceAll("\\D", "");
        Log.d("AlexDebug", "phone after replacement: " + phone);
        if (phone.trim().equals("") || phone.length() != 11) {
            showToast(getText(R.string.orderActivityEnterCorrectPhone).toString());
            return false;
        }

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("phone", phone);
            for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
                if (userAddress.isChecked) {
                    binding.userAddress.setText(userAddress.address);
                    jsonData.put("address", userAddress.address);
                    break;
                }
            }

            jsonData.put("payment", paymentType);
            jsonData.put("delivery", "delivery");
            jsonData.put("flat", binding.flat.getText().toString());
            jsonData.put("floor", binding.floor.getText().toString());
            jsonData.put("entrance", binding.entrance.getText().toString());
            jsonData.put("total", getIntent().getStringExtra("finalPrice"));
            List<BasketCart> basketCarts = Common.basketCartRepository.getBasketCartsList();
            JSONArray jsonObjectItems = new JSONArray();
            for (int i = 0; i < basketCarts.size(); i++) {
                BigDecimal fullPrice = new BigDecimal(basketCarts.get(i).finalPrice).multiply(new BigDecimal(basketCarts.get(i).count));
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem
                        .put("item_id", basketCarts.get(i).itemID)
                        //.put("name", basketCarts.get(i).name)
                        //.put("Quantity", basketCarts.get(i).quantity)
                        //.put("startPrice", basketCarts.get(i).startPrice)
                        .put("price_item", basketCarts.get(i).finalPrice)
                        //.put("type", basketCarts.get(i).type)
                        .put("quantity_item", basketCarts.get(i).count)
                        //.put("imageUrl", basketCarts.get(i).imageUrl)
                        //.put("quantityType", basketCarts.get(i).quantityType)
                        .put("discount_item", basketCarts.get(i).discount
                                //.put("fullPrice", fullPrice
                        );
                jsonObjectItems.put(jsonObjectItem);
            }
            jsonData.put("items", jsonObjectItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        order = jsonData.toString();
        Log.d("AlexDebug", "order:" + order);
        md5 = md5("45yu3d7h9Jca0ppq2l" + order);

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
                intent.putExtra("success", "card payment");
                setResult(RESULT_OK, intent);
                finish();
                break;
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(AlexTAG.debug, "Method payment with GooglePay(): RESULT_OK");
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(AlexTAG.debug, "Method payment with GooglePay(): RESULT_CANCELED");
                        // Nothing to here normally - the user simply cancelled without selecting a payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Log.d(AlexTAG.debug, "Method payment with GooglePay(): RESULT_ERROR");
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        if (status != null) {
                            handlePaymentError(status.getStatusCode());
                            Log.e(AlexTAG.error, "Method - status.getStatusMessage(): " + status.getStatusMessage());
                        } else {
                            Log.e(AlexTAG.error, "Method - status.getStatusMessage(): status is null");
                        }
                        break;
                }
                break;
        }
    }

    private void binding() {
        binding.startPrice.setText(String.format("%s %s", bigTotal.subtract(deliveryCost), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.finalPrice.setText(String.format("%s %s", bigTotal, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.back.setOnClickListener(v -> finish());
        binding.deliveryPrice.setText(String.format("%s %s", deliveryCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        //set amount of products
        BigInteger productsAmount = new BigInteger("0");
        for (BasketCart basketCart : Common.basketCartRepository.getBasketCartsList()) {
            productsAmount = productsAmount.add(new BigInteger(basketCart.count));
        }
        binding.amountOfProducts.setText(String.format("%s %s %s", getText(R.string.inYourOrderProductsCount), productsAmount, getText(R.string.inYourOrderProductsPC)));

        //set chosen user address
        for (UserAddress address : Common.userAddressesRepository.getUserAddressesList()) {
            if (address.isChecked) {
                binding.userAddress.setText(address.address);
                break;
            }
        }

        binding.phone.addTextChangedListener(new PhoneTextFormatter(binding.phone, "+# (###) ###-##-##"));

//        if (Objects.equals(getIntent().getStringExtra("methodDelivery"), "delivery")) {
//            binding.layoutDelivery.setVisibility(View.VISIBLE);
//            binding.deliveryPrice.setText(String.format("%s %s", getIntent().getStringExtra("deliveryCost"), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
//            binding.layoutAddress.setVisibility(View.VISIBLE);
//            binding.time.setVisibility(View.VISIBLE);
//            binding.time.setText(String.format("%s %s", getIntent().getStringExtra("deliveryTime"), getText(R.string.delivery_time)));
//        }

        binding.paymentCard.setOnClickListener(v -> {
            if (makeOrder("Payment card")) {
                Intent intent = new Intent(OrderActivityNew.this, PaymentActivity.class);
                intent.putExtra("Order", order);
                intent.putExtra("md5", md5);
                intent.putExtra("Price", bigTotal.toString());
                startActivity(intent);
            }
        });
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

    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(paymentsClient).addOnCompleteListener(
                task -> {
                    try {
                        Log.d(AlexTAG.debug, "isReadyToPay");
                        boolean result = task.getResult(ApiException.class);
                        setPwgAvailable(result);
                    } catch (ApiException exception) {
                        // Process error
                        Log.e(AlexTAG.error, exception.toString());
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
            if (makeOrder("Google Pay")) {
                //requestPayment(paymentsClient);
            }
        });
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(PaymentsClient paymentsClient) {
        // Disables the button to prevent multiple clicks.
        //pwg_button.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        String price = bigTotal.toString();

        TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(request);
        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, Objects.requireNonNull(this), LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();
        Log.d("AlexDebug", token.toString());

        // getPaymentMethodToken will only return null if Payment Method Tokenization Parameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();
            // Use token.getToken() to get the token string.
            Log.d("AlexDebug", token.getToken());
            charge(token.getToken(), "Google Pay", bigTotal, order);
        }
    }

    private void handlePaymentError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.d("AlexDebug", String.format("Error code: %d", statusCode));
    }

    // Запрос на прведение одностадийного платежа
    private void charge(String cardCryptogramPacket, String cardHolderName, BigDecimal bigTotal, String order) {
        compositeDisposable.add(PayApi
                .charge(cardCryptogramPacket, cardHolderName, bigTotal, order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe(disposable -> showLoading())
//                .doOnEach(notification -> hideLoading())
                .subscribe(transaction -> {
                    checkResponse(transaction);
                }, this::handleError));
    }

    // Проверяем необходимо ли подтверждение с использованием 3DS
    private void checkResponse(Transaction transaction) {
        if (transaction.getPaReq() != null && transaction.getAcsUrl() != null) {
            // Показываем 3DS форму
            show3DS(transaction);
        } else {
            // Показываем результат
            Log.d("AlexDebug", "transaction result: " + transaction.getCardHolderMessage());
            Log.d("AlexDebug", "transaction.getReasonCode(): " + transaction.getReasonCode());
            showToast(transaction.getCardHolderMessage());
            if (transaction.getReasonCode() == 0) {
                transactionID = transaction.getId();
                Log.d("AlexDebug", "transaction.getId(): " + transaction.getId());
                sendOrderToServer();
                Common.cartRepository.emptyCart();
                Intent intentGP = new Intent();
                intentGP.putExtra("success", "googlePay payment");
                setResult(RESULT_OK, intentGP);
                finish();
            }
        }
    }

    private void show3DS(Transaction transaction) {
        // Открываем 3ds форму
        ThreeDsDialogFragment.newInstance(transaction.getAcsUrl(),
                transaction.getId(),
                transaction.getPaReq())
                .show(this.getSupportFragmentManager(), "3DS");
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
            showToast(message);
        } else if (throwable instanceof UnknownHostException) {
            showToast(getString(R.string.common_no_internet_connection));
        } else {
            showToast(throwable.getMessage());
        }
    }

    public void showToast(String message) {
        Toast.makeText(OrderActivityNew.this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendOrderToServer() {
//        RetrofitClientNew.
//                getClient(RestAPI.URL_API_MAIN)
//                .create(RestAPI.class)
//                .postOrder(order, md5, userID, "new", transactionID)
//                .enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        if (response.isSuccessful()) {
//                            //Log.d("AlexDebug", "response: " + response.body().getStatus());
//                            //Log.d("AlexDebug", "response: " + response.body().getId());
//                        } else {
//                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Log.e("AlexDebug", "sendOrderToServer Failer: " + t.toString());
//                    }
//                });
    }


}