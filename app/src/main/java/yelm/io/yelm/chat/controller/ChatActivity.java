package yelm.io.yelm.chat.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yelm.io.yelm.R;
import yelm.io.yelm.chat.adapter.ChatAdapter;
import yelm.io.yelm.chat.model.ChatContent;
import yelm.io.yelm.databinding.ActivityChatBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.support_stuff.AlexTAG;


public class ChatActivity extends AppCompatActivity implements PickImageBottomSheet.BottomSheetShopListener {

    ActivityChatBinding binding;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    ChatAdapter chatAdapter;

    ArrayList<ChatContent> chatContentList = new ArrayList<>();
    Bitmap bitmap;
    PickImageBottomSheet pickImageBottomSheet = new PickImageBottomSheet();

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String[] READ_WRITE_EXTERNAL_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};


    private static final int REQUEST_PERMISSIONS_READ_WRITE_STORAGE = 100;
    private static final int REQUEST_PERMISSIONS_CAMERA = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tuneChatRecycler();
        binding();
    }


    private boolean hasReadExternalStoragePermission() {
        int result = ContextCompat
                .checkSelfPermission(this.getApplicationContext(), READ_WRITE_EXTERNAL_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void tuneChatRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRecycler.setLayoutManager(linearLayoutManager);
        chatContentList.add(new ChatContent("shop", "hi what would you like?", "", null));

        chatAdapter = new ChatAdapter(this, chatContentList);
        binding.chatRecycler.setAdapter(chatAdapter);
        chatAdapter.setListener(new ChatAdapter.Listener() {
            @Override
            public void onComplete() {
                Log.d(AlexTAG.debug, "onComplete");
                new Handler().postDelayed(() -> binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1), 100);
            }
        });
    }

    private void binding() {

        binding.sendMessage.setOnClickListener(v -> {
            if (!binding.messageField.getText().toString().trim().isEmpty()) {
                ChatContent temp = new ChatContent(userID, binding.messageField.getText().toString().trim(), "", null);
                chatContentList.add(temp);
                chatAdapter.notifyDataSetChanged();
                binding.messageField.setText("");
            }
        });
        binding.back.setOnClickListener(v -> finish());

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            binding.rootLayout.getWindowVisibleDisplayFrame(r);
            int heightDiff = binding.rootLayout.getRootView().getHeight() - r.height();
            if (heightDiff > 0.25 * binding.rootLayout.getRootView().getHeight()) {
                Log.d(AlexTAG.debug, "keyboard opened");
                binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
            }
        });

        binding.choosePicture.setOnClickListener(v -> callPickImageBottomSheet());

    }

    private void callPickImageBottomSheet() {

        if (hasCameraPermission()) {
        } else {
            ActivityCompat.requestPermissions(
                    this, CAMERA_PERMISSIONS, REQUEST_PERMISSIONS_CAMERA);
        }

        if (hasReadExternalStoragePermission()) {
            //check if AddressesBottomSheet is added otherwise we get exception:
            //java.lang.IllegalStateException: Fragment already added
            if (!pickImageBottomSheet.isAdded()) {
                pickImageBottomSheet = new PickImageBottomSheet();
                pickImageBottomSheet.show(getSupportFragmentManager(), "pickImageBottomSheet");
            }
        } else {
            ActivityCompat.requestPermissions(this, READ_WRITE_EXTERNAL_PERMISSIONS, REQUEST_PERMISSIONS_READ_WRITE_STORAGE);
        }

    }

    private boolean hasCameraPermission() {
        int result = ContextCompat
                .checkSelfPermission(this.getApplicationContext(), CAMERA_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_READ_WRITE_STORAGE:
                if (hasReadExternalStoragePermission()) {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Location Permissions Result: Success!");
                    callPickImageBottomSheet();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestLocationPermission(getText(R.string.chatActivityRequestStoragePermission).toString());
                } else {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Location Permissions Result: Failed!");
                }
                break;
            case REQUEST_PERMISSIONS_CAMERA:
                if (hasCameraPermission()) {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Camera Permissions Result: Success!");
                    callPickImageBottomSheet();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestCameraPermission(getText(R.string.chatActivityRequestCameraPermission).toString());
                } else {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Camera Permissions Result: Failed!");
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    private void showDialogExplanationAboutRequestLocationPermission(String message) {
        new AlertDialog.Builder(ChatActivity.this)
                .setMessage(message)
                .setTitle(getText(R.string.mainActivityAttention))
                .setOnCancelListener(dialogInterface -> ActivityCompat.requestPermissions(ChatActivity.this, READ_WRITE_EXTERNAL_PERMISSIONS, REQUEST_PERMISSIONS_READ_WRITE_STORAGE))
                .setPositiveButton(getText(R.string.mainActivityOk), (dialogInterface, i) -> ActivityCompat.requestPermissions(ChatActivity.this, READ_WRITE_EXTERNAL_PERMISSIONS, REQUEST_PERMISSIONS_READ_WRITE_STORAGE))
                .create()
                .show();
    }

    private void showDialogExplanationAboutRequestCameraPermission(String message) {
        new AlertDialog.Builder(ChatActivity.this)
                .setMessage(message)
                .setTitle(getText(R.string.mainActivityAttention))
                .setOnCancelListener(dialogInterface -> ActivityCompat.requestPermissions(ChatActivity.this, CAMERA_PERMISSIONS, REQUEST_PERMISSIONS_CAMERA))
                .setPositiveButton(getText(R.string.mainActivityOk), (dialogInterface, i) -> ActivityCompat.requestPermissions(ChatActivity.this, CAMERA_PERMISSIONS, REQUEST_PERMISSIONS_CAMERA))
                .create()
                .show();
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Uri imageUri = data.getData();
//            Log.d(AlexTAG.debug, "imageUri " + imageUri);
//            Log.d(AlexTAG.debug, "getPath " + imageUri.getPath());
//            chatAdapter.notifyDataSetChanged();
//            binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                Log.d(AlexTAG.debug, "bitmap.getWidth() " + bitmap.getWidth());
//                Log.d(AlexTAG.debug, "bitmap.getHeight() " + bitmap.getHeight());
//                ConvertingImageToBase64(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void ConvertingImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        //Log.d(AlexTAG.debug, "imageBytes " + Arrays.toString(imageBytes));

        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //Log.d(AlexTAG.debug, "imageString " + imageString);

        //byte[] byteArray = Base64.decode(imageString, Base64.DEFAULT);
        //Log.d(AlexTAG.debug, "Arrays.toString(byteArray) " + Arrays.toString(byteArray));

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onSendPictures(HashMap<Integer, String> picturesMap) {
        for (Map.Entry<Integer, String> picture : picturesMap.entrySet()) {
            Log.d("AlexDebug", "picture.getKey(): " + picture.getKey());
            Log.d("AlexDebug", "picture.getValue(): " + picture.getValue());
            chatContentList.add(new ChatContent(userID, "", "", picture.getValue()));
        }
        chatAdapter.notifyDataSetChanged();
        binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
    }
}


