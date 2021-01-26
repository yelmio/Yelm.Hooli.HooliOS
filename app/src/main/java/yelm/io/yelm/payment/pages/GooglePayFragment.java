package yelm.io.yelm.payment.pages;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.wallet.PaymentsClient;

import yelm.io.yelm.R;
import yelm.io.yelm.order.OrderActivity;
import yelm.io.yelm.payment.googleplay.PaymentsUtil;


public class GooglePayFragment extends Fragment {

    private PaymentsClient paymentsClient;

    //display google pay view
    View pwg_button;
    TextView textViewPwgStatus;

    public GooglePayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentsClient = PaymentsUtil.createPaymentsClient(getActivity());
        checkIsReadyToPay();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_pay, container, false);
        textViewPwgStatus = v.findViewById(R.id.pwg_status);
        pwg_button = v.findViewById(R.id.pwg_button);
        pwg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AlexDebug", "onClick");
                ((OrderActivity) getActivity()).googlePayOrder(paymentsClient);
            }
        });
        paymentsClient = PaymentsUtil.createPaymentsClient(getActivity());
        checkIsReadyToPay();
        return v;
    }

    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(paymentsClient).addOnCompleteListener(
                task -> {
                    try {
                        Log.d("AlexDebug", "isReadyToPay");
                        boolean result = task.getResult(ApiException.class);
                        setPwgAvailable(result);
                    } catch (ApiException exception) {
                        Log.d("AlexDebug", exception.toString());
                        Log.w("AlexDebug", exception);
                    }
                });
    }

    private void setPwgAvailable(boolean available) {
        if (available) {
            textViewPwgStatus.setVisibility(View.GONE);
            pwg_button.setVisibility(View.VISIBLE);
        } else {
            textViewPwgStatus.setText(R.string.pwg_status_unavailable);
        }
    }


}