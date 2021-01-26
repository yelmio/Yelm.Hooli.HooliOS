package yelm.io.yelm.order;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.wallet.PaymentsClient;

import java.math.BigInteger;
import java.util.Objects;

import yelm.io.yelm.old_version.maps.MapActivity;
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

    private PaymentsClient paymentsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding();

        paymentsClient = PaymentsUtil.createPaymentsClient(this);
        checkIsReadyToPay();

        bindingChosePaymentType();

//        String phone = binding.phone.getText().toString();
//        Log.d("AlexDebug", "phone: " + phone);
//        phone = phone.replaceAll("\\D", "");
//        Log.d("AlexDebug", "phone after replacement: " + phone);


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


    private void binding() {
        binding.back.setOnClickListener(v -> finish());

        //set amount of products
        BigInteger productsAmount = new BigInteger("0");
        for (BasketCart basketCart : Common.basketCartRepository.getBasketCartsList()) {
            productsAmount = productsAmount.add(new BigInteger(basketCart.count));
        }
        binding.amountOfProducts.setText(String.format("%s %s %s", getText(R.string.inYourOrderProductsCount), productsAmount, getText(R.string.inYourOrderProductsPC)));

        //set segments that separate payment by card and google pay
        binding.segmented.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.card:
                    binding.paymentCard.setVisibility(View.VISIBLE);
                    binding.googlePay.setVisibility(View.GONE);
                    break;
                case R.id.google:
                    binding.paymentCard.setVisibility(View.GONE);
                    binding.googlePay.setVisibility(View.VISIBLE);
                    break;
                default:
            }
        });

        binding.phone.addTextChangedListener(new PhoneTextFormatter(binding.phone, "+7 (###) ###-##-##"));

        if (Objects.equals(getIntent().getStringExtra("methodDelivery"), "delivery")) {
            binding.layoutDelivery.setVisibility(View.VISIBLE);
            binding.deliveryPrice.setText(String.format("%s %s", getIntent().getStringExtra("deliveryCost"), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            binding.layoutAddress.setVisibility(View.VISIBLE);
            binding.time.setVisibility(View.VISIBLE);
            binding.time.setText(String.format("%s %s", getIntent().getStringExtra("deliveryTime"), getText(R.string.delivery_time)));
        }

        binding.finalPrice.setText(String.format("%s %s", getIntent().getStringExtra("finalCost"), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.startPrice.setText(String.format("%s %s", getIntent().getStringExtra("finalCost"), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));

        binding.paymentCard.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivityNew.this, PaymentActivity.class);
            intent.putExtra("Order", "test");
            intent.putExtra("Price", "0");
            startActivity(intent);
        });

        //set chosen user address
        for (UserAddress address : Common.userAddressesRepository.getUserAddressesList()) {
            if (address.isChecked) {
                binding.userAddress.setText(address.address);
                return;
            }
        }


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
            binding.pwgStatus.setVisibility(View.GONE);
            binding.pwgButton.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.pwgStatus.setText(R.string.pwg_status_unavailable);
        }
    }
}