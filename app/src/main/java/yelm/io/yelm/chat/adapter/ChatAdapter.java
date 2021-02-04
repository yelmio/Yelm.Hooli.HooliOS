package yelm.io.yelm.chat.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import yelm.io.yelm.R;
import yelm.io.yelm.chat.controller.ChatActivity;
import yelm.io.yelm.chat.model.ChatContent;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.support_stuff.ImageCornerRadius;
import yelm.io.yelm.support_stuff.ScreenDimensions;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_LEFT_R = 2;
    private static final int MSG_TYPE_RIGHT_R = 3;
    private ArrayList<ChatContent> chatContentList;
    private Context context;
    ScreenDimensions screenDimensions;
    private Listener listener;

    public interface Listener {
        void onComplete();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ChatAdapter(Context context, ArrayList<ChatContent> chatContentList) {
        this.chatContentList = chatContentList;
        this.context = context;
        this.screenDimensions = new ScreenDimensions((Activity) context);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("AlexDebug", "viewType: " + viewType);
        if (viewType == MSG_TYPE_RIGHT) {
            View v = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatContent chatContent = chatContentList.get(position);

        holder.layoutContent.removeAllViews();
        holder.layoutContent.setBackgroundResource(0);
        holder.date.setText(chatContent.getCreated_at());

        if (holder.getItemViewType() == MSG_TYPE_LEFT) {
            holder.nameSender.setText(chatContent.getFrom_whom());
        }

        if (chatContent.getMessage().isEmpty()) {
            if (chatContent.isInner()) {
                Log.d(AlexTAG.debug, "Inner");
                setImageInner(holder, chatContent);
            } else {
                Log.d(AlexTAG.debug, "Outer");
                setImageOuter(holder, chatContent);
            }
        } else {
            setText(holder, chatContent);
        }
    }

    private void setImageInner(@NonNull ViewHolder holder, ChatContent chatContent) {
        for (String image : chatContent.getImages()) {
            ImageCornerRadius imageCornerRadius = new ImageCornerRadius(context);
            imageCornerRadius.setCornerRadius((int) context.getResources().getDimension(R.dimen.dimens_10dp));
            imageCornerRadius.setRoundedCorners(ImageCornerRadius.CORNER_ALL);
            LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsImage.setMargins(0, 0, 0, (int) context.getResources().getDimension(R.dimen.dimens_4dp));

            imageCornerRadius.setLayoutParams(paramsImage);
            Bitmap bitmap = null;
            int newWight = 0;
            int newHeight = 0;
            Uri uri = Uri.fromFile(new File(image));
            Log.d(AlexTAG.debug, "image " + image);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                Log.d(AlexTAG.debug, "bitmap.getWidth() " + bitmap.getWidth());
                Log.d(AlexTAG.debug, "bitmap.getHeight() " + bitmap.getHeight());
                newWight = bitmap.getWidth();
                newHeight = bitmap.getHeight();
                double ratio = (double) bitmap.getWidth() / bitmap.getHeight();

//                if (newWight > (screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity()) {
//                    newWight = (int) ((screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity());
//                }
                newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 1.7);
                if (bitmap.getHeight() > bitmap.getWidth()) {
                    newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 2.4);
                }

                newHeight = (int) (newWight / ratio);

                Log.d(AlexTAG.debug, "ratio " + ratio);
                Log.d(AlexTAG.debug, "newHeight " + newHeight);
                Log.d(AlexTAG.debug, "newWight " + newWight);

                Picasso.get().load(uri)
                        .resize(newWight, newHeight)
                        .centerCrop()
                        .into(imageCornerRadius, new Callback() {
                            @Override
                            public void onSuccess() {
                                //listener.onComplete();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                holder.layoutContent.addView(imageCornerRadius);
                imageCornerRadius.setOnLongClickListener(v -> {
                    popupMenu(context, imageCornerRadius, image);
                    return true;
                });


            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageOuter(@NonNull ViewHolder holder, ChatContent chatContent) {
        for (String image : chatContent.getImages()) {
            ImageCornerRadius imageCornerRadius = new ImageCornerRadius(context);
            imageCornerRadius.setCornerRadius((int) context.getResources().getDimension(R.dimen.dimens_10dp));
            imageCornerRadius.setRoundedCorners(ImageCornerRadius.CORNER_ALL);
            LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageCornerRadius.setLayoutParams(paramsImage);
            holder.layoutContent.addView(imageCornerRadius);

            Log.d(AlexTAG.debug, "image " + image);

            Picasso.get().load(image).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(AlexTAG.debug, "onBitmapLoaded - bitmap.getByteCount(): " + bitmap.getByteCount());

                    int newWight = bitmap.getWidth();
                    int newHeight = bitmap.getHeight();
                    Log.d(AlexTAG.debug, "bitmap.getWidth() " + bitmap.getWidth());
                    Log.d(AlexTAG.debug, "bitmap.getHeight() " + bitmap.getHeight());
                    double ratio = (double) bitmap.getWidth() / bitmap.getHeight();
//                    if (newWight > (screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity()) {
//                        newWight = (int) ((screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity());
//                    }
                    newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 1.7);
                    if (bitmap.getHeight() > bitmap.getWidth()) {
                        newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 2.4);
                    }
                    newHeight = (int) (newWight / ratio);
                    Log.d(AlexTAG.debug, "ratio " + ratio);
                    Log.d(AlexTAG.debug, "newHeight " + newHeight);
                    Log.d(AlexTAG.debug, "newWight " + newWight);
                    imageCornerRadius.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, newWight, newHeight));
//                    Picasso.get().load(image)
//                            .resize(newWight, newHeight)
//                            .into(imageCornerRadius);
                    imageCornerRadius.setOnLongClickListener(v -> {
                        //popupMenu(context, imageCornerRadius, bitmap);
                        return true;
                    });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.d(AlexTAG.debug, "onBitmapFailed " + e.toString());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

//                Bitmap bitmap = getBitmapFromURL(image);
//                int newWight = 0;
//                int newHeight = 0;
//                Log.d(AlexTAG.debug, "bitmap.getWidth() " + bitmap.getWidth());
//                Log.d(AlexTAG.debug, "bitmap.getHeight() " + bitmap.getHeight());
//                newWight = bitmap.getWidth();
//                newHeight = bitmap.getHeight();
//                double ratio = (double) bitmap.getWidth() / bitmap.getHeight();
//
//                //            if (newWight > (screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity()) {
////                newWight = (int) ((screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity());
////                newHeight = (int) (newWight / ratio);
////            }
//
//                newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 1.7);
//                if (bitmap.getHeight() > bitmap.getWidth()) {
//                    newWight = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 2.4);
//                }
//                newHeight = (int) (newWight / ratio);
//
//
//                Log.d(AlexTAG.debug, "ratio " + ratio);
//                Log.d(AlexTAG.debug, "newHeight " + newHeight);
//                Log.d(AlexTAG.debug, "newWight " + newWight);
//
//
//                Picasso.get().load(image)
//                        .resize(newWight, newHeight)
//                        .into(imageCornerRadius);
//                Bitmap finalBitmap = bitmap;
//                imageCornerRadius.setOnLongClickListener(v -> {
//                    popupMenu(context, imageCornerRadius, finalBitmap);
//                    return true;
//                });

        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            Log.d(AlexTAG.debug, "IOException: " + e.getMessage());
            Log.d(AlexTAG.debug, "IOException: " + e.toString());
            return null;
        }
    }

    private void saveImage(Bitmap bitmap) {
        new Thread(() -> {
            FileOutputStream fileOutputStream = null;

            //File file = Environment.getExternalStorageDirectory();
            //Log.d(AlexTAG.debug, "file.getAbsolutePath()" + file.getAbsolutePath());

            File dir = new File(Environment.getExternalStorageDirectory() + "/" + context.getText(R.string.app_name));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Log.d(AlexTAG.debug, "dir.getAbsolutePath()" + dir.getAbsolutePath());

            String fileName = String.format("IMG_%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);

            try {
                fileOutputStream = new FileOutputStream(outFile);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(AlexTAG.debug, " " + e.toString());
            }
            Log.d(AlexTAG.debug, "outFile.getAbsolutePath()" + outFile.getAbsolutePath());

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //update the gallery to gain access to recently added
            MediaScannerConnection.scanFile(context,
                    new String[]{outFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d(AlexTAG.debug, "Scanned " + path + ":");
                            Log.d(AlexTAG.debug, "-> uri=" + uri);
                        }
                    });
        }).start();
    }

    private void popupMenu(Context context, View view, String image) {
        Log.d(AlexTAG.debug, "image " + image);
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.save:
                    new Thread(() -> {
                        Uri uri = Uri.fromFile(new File(image));
                        Log.d(AlexTAG.debug, "uri: " + uri.getPath());
                        Bitmap bitmap;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            saveImage(bitmap);
                            Toast.makeText(context, context.getText(R.string.chatActivitySavedImage), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(AlexTAG.debug, "IOException: " + e.getMessage());
                        }
                    }).start();
                    return true;
                default:
                    return true;
            }
        });
        insertMenuItemIcons(context, popupMenu);
        popupMenu.show();
    }


    public void insertMenuItemIcons(Context context, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();
        if (hasIcon(menu)) {
            for (int i = 0; i < menu.size(); i++) {
                insertMenuItemIcon(context, menu.getItem(i));
            }
        }
    }

    private boolean hasIcon(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getIcon() != null) return true;
        }
        return false;
    }

    private void insertMenuItemIcon(Context context, MenuItem menuItem) {
        Drawable icon = menuItem.getIcon();

        // If there's no icon, we insert a transparent one to keep the title aligned with the items
        // which do have icons.
        if (icon == null) icon = new ColorDrawable(Color.TRANSPARENT);

        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.dimens_24dp);
        icon.setBounds(0, -12, iconSize, iconSize - 12);
        ImageSpan imageSpan = new ImageSpan(icon);

        // Add a space placeholder for the icon, before the title.
        SpannableStringBuilder ssb = new SpannableStringBuilder("     " + menuItem.getTitle());

        // Replace the space placeholder with the icon.
        ssb.setSpan(imageSpan, 1, 2, 0);
        menuItem.setTitle(ssb);
        // Set the icon to null just in case, on some weird devices, they've customized Android to display
        // the icon in the menu... we don't want two icons to appear.
        menuItem.setIcon(null);
    }

    private void setText(@NonNull ViewHolder holder, ChatContent chatContent) {
        TextView textView = new TextView(context);
        textView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.sf_prodisplay_regular), Typeface.NORMAL);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        paramsText.setMargins((int) context.getResources().getDimension(R.dimen.dimens_10dp),
                (int) context.getResources().getDimension(R.dimen.dimens_6dp),
                (int) context.getResources().getDimension(R.dimen.dimens_10dp),
                (int) context.getResources().getDimension(R.dimen.dimens_6dp));
        if (holder.getItemViewType() == MSG_TYPE_RIGHT) {
            textView.setTextColor(context.getResources().getColor(R.color.white));
            holder.layoutContent.setBackgroundResource(R.drawable.chat_message_right);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.colorText));
            holder.layoutContent.setBackgroundResource(R.drawable.chat_message_left);
        }
        textView.setLayoutParams(paramsText);
        textView.setText(chatContent.getMessage());
        holder.layoutContent.addView(textView);
    }

    @Override
    public int getItemCount() {
        return chatContentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameSender, date;
        public LinearLayout layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameSender = itemView.findViewById(R.id.nameSender);
            date = itemView.findViewById(R.id.date);
            layoutContent = itemView.findViewById(R.id.layoutContent);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (chatContentList.get(position).getFrom_whom().equals(LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""))) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}