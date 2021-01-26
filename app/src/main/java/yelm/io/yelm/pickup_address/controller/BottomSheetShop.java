package yelm.io.yelm.pickup_address.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ShopDescriptionSheetBinding;
import yelm.io.yelm.old_version.maps.ShopClass;

public class BottomSheetShop extends BottomSheetDialogFragment {

    private BottomSheetShopListener listener;
    ShopClass shopClass;
    ShopDescriptionSheetBinding binding;

    public BottomSheetShop(ShopClass shopClass) {
        this.shopClass = shopClass;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ShopDescriptionSheetBinding.inflate(inflater, container, false);

        binding.getShopAddress.setOnClickListener(view -> {
            listener.onClicked(shopClass.getAddress(), shopClass.getID());
            dismiss();
        });

        binding.name.setText(shopClass.getName());

        binding.description.setText(shopClass.getText());

        String workingTime = "Режим работы: " + shopClass.getTimeWorkStart() + " - " + shopClass.getTimeWorkEnd();
        binding.time.setText(workingTime);

        binding.userAddress.setText(shopClass.getAddress());

        binding.userPhone.setText(shopClass.getPhone().toString());
        binding.userPhone.setOnClickListener(view -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + shopClass.getPhone()));
            startActivity(callIntent);
        });

        Picasso.get()
                .load(shopClass.getImage())
                .noPlaceholder()
                .centerCrop()
                .resize(800, 0)
                .into(binding.shopImage);

        binding.link.setText(shopClass.getWebsite());
        binding.link.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(shopClass.getWebsite()))));

        return binding.getRoot();
    }

    public interface BottomSheetShopListener {
        void onClicked(String address, String id);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (BottomSheetShopListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetShopListener");
        }
    }

}
