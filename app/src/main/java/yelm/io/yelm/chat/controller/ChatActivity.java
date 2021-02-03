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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.chat.adapter.ChatAdapter;
import yelm.io.yelm.chat.model.ChatContent;
import yelm.io.yelm.chat.model.ChatHistoryClass;
import yelm.io.yelm.databinding.ActivityChatBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RestApiChat;
import yelm.io.yelm.retrofit.new_api.RetrofitClientChat;
import yelm.io.yelm.support_stuff.AlexTAG;


public class ChatActivity extends AppCompatActivity implements PickImageBottomSheet.BottomSheetShopListener, PickImageBottomSheet.CameraListener {

    ActivityChatBinding binding;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    ChatAdapter chatAdapter;

    ArrayList<ChatContent> chatContentList = new ArrayList<>();
    Bitmap bitmap;
    PickImageBottomSheet pickImageBottomSheet = new PickImageBottomSheet();

    private static final int REQUEST_TAKE_PHOTO = 11;

    private static final String[] READ_WRITE_EXTERNAL_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};


    private static final int REQUEST_PERMISSIONS_READ_WRITE_STORAGE = 100;
    private static final int REQUEST_PERMISSIONS_CAMERA = 10;

    SimpleDateFormat currentFormatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat printedFormatterDate = new SimpleDateFormat("HH:mm");

    public static final String CHAT_SERVER_URL = "https://chat.yelm.io/";
    private Socket socket;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tuneChatRecycler();
        binding();

        tuneSocketConnection();


        getChatHistory();
    }

    private void tuneSocketConnection() {
        try {
            IO.Options options = new IO.Options();
            String token = "token=T6MBWl29sxDEzxVBPCNNzLyzLgk0gb9G&room_id=8&user=Client";
            //options.query = jsonObjectItem.toString();
            options.query = token;
            socket = IO.socket(CHAT_SERVER_URL, options);
            //6 - dynamic number
            socket.on("room.8", onLogin);
            socket.connect();
            Log.d("AlexDebug", "connected!");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("AlexDebug", "error: " + e.getMessage());
        }
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatActivity.this.runOnUiThread(() -> {
                Log.d("AlexDebug", "received");
                JSONObject data = (JSONObject) args[0];
                //String username;
                String message;
                try {
                    //username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    return;
                }
                //Log.d("AlexDebug", "username " + username);
                Log.d("AlexDebug", "message " + message);


                // add the message to view
                //addMessage(username, message);
            });
        }
    };



    private String ConvertingImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        bitmap.recycle();

        Log.d("AlexDebug", "imageBytes " + Arrays.toString(imageBytes));
        Log.d("AlexDebug", "length " + imageBytes.length);


        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //StringBuilder builder = new StringBuilder("data:image/png;base64,").append(imageString);
        Log.d("AlexDebug", "imageString " + imageString);
        Log.d("AlexDebug", "imageString.length " + imageString.length());
        //Log.d("AlexDebug", "builder " + builder);
        //byte[] byteArray = Base64.decode(imageString, Base64.DEFAULT);
        //Log.d(AlexTAG.debug, "Arrays.toString(byteArray) " + Arrays.toString(byteArray));


        //byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        //binding.imageView.setImageBitmap(decodedByte);

        return imageString;
        //return "data:image/png;base64,"+imageString;

    }


    private void getChatHistory() {
        RetrofitClientChat.
                getClient(RestApiChat.URL_API_MAIN).
                create(RestApiChat.class).
                getChatHistory(RestApiChat.PLATFORM_NUMBER,
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, "")).
                enqueue(new Callback<ArrayList<ChatHistoryClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<ChatHistoryClass>> call, @NotNull final Response<ArrayList<ChatHistoryClass>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.d(AlexTAG.debug, "ChatSettingsClass: " + response.body().toString());
                                for (ChatHistoryClass chat : response.body()) {
                                    String value = chat.getCreatedAt();
                                    Calendar current = GregorianCalendar.getInstance();
                                    try {
                                        current.setTime(currentFormatterDate.parse(value));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    chatContentList.add(new ChatContent(chat.getFromWhom(), chat.getToWhom(), chat.getMessage(), printedFormatterDate.format(current.getTime()), chat.getImages(), false));
                                }
                                chatAdapter = new ChatAdapter(ChatActivity.this, chatContentList);
                                binding.chatRecycler.setAdapter(chatAdapter);
                                chatAdapter.setListener(new ChatAdapter.Listener() {
                                    @Override
                                    public void onComplete() {
                                        Log.d(AlexTAG.debug, "onComplete");
                                        new Handler().postDelayed(() -> binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1), 100);
                                    }
                                });
                                //connect

                            } else {
                                Log.e(AlexTAG.error, "Method getChatHistory(): by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method getChatHistory() response is not successful." +
                                    " Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<ChatHistoryClass>> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method getChatHistory() failure: " + t.toString());
                    }
                });
    }

    private boolean hasReadExternalStoragePermission() {
        int result = ContextCompat
                .checkSelfPermission(this.getApplicationContext(), READ_WRITE_EXTERNAL_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void tuneChatRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRecycler.setLayoutManager(linearLayoutManager);
//        chatAdapter = new ChatAdapter(this, chatContentList);
//        binding.chatRecycler.setAdapter(chatAdapter);

    }

    private void binding() {
        binding.sendMessage.setOnClickListener(v -> {
            String message = binding.messageField.getText().toString().trim();
            if (!message.isEmpty()) {
                Calendar current = GregorianCalendar.getInstance();
                ChatContent temp = new ChatContent(
                        LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""),
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, ""),
                        message,
                        printedFormatterDate.format(current.getTime()),
                        null,
                        true);
                chatContentList.add(temp);
                chatAdapter.notifyDataSetChanged();
                binding.messageField.setText("");
                socketSendMessage(message);
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
            } else {
                Log.d(AlexTAG.debug, "keyboard closed");
            }
        });
        binding.choosePicture.setOnClickListener(v -> requestPermissions());
    }

    private void requestPermissions() {
        if (hasReadExternalStoragePermission()) {
            if (hasCameraPermission()) {
                callPickImageBottomSheet();
            } else {
                ActivityCompat.requestPermissions(
                        this, CAMERA_PERMISSIONS, REQUEST_PERMISSIONS_CAMERA);
            }
        } else {
            ActivityCompat.requestPermissions(this, READ_WRITE_EXTERNAL_PERMISSIONS, REQUEST_PERMISSIONS_READ_WRITE_STORAGE);
        }
    }

    private void callPickImageBottomSheet() {
        //check if AddressesBottomSheet is added otherwise we get exception:
        //java.lang.IllegalStateException: Fragment already added
        if (!pickImageBottomSheet.isAdded()) {
            pickImageBottomSheet = new PickImageBottomSheet();
            pickImageBottomSheet.show(getSupportFragmentManager(), "pickImageBottomSheet");
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
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request STORAGE Permissions Result: Success!");
                    requestPermissions();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestReadWriteStoragePermission(getText(R.string.chatActivityRequestStoragePermission).toString());
                } else {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request STORAGE Permissions Result: Failed!");
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

    private void showDialogExplanationAboutRequestReadWriteStoragePermission(String message) {
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

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (data != null) {

//                try {
//                    Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
//                            getContentResolver(), imageUri);
//                    Log.d(AlexTAG.debug, "Bitmap: " + bitmap.getByteCount());
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.d(AlexTAG.debug, "Bitmap error: " + e.getMessage());
//
//                }

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Log.d(AlexTAG.debug, "Bitmap: " + bitmap.getByteCount());

                File dir = new File(Environment.getExternalStorageDirectory() + "/" + getText(R.string.app_name));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fileName = String.format("IMG_%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(AlexTAG.debug, " " + e.toString());
                }
                Log.d(AlexTAG.debug, "outFile.getAbsolutePath()" + outFile.getAbsolutePath());

                Calendar current = GregorianCalendar.getInstance();
                ChatContent temp = new ChatContent(
                        LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""),
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, ""),
                        "",
                        printedFormatterDate.format(current.getTime()),
                        new ArrayList<String>(Arrays.asList(outFile.getAbsolutePath())),
                        true);
                chatContentList.add(temp);
                chatAdapter.notifyDataSetChanged();
                binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);

                MediaScannerConnection.scanFile(this,
                        new String[]{outFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.d(AlexTAG.debug, "Scanned " + path + ":");
                                Log.d(AlexTAG.debug, "-> uri=" + uri);
                            }
                        });

            }
            pickImageBottomSheet.dismiss();
        }
    }

    @Override
    public void onSendPictures(HashMap<Integer, String> picturesMap) {
        ArrayList<String> images = new ArrayList<>();
        for (Map.Entry<Integer, String> picture : picturesMap.entrySet()) {
            Log.d("AlexDebug", "picture.getKey(): " + picture.getKey());
            Log.d("AlexDebug", "picture.getValue(): " + picture.getValue());
            images.add(picture.getValue());
        }

        Calendar current = GregorianCalendar.getInstance();
        chatContentList.add(new ChatContent(
                LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""),
                LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, ""),
                binding.messageField.getText().toString().trim(),
                printedFormatterDate.format(current.getTime()),
                images,
                true));

        chatAdapter.notifyDataSetChanged();
        binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
        socketSendPictures(images);
    }

    private void socketSendPictures(ArrayList<String> images) {
        JSONObject jsonObjectItem = new JSONObject();
        JSONArray picturesArray = new JSONArray();
        for (String imageUri : images) {
            Uri uri = Uri.fromFile(new File(imageUri));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                String base64 = ConvertingImageToBase64(bitmap);
                JSONObject pictureObject = new JSONObject();
                pictureObject.put("image", base64);
                picturesArray.put(pictureObject);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            jsonObjectItem.put("room_id", LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, ""));
            jsonObjectItem.put("from_whom", LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""));
            jsonObjectItem.put("to_whom", LoaderActivity.settings.getString(LoaderActivity.SHOP_ID, ""));
            jsonObjectItem.put("message", "");
            jsonObjectItem.put("type", "images");//"type"-"images/message"
            jsonObjectItem.put("platform", RestAPI.PLATFORM_NUMBER);
            jsonObjectItem.put("images", picturesArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("room.8", jsonObjectItem);
    }

    private void socketSendMessage(String message) {
        JSONObject jsonObjectItem = new JSONObject();
        try {
            jsonObjectItem.put("room_id", LoaderActivity.settings.getString(LoaderActivity.ROOM_ID, ""));
            jsonObjectItem.put("from_whom", LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""));
            jsonObjectItem.put("to_whom", LoaderActivity.settings.getString(LoaderActivity.SHOP_ID, ""));
            jsonObjectItem.put("message", message);
            jsonObjectItem.put("type", "message");//"type"-"images/message"
            jsonObjectItem.put("platform", RestAPI.PLATFORM_NUMBER);
            jsonObjectItem.put("images", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("room.8", jsonObjectItem);
    }

    @Override
    public void onCameraClick() {

//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Camera");
//        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File file = new File(Environment.getExternalStorageDirectory(), "/" + getText(R.string.app_name) + "/photo_" + timeStamp + ".png");
//        imageUri = Uri.fromFile(file);


        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onDestroy() {
        socket.off("room.8", onLogin);
        socket.disconnect();
        super.onDestroy();
    }
}