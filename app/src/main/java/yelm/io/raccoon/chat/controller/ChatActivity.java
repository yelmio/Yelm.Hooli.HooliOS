package yelm.io.raccoon.chat.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.raccoon.R;
import yelm.io.raccoon.chat.adapter.ChatAdapter;
import yelm.io.raccoon.chat.model.ChatContent;
import yelm.io.raccoon.chat.model.ChatHistoryClass;
import yelm.io.raccoon.constants.Constants;
import yelm.io.raccoon.databinding.ActivityChatBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Item;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.rest_api.RestApiChat;
import yelm.io.raccoon.rest.client.RetrofitClientChat;
import yelm.io.raccoon.support_stuff.Logging;


public class ChatActivity extends AppCompatActivity implements PickImageBottomSheet.BottomSheetShopListener, PickImageBottomSheet.CameraListener {

    ActivityChatBinding binding;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    ChatAdapter chatAdapter;

    ArrayList<ChatContent> chatContentList = new ArrayList<>();
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
        Constants.customerInChat = true;

        try {
            IO.Options options = new IO.Options();
            options.query = "token=" + LoaderActivity.settings.getString(LoaderActivity.API_TOKEN, "")
                    + "&room_id=" + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, "")
                    + "&user=Client";
            socket = IO.socket(CHAT_SERVER_URL, options);
            socket.on("room." + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""), onLogin);
            socket.connect();

            Log.d("AlexDebug", "room: " + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""));
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
                Log.d("AlexDebug", "data: " + data.toString());

                if (data.has("role")) {
                    try {
                        String role = data.getString("role");
                        Log.d("AlexDebug", "role: " + role);
                        String type = data.getString("type");
                        Log.d("AlexDebug", "type: " + type);
                        if (data.getString("type").equals("connected")) {
                            binding.chatStatus.setText(getText(R.string.chatActivityOnline));
                            binding.chatStatus.setTextColor(getResources().getColor(R.color.colorAcceptOrder));
                        } else {
                            binding.chatStatus.setText(getText(R.string.chatActivityOffline));
                            binding.chatStatus.setTextColor(getResources().getColor(R.color.colorRedDiscount));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("AlexDebug", "JSONException: " + e.getMessage());
                    }
                    return;
                }

                try {
                    Log.d("AlexDebug", "data.toString(): " + data.toString());
                    if (data.getString("from_whom").equals(LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""))) {
                        return;
                    }
                    if (data.getString("type").equals("message")) {
                        String message = data.getString("message");
                        Calendar current = GregorianCalendar.getInstance();
                        ChatContent chatMessage = new ChatContent(
                                LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""),
                                LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                                message,
                                printedFormatterDate.format(current.getTime()),
                                "message",
                                "",
                                null,
                                false,
                                "0");
                        chatContentList.add(chatMessage);
                        chatAdapter.notifyDataSetChanged();
                        Log.d("AlexDebug", "message " + message);
                    } else if (data.getString("type").equals("images")) {
                        String arrayImages = data.getString("images");
                        Gson gson = new Gson();
                        Type typeString = new TypeToken<ArrayList<String>>() {
                        }.getType();
                        ArrayList<String> arrayImagesList = gson.fromJson(arrayImages, typeString);
                        Log.d("AlexDebug", "arrayImagesList " + arrayImagesList.toString());
                        for (String image : arrayImagesList) {
                            Calendar current = GregorianCalendar.getInstance();
                            ChatContent temp = new ChatContent(
                                    LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""),
                                    LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                                    "",
                                    printedFormatterDate.format(current.getTime()),
                                    "images",
                                    image,
                                    null,
                                    false,
                                    "0");
                            chatContentList.add(temp);
                            chatAdapter.notifyDataSetChanged();
                        }
                    } else if (data.getString("type").equals("items")) {
                        String itemString = data.getString("items");
                        Gson gson = new Gson();
                        Type typeItem = new TypeToken<Item>() {
                        }.getType();
                        Item item = gson.fromJson(itemString, typeItem);
                        Calendar current = GregorianCalendar.getInstance();
                        ChatContent temp = new ChatContent(
                                LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""),
                                LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                                "",
                                printedFormatterDate.format(current.getTime()),
                                "items",
                                null,
                                item,
                                false,
                                "0");
                        chatContentList.add(temp);
                        chatAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AlexDebug", "JSONException: " + e.getMessage());
                }
            });
        }
    };

    private String ConvertingImageToBase64(Bitmap bitmap) {
        Log.d("AlexDebug", "ConvertingImageToBase64()");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] imageBytes = baos.toByteArray();
        bitmap.recycle();
        Log.d("AlexDebug", "imageBytes - length " + imageBytes.length);
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d("AlexDebug", "Base64.encodeToString - imageString.length " + imageString.length());

        //byte[] byteArray = Base64.decode(imageString, Base64.DEFAULT);
        //Log.d(Logging.debug, "Arrays.toString(byteArray) " + Arrays.toString(byteArray));
        //byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        // binding.imageView.setImageBitmap(decodedByte);

        return imageString;
    }

    private void getChatHistory() {
        RetrofitClientChat.
                getClient(RestApiChat.URL_API_MAIN).
                create(RestApiChat.class).
                getChatHistory(RestApiChat.PLATFORM_NUMBER,
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, "")).
                enqueue(new Callback<ArrayList<ChatHistoryClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<ChatHistoryClass>> call, @NotNull final Response<ArrayList<ChatHistoryClass>> response) {
                        if (response.isSuccessful()) {
                            Log.d(Logging.debug, "isSuccessful");
                            if (response.body() != null) {
                                Log.d(Logging.debug, "ChatSettingsClass: " + response.body().toString());
                                for (ChatHistoryClass chat : response.body()) {
                                    String value = chat.getCreatedAt();
                                    Calendar current = GregorianCalendar.getInstance();
                                    try {
                                        current.setTime(Objects.requireNonNull(currentFormatterDate.parse(value)));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    switch (chat.getType()) {
                                        case "message":
                                            chatContentList.add(new ChatContent(chat.getFromWhom(),
                                                    chat.getToWhom(),
                                                    chat.getMessage(),
                                                    printedFormatterDate.format(current.getTime()),
                                                    "message",
                                                    "",
                                                    null,
                                                    false,
                                                    chat.getOrderID()));
                                            break;
                                        case "images":
                                            for (String image : chat.getImages()) {
                                                chatContentList.add(new ChatContent(chat.getFromWhom(),
                                                        chat.getToWhom(),
                                                        chat.getMessage(),
                                                        printedFormatterDate.format(current.getTime()),
                                                        "images",
                                                        image,
                                                        null,
                                                        false,
                                                        chat.getOrderID()));
                                            }
                                            break;
                                        case "items":
                                            chatContentList.add(new ChatContent(chat.getFromWhom(),
                                                    chat.getToWhom(),
                                                    chat.getMessage(),
                                                    printedFormatterDate.format(current.getTime()),
                                                    "items",
                                                    "",
                                                    chat.getItems(),
                                                    false,
                                                    chat.getOrderID()));
                                            break;
                                        case "order":
                                            chatContentList.add(new ChatContent(chat.getFromWhom(),
                                                    chat.getToWhom(),
                                                    chat.getMessage(),
                                                    printedFormatterDate.format(current.getTime()),
                                                    "order",
                                                    "",
                                                    null,
                                                    false,
                                                    chat.getOrderID()));
                                            break;
                                    }
                                }
                                chatAdapter = new ChatAdapter(ChatActivity.this, chatContentList);
                                binding.chatRecycler.setAdapter(chatAdapter);
                            } else {
                                Log.e(Logging.error, "Method getChatHistory(): by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getChatHistory() response is not successful." +
                                    " Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<ChatHistoryClass>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getChatHistory() failure: " + t.toString());
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
        chatAdapter = new ChatAdapter(this, chatContentList);
        binding.chatRecycler.setAdapter(chatAdapter);
    }

    private void binding() {
        binding.sendMessage.setOnClickListener(v -> {
            String message = binding.messageField.getText().toString().trim();
            if (!message.isEmpty()) {
                Calendar current = GregorianCalendar.getInstance();
                ChatContent temp = new ChatContent(
                        LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""),
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                        message,
                        printedFormatterDate.format(current.getTime()),
                        "message",
                        "",
                        null,
                        true,
                        "0");
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
                Log.d(Logging.debug, "keyboard opened");
                if (chatContentList.size() != 0) {
                    binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
                }
            } else {
                Log.d(Logging.debug, "keyboard closed");
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
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request STORAGE Permissions Result: Success!");
                    requestPermissions();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestReadWriteStoragePermission(getText(R.string.chatActivityRequestStoragePermission).toString());
                } else {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request STORAGE Permissions Result: Failed!");
                }
                break;
            case REQUEST_PERMISSIONS_CAMERA:
                if (hasCameraPermission()) {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Camera Permissions Result: Success!");
                    callPickImageBottomSheet();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestCameraPermission(getText(R.string.chatActivityRequestCameraPermission).toString());
                } else {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Camera Permissions Result: Failed!");
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
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Log.d(Logging.debug, "Bitmap: " + bitmap.getByteCount());
                new Thread(() -> socketSendPhoto(bitmap)).start();
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
                    Log.d(Logging.debug, " " + e.toString());
                }
                Log.d(Logging.debug, "outFile.getAbsolutePath()" + outFile.getAbsolutePath());

                Calendar current = GregorianCalendar.getInstance();
                ChatContent temp = new ChatContent(
                        LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""),
                        LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                        "",
                        printedFormatterDate.format(current.getTime()),
                        "images",
                        outFile.getAbsolutePath(),
                        null,
                        true,
                        "0");
                chatContentList.add(temp);
                chatAdapter.notifyDataSetChanged();
                binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);

                MediaScannerConnection.scanFile(this,
                        new String[]{outFile.toString()}, null,
                        (path, uri) -> {
                            Log.d(Logging.debug, "Scanned " + path + ":");
                            Log.d(Logging.debug, "-> uri=" + uri);
                        });
            }
            pickImageBottomSheet.dismiss();
        }
    }

    @Override
    public void onSendPictures(HashMap<Integer, String> picturesMap) {
        Log.d(Logging.debug, "onSendPictures()");
        for (Map.Entry<Integer, String> picture : picturesMap.entrySet()) {
            Log.d("AlexDebug", "picture.getKey(): " + picture.getKey());
            Log.d("AlexDebug", "picture.getValue(): " + picture.getValue());
            Calendar current = GregorianCalendar.getInstance();
            chatContentList.add(new ChatContent(
                    LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""),
                    LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""),
                    "",
                    printedFormatterDate.format(current.getTime()),
                    "images",
                    picture.getValue(),
                    null,
                    true,
                    "0"));
            chatAdapter.notifyDataSetChanged();
            binding.chatRecycler.smoothScrollToPosition(chatContentList.size() - 1);
            new Thread(() -> socketSendPictures(picture.getValue())).start();
        }
    }

    private void socketSendPictures(String image) {
        JSONObject jsonObjectItem = new JSONObject();
        JSONArray picturesArray = new JSONArray();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(image)));
            Log.d(Logging.debug, "socketSendPictures: bitmap.getByteCount() " + bitmap.getByteCount());
            String base64 = ConvertingImageToBase64(bitmap);
            JSONObject pictureObject = new JSONObject();
            pictureObject.put("image", base64);
            picturesArray.put(pictureObject);
            jsonObjectItem.put("room_id", LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""));
            jsonObjectItem.put("from_whom", LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""));
            jsonObjectItem.put("to_whom", LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""));
            jsonObjectItem.put("message", "");
            jsonObjectItem.put("items", "{}");
            jsonObjectItem.put("type", "images");
            jsonObjectItem.put("platform", RestAPI.PLATFORM_NUMBER);
            jsonObjectItem.put("images", picturesArray.toString());
            socket.emit("room." + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""), jsonObjectItem);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void socketSendPhoto(Bitmap bitmap) {
        JSONObject jsonObjectItem = new JSONObject();
        JSONArray picturesArray = new JSONArray();
        try {
            String base64 = ConvertingImageToBase64(bitmap);
            JSONObject pictureObject = new JSONObject();
            pictureObject.put("image", base64);
            picturesArray.put(pictureObject);
            jsonObjectItem.put("room_id", LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""));
            jsonObjectItem.put("from_whom", LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""));
            jsonObjectItem.put("to_whom", LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""));
            jsonObjectItem.put("message", "");
            jsonObjectItem.put("items", "{}");
            jsonObjectItem.put("type", "images");//"type"-"images/message"
            jsonObjectItem.put("platform", RestAPI.PLATFORM_NUMBER);
            jsonObjectItem.put("images", picturesArray.toString());
            socket.emit("room." + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""), jsonObjectItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void socketSendMessage(String message) {
        JSONObject jsonObjectItem = new JSONObject();
        try {
            jsonObjectItem.put("room_id", LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""));
            jsonObjectItem.put("from_whom", LoaderActivity.settings.getString(LoaderActivity.CLIENT_CHAT_ID, ""));
            jsonObjectItem.put("to_whom", LoaderActivity.settings.getString(LoaderActivity.SHOP_CHAT_ID, ""));
            jsonObjectItem.put("message", message);
            jsonObjectItem.put("type", "message");//"type"-"images/message"
            jsonObjectItem.put("platform", RestAPI.PLATFORM_NUMBER);
            jsonObjectItem.put("images", "[]");
            jsonObjectItem.put("items", "{}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("room." + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""), jsonObjectItem);
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
        Constants.customerInChat = false;
        socket.off("room." + LoaderActivity.settings.getString(LoaderActivity.ROOM_CHAT_ID, ""));
        socket.disconnect();
        super.onDestroy();
    }
}