package yelm.io.yelm.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import yelm.io.yelm.databinding.PickStorageImageBinding;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.support_stuff.ScreenDimensions;

public class PickImageAdapter extends RecyclerView.Adapter<PickImageAdapter.ViewHolder> {

    private ArrayList<String> listImages;
    private Context context;
    ScreenDimensions screenDimensions;
    private Listener listener;
    boolean[] checked;

    public interface Listener {
        void selectedPicture(Integer position, String path, boolean check);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PickImageAdapter(Context context, ArrayList<String> listImages) {
        this.listImages = listImages;
        this.context = context;
        this.screenDimensions = new ScreenDimensions((Activity) context);
        checked = new boolean[listImages.size()];
    }

    @NonNull
    @Override
    public PickImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PickImageAdapter.ViewHolder(PickStorageImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String path = listImages.get(position);

        holder.binding.selector.setChecked(checked[position]);

        //holder.binding.image.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 3) * screenDimensions.getScreenDensity() + 0.5f);
        Picasso.get().load(Uri.fromFile(new File(path)))
                .resize(200, 0)
                .into(holder.binding.image);


        holder.binding.selector.setOnClickListener(view -> {
            checked[position] = !checked[position];
            if (listener != null) {
                listener.selectedPicture(position, path, checked[position]);
            }
        });

//        holder.binding.selector.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (holder.binding.selector.isChecked()) {
//                Log.d(AlexTAG.debug, "isChecked: " + path);
//                checked[position] = true;
//                Log.d(AlexTAG.debug, "checked[position]: " + position + " " + checked[position]);
//
//                //listener.onChecked(current, true);
//            } else {
//                Log.d(AlexTAG.debug, "isNotChecked: " + path);
//                //listener.onChecked(current, false);
//                checked[position] = false;
//                Log.d(AlexTAG.debug, "checked[position]: " + position + " " + checked[position]);
//
//            }
//        });

    }


    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PickStorageImageBinding binding;

        public ViewHolder(PickStorageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}