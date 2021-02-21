package yelm.io.yelm.user_address.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.UserAddressItemBinding;
import yelm.io.yelm.support_stuff.Logging;

public class UserAddressesAdapter extends RecyclerView.Adapter<UserAddressesAdapter.ViewHolder> {

    private Context context;
    private List<UserAddress> userAddresses;
    private AddressChangeListener addressChangeListener;

    public interface AddressChangeListener {
        void onAddressChange();
    }

    public void setListener(AddressChangeListener addressChangeListener) {
        this.addressChangeListener = addressChangeListener;
    }

    public UserAddressesAdapter(Context context, List<UserAddress> userAddresses) {
        this.context = context;
        this.userAddresses = userAddresses;
    }

    public void setNewUserAddressList(List<UserAddress> userAddresses){
        this.userAddresses = userAddresses;
        notifyDataSetChanged();
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
        }else {
            holder.binding.checkedIcon.setImageResource(R.drawable.ic_pin_gray_24);
        }
        holder.binding.removeAddress.setOnClickListener(v -> {
            if (Common.userAddressesRepository.getUserAddressesList().size() == 1) {
                return;
            }
            Common.userAddressesRepository.deleteUserAddressById(userAddress.id);
            if (userAddress.isChecked) {
                UserAddress temp = Common.userAddressesRepository.getUserAddressesList().get(0);
                temp.isChecked = true;
                Common.userAddressesRepository.updateUserAddresses(temp);
            }
            if (addressChangeListener != null) {
                addressChangeListener.onAddressChange();
                Log.d(Logging.debug, "removeAddress()");
            }
        });

        holder.binding.checkedIcon.setOnClickListener(v -> {
            if (Common.userAddressesRepository.getUserAddressesList().get(position).isChecked) {
                return;
            }

            for (UserAddress current : Common.userAddressesRepository.getUserAddressesList()) {
                if (current.isChecked) {
                    current.isChecked = false;
                    Common.userAddressesRepository.updateUserAddresses(current);
                    break;
                }
            }

            UserAddress allocated = Common.userAddressesRepository.getUserAddressById(userAddress.id);
            allocated.isChecked = true;
            Common.userAddressesRepository.updateUserAddresses(allocated);
            if (addressChangeListener != null) {
                addressChangeListener.onAddressChange();
                Log.d(Logging.debug, "changeAddress");
            }
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
