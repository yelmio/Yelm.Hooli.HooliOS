package yelm.io.yelm.payment.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import yelm.io.yelm.R;
import yelm.io.yelm.order.OrderActivity;


public class CashPayFragment extends Fragment {

    Button payButton;

    public CashPayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cash_pay, container, false);
        payButton = v.findViewById(R.id.payButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((OrderActivity) getActivity()).cashPayOrder();
            }
        });
        return v;
    }


}