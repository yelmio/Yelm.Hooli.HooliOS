package yelm.io.yelm.user_address.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.UserAddressItemBinding;

public class UserAddressesAdapter extends RecyclerView.Adapter<UserAddressesAdapter.ViewHolder> {

    private Context context;
    private List<UserAddress> userAddresses;

    public UserAddressesAdapter(Context context, List<UserAddress> userAddresses) {
        this.context = context;
        this.userAddresses = userAddresses;
    }

    @NonNull
    @Override
    public UserAddressesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(UserAddressItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserAddressesAdapter.ViewHolder holder, int position) {
        UserAddress userAddress = userAddresses.get(position);
        holder.binding.userAddress.setText(userAddress.address);
        if (userAddress.isChecked) {
            holder.binding.checkedIcon.setImageResource(R.drawable.ic_pin_blue_24);
        }
        holder.binding.removeAddress.setOnClickListener(v -> {
            if (Common.userAddressesRepository.getUserAddressesList().size() == 1) {
                return;
            }
            Common.userAddressesRepository.deleteUserAddressById(userAddress.id);
            if (userAddress.isChecked) {
                if (Common.userAddressesRepository.getUserAddressesList().size() != 0) {
                    UserAddress temp = Common.userAddressesRepository.getUserAddressesList().get(0);
                    temp.isChecked = true;
                    Common.userAddressesRepository.updateUserAddresses(temp);
                }
            }
        });
        holder.binding.addressLayout.setOnClickListener(v -> {
            for (int i = 0; i < Common.userAddressesRepository.getUserAddressesList().size(); i++) {
                UserAddress temp = Common.userAddressesRepository.getUserAddressesList().get(i);
                temp.isChecked = false;
                Common.userAddressesRepository.updateUserAddresses(temp);
            }
            UserAddress allocated = Common.userAddressesRepository.getUserAddressById(userAddress.id);
            allocated.isChecked = true;
            Common.userAddressesRepository.updateUserAddresses(allocated);
        });
    }

    @Override
    public int getItemCount() {
        return userAddresses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private UserAddressItemBinding binding;

        public ViewHolder(@NonNull UserAddressItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
