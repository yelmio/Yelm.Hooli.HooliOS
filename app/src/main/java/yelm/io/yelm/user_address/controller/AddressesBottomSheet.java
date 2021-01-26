package yelm.io.yelm.user_address.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.AdressesBottomSheepDialogBinding;
import yelm.io.yelm.user_address.adapter.UserAddressesAdapter;

public class AddressesBottomSheet extends BottomSheetDialogFragment {

    private AddressesBottomSheetListener listener;
    private AdressesBottomSheepDialogBinding binding;
    private UserAddressesAdapter userAddressesAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void setListener(AddressesBottomSheetListener listener) {
        this.listener = listener;
    }

    public interface AddressesBottomSheetListener {
        void selectedAddress(UserAddress address);
    }

    public AddressesBottomSheet() {
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdressesBottomSheepDialogBinding.inflate(inflater, container, false);
        binding.addressesDone.setOnClickListener(v -> this.dismiss());
        binding.addNewAddress.setOnClickListener(v -> startActivity(new Intent(getContext(), AddressChooseActivity.class)));
        binding.recyclerUserAddresses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddressesBottomSheet.AddressesBottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetShopListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        compositeDisposable.
                add(Common.userAddressesRepository.
                        getUserAddresses().
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(userAddresses -> {
                            binding.youDontHaveAddressesYet.setVisibility(userAddresses.size() == 0 ? View.VISIBLE : View.GONE);
                            userAddressesAdapter = new UserAddressesAdapter(getContext(), userAddresses);
                            binding.recyclerUserAddresses.setAdapter(userAddressesAdapter);
                            for (UserAddress current : userAddresses) {
                                if (current.isChecked) {
                                    listener.selectedAddress(current);
                                    break;
                                }
                            }
                        }));
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
