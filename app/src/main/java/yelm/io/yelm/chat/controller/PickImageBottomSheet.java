package yelm.io.yelm.chat.controller;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;

import yelm.io.yelm.R;
import yelm.io.yelm.chat.adapter.PickImageAdapter;
import yelm.io.yelm.databinding.PickImageBottomSheetBinding;
import yelm.io.yelm.chat.model.ModelImages;
import yelm.io.yelm.support_stuff.AlexTAG;

import static android.app.Activity.RESULT_OK;

public class PickImageBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetShopListener listener;
    PickImageBottomSheetBinding binding;

    public ArrayList<ModelImages> allImages;
    boolean isFolder;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_TAKE_PHOTO = 11;

    PickImageAdapter pickImageAdapter;
    HashMap<Integer, String> picturesMap;

    public PickImageBottomSheet() {
        allImages = new ArrayList<>();
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PickImageBottomSheetBinding.inflate(inflater, container, false);
        picturesMap = new HashMap<>();
        binding.done.setText(String.format("%s: (%s)", getText(R.string.pickImageBottomSheetSend), "0"));

        binding.recyclerPickImages.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL));
        binding.done.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSendPictures(picturesMap);
            }
            dismiss();
        });
        getImagesFromStorage(binding.recyclerPickImages);

        return binding.getRoot();
    }


    private void getImagesFromStorage(RecyclerView recyclerPickImages) {
        Log.d(AlexTAG.debug, "getImages()");

        int positionIndex = 0;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor;
        int columnIndexData, columnIndexFolderName;

        String absolutePathOfImage = null;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        // String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME};

        //final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getContext().getApplicationContext().getContentResolver()
                .query(uri, projection, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
//            cursor = getApplicationContext().getContentResolver()
//                    .query(uri, projection, null, null, orderBy + " DESC");

        columnIndexData = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA) : 0;
        columnIndexFolderName = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME) : 0;

        while (cursor.moveToNext()) {

            absolutePathOfImage = cursor.getString(columnIndexData);
            //Log.d(AlexTAG.debug, "Column: " + absolutePathOfImage);
            //Log.d(AlexTAG.debug, "Folder: " + cursor.getString(column_index_folder_name));

            for (int i = 0; i < allImages.size(); i++) {
                if (allImages.get(i).getFolder().equals(cursor.getString(columnIndexFolderName))) {
                    isFolder = true;
                    positionIndex = i;
                    break;
                } else {
                    isFolder = false;
                }
            }

            ArrayList<String> allPath = new ArrayList<>();
            if (isFolder) {
                allPath.addAll(allImages.get(positionIndex).getAllImagesPath());
                allPath.add(absolutePathOfImage);
                allImages.get(positionIndex).setAllImagesPath(allPath);
            } else {
                allPath.add(absolutePathOfImage);
                ModelImages modelImage = new ModelImages();
                modelImage.setFolder(cursor.getString(columnIndexFolderName));
                modelImage.setAllImagesPath(allPath);
                allImages.add(modelImage);
            }
        }

        ArrayList<String> imagesList = new ArrayList<>();

        for (int i = 0; i < allImages.size(); i++) {
            Log.d(AlexTAG.debug, allImages.get(i).getFolder());
            for (int j = 0; j < allImages.get(i).getAllImagesPath().size(); j++) {
                //Log.d(AlexTAG.debug, allImages.get(i).getAllImagesPath().get(j));
                imagesList.add(allImages.get(i).getAllImagesPath().get(j));
            }
        }
        cursor.close();
        //obj_adapter = new Adapter_PhotosFolder(getApplicationContext(),al_images);
        //gv_folder.setAdapter(obj_adapter);
        //return al_images;
        Log.d(AlexTAG.debug, "imagesList.size: " + imagesList.size());

        pickImageAdapter = new PickImageAdapter(getContext(), imagesList);
        pickImageAdapter.setListener((position, path, check) -> {
            if (check) {
                picturesMap.put(position, path);
            } else {
                picturesMap.remove(position);
            }
            binding.done.setText(String.format("%s: (%s)", getText(R.string.pickImageBottomSheetSend), picturesMap.size()));
        });

        pickImageAdapter.setCameraListener(() -> {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
        });


        recyclerPickImages.setAdapter(pickImageAdapter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Фотка сделана, извлекаем миниатюру картинки
            Log.d(AlexTAG.debug, "made photo");

            Bundle extras = data.getExtras();
            Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
            Log.d(AlexTAG.debug, "Bitmap: " + thumbnailBitmap.getByteCount());

        }
    }


    public interface BottomSheetShopListener {
        void onSendPictures(HashMap<Integer, String> picturesMap);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PickImageBottomSheet.BottomSheetShopListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetShopListener");
        }
    }

}
