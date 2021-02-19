package yelm.io.yelm.chat.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.chat.model.ChatContent;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.item.ItemActivity;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.order.OrderActivity;
import yelm.io.yelm.order.user_order.OrderByIDActivity;
import yelm.io.yelm.constants.Logging;
import yelm.io.yelm.support_stuff.ScreenDimensions;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MSG_TYPE_MESSAGE_LEFT = 0;
    private static final int MSG_TYPE_MESSAGE_RIGHT = 1;
    private static final int MSG_TYPE_PICTURE_LEFT = 2;
    private static final int MSG_TYPE_PICTURE_RIGHT = 3;
    private static final int MSG_TYPE_ITEM = 4;
    private static final int MSG_TYPE_ORDER = 5;
    private ArrayList<ChatContent> chatContentList;
    private Context context;
    ScreenDimensions screenDimensions;
    int widthFixedPortrait = 1;
    int widthFixedLandscape = 1;
    private static final String[] READ_WRITE_EXTERNAL_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_PERMISSIONS_READ_WRITE_STORAGE = 333;

    public ChatAdapter(Context context, ArrayList<ChatContent> chatContentList) {
        this.chatContentList = chatContentList;
        this.context = context;
        this.screenDimensions = new ScreenDimensions((Activity) context);
        widthFixedLandscape = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 1.7);
        widthFixedPortrait = (int) ((screenDimensions.getWidthDP() - 32) * screenDimensions.getScreenDensity() / 2.4);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(Logging.debug, "viewType: " + viewType);
        switch (viewType) {
            case MSG_TYPE_ITEM:
                return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_item, parent, false));
            case MSG_TYPE_MESSAGE_RIGHT:
                return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_message_right, parent, false));
            case MSG_TYPE_PICTURE_RIGHT:
                return new PictureHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_picture_right, parent, false));
            case MSG_TYPE_PICTURE_LEFT:
                return new PictureHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_picture_left, parent, false));
            case MSG_TYPE_ORDER:
                return new OrderHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_order, parent, false));
            default:
                return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.chat_type_message_left, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatContent chatContent = chatContentList.get(position);
        if (holder instanceof MessageHolder) {
            Log.d(Logging.debug, "MessageHolder");
            ((MessageHolder) holder).date.setText(chatContent.getCreated_at());
            ((MessageHolder) holder).nameSender.setText(context.getResources().getText(R.string.app_name));
            ((MessageHolder) holder).message.setText(chatContent.getMessage());
        }
        if (holder instanceof PictureHolder) {
            Log.d(Logging.debug, "PictureHolder");
            ((PictureHolder) holder).date.setText(chatContent.getCreated_at());
            ((PictureHolder) holder).nameSender.setText(context.getResources().getText(R.string.app_name));
            if (chatContent.isInner()) {
                Log.d(Logging.debug, "Inner");
                setImageInner((PictureHolder) holder, chatContent);
            } else {
                Log.d(Logging.debug, "Outer");
                setImageOuter((PictureHolder) holder, chatContent);
            }
        }
        if (holder instanceof ItemHolder) {
            Log.d(Logging.debug, "ItemHolder");
            setItem((ItemHolder) holder, chatContent);
        }

        if (holder instanceof OrderHolder) {
            Log.d(Logging.debug, "OrderHolder");
            ((OrderHolder) holder).date.setText(chatContent.getCreated_at());
            ((OrderHolder) holder).nameSender.setText(context.getResources().getText(R.string.app_name));
            ((OrderHolder) holder).message.setText(chatContent.getMessage());
            ((OrderHolder) holder).details.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderByIDActivity.class);
                intent.putExtra("id", chatContent.getOrderID());
                context.startActivity(intent);
            });
        }
    }

    private void setItem(@NonNull ItemHolder holder, ChatContent chatContent) {
        holder.date.setText(chatContent.getCreated_at());
        holder.nameSender.setText(context.getResources().getText(R.string.app_name));
        Picasso.get().load(chatContent.getItem().getPreviewImage())
                .resize(300, 300)
                .centerCrop()
                .into(holder.image);

        List<BasketCart> listBasketCartByItemID = Common.basketCartRepository.getListBasketCartByItemID(chatContent.getItem().getId());
        if (listBasketCartByItemID != null && listBasketCartByItemID.size() != 0) {
            BigInteger countOfAllProducts = new BigInteger("0");
            for (BasketCart basketCart : listBasketCartByItemID) {
                countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
            }
            holder.countItemInCart.setText(String.format("%s", countOfAllProducts));
            holder.removeProduct.setVisibility(View.VISIBLE);
            holder.countItemsLayout.setVisibility(View.VISIBLE);
        }
        holder.cardProduct.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("item", chatContent.getItem());
            context.startActivity(intent);
        });
        holder.description.setText(chatContent.getItem().getName());
        holder.weight.setText(String.format("%s %s", chatContent.getItem().getUnitType(), chatContent.getItem().getType()));

        //calculate final price depending on the discount
        BigDecimal bd = new BigDecimal(chatContent.getItem().getPrice());
        if (chatContent.getItem().getDiscount().equals("0")) {
            holder.priceFinal.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.priceStart.setVisibility(View.GONE);
        } else {
            holder.discountProcent.setVisibility(View.VISIBLE);
            holder.discountProcent.setText(String.format("- %s %%", chatContent.getItem().getDiscount()));
            bd = new BigDecimal(chatContent.getItem().getDiscount()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            bd = bd.multiply(new BigDecimal(chatContent.getItem().getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);
            bd = new BigDecimal(chatContent.getItem().getPrice()).subtract(bd);
            //trim zeros if after comma there are only zeros: 45.00 -> 45
            if (bd.compareTo(new BigDecimal(String.valueOf(bd.setScale(0, BigDecimal.ROUND_HALF_UP)))) == 0) {
                bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            }
            holder.priceFinal.setText(String.format("%s %s", bd.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.priceStart.setText(String.format("%s %s", chatContent.getItem().getPrice(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            holder.priceStart.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //add product into basket
        BigDecimal finalBd = bd;
        holder.addProduct.setOnClickListener(v -> {
            if (chatContent.getItem().getModifier().size() != 0) {
                //showBottomSheetDialog(holder, current, finalBd);
            } else {
                holder.countItemsLayout.setVisibility(View.VISIBLE);
                List<BasketCart> listCartsByID = Common.basketCartRepository.getListBasketCartByItemID(chatContent.getItem().getId());
                if (listCartsByID != null && listCartsByID.size() != 0) {
                    BigInteger countOfAllProducts = new BigInteger("0");
                    for (BasketCart basketCart : listCartsByID) {
                        countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
                    }
                    for (BasketCart basketCart : listCartsByID) {
                        if (basketCart.modifier.equals(chatContent.getItem().getModifier())) {
                            basketCart.count = new BigDecimal(basketCart.count).add(new BigDecimal("1")).toString();
                            holder.countItemInCart.setText(String.format("%s", countOfAllProducts.add(new BigInteger("1"))));
                            Common.basketCartRepository.updateBasketCart(basketCart);
                            Log.d(Logging.debug, "Method add BasketCart to Basket. No modifiers - listCartsByID !=null:  " + basketCart.toString());
                            return;
                        }
                    }
                }
                holder.removeProduct.setVisibility(View.VISIBLE);
                holder.countItemInCart.setText("1");
                BasketCart cartItem = new BasketCart();
                cartItem.itemID = chatContent.getItem().getId();
                cartItem.name = chatContent.getItem().getName();
                cartItem.discount = chatContent.getItem().getDiscount();
                cartItem.startPrice = chatContent.getItem().getPrice();
                cartItem.finalPrice = finalBd.toString();
                cartItem.type = chatContent.getItem().getType();
                cartItem.count = "1";
                cartItem.quantity = chatContent.getItem().getQuantity();
                cartItem.imageUrl = chatContent.getItem().getPreviewImage();
                cartItem.discount = chatContent.getItem().getDiscount();
                cartItem.modifier = chatContent.getItem().getModifier();
                cartItem.isPromo = false;
                cartItem.isExist = true;
                cartItem.quantityType = chatContent.getItem().getUnitType();
                Common.basketCartRepository.insertToBasketCart(cartItem);
                Log.d(Logging.debug, "Method add BasketCart to Basket. No modifiers - listCartsByID == null:  " + cartItem.toString());
            }
        });

        //remove product from basket
        holder.removeProduct.setOnClickListener(v -> {
            List<BasketCart> listCartsByID = Common.basketCartRepository.getListBasketCartByItemID(chatContent.getItem().getId());
            if (listCartsByID != null && listCartsByID.size() != 0) {
                if (listCartsByID.size() == 1) {
                    BasketCart cartItem = listCartsByID.get(0);
                    BigInteger countOfProduct = new BigInteger(cartItem.count);
                    if (countOfProduct.equals(new BigInteger("1"))) {
                        holder.countItemsLayout.setVisibility(View.GONE);
                        holder.removeProduct.setVisibility(View.GONE);
                        Common.basketCartRepository.deleteBasketCart(cartItem);
                    } else {
                        countOfProduct = countOfProduct.subtract(new BigInteger("1"));
                        cartItem.count = countOfProduct.toString();
                        holder.countItemInCart.setText(cartItem.count);
                        Common.basketCartRepository.updateBasketCart(cartItem);
                    }
                } else {
                    BigInteger countOfAllProducts = new BigInteger("0");
                    for (BasketCart basketCart : listCartsByID) {
                        countOfAllProducts = countOfAllProducts.add(new BigInteger(basketCart.count));
                    }
                    BasketCart cartItem = listCartsByID.get(listCartsByID.size() - 1);
                    BigInteger countOfProduct = new BigInteger(cartItem.count);
                    if (countOfProduct.equals(new BigInteger("1"))) {
                        Common.basketCartRepository.deleteBasketCart(cartItem);
                    } else {
                        countOfProduct = countOfProduct.subtract(new BigInteger("1"));
                        cartItem.count = countOfProduct.toString();
                        holder.countItemInCart.setText(String.format("%s", countOfAllProducts.subtract(new BigInteger("1"))));
                        Common.basketCartRepository.updateBasketCart(cartItem);
                    }
                }
            }
        });
    }

    private void setImageInner(@NonNull PictureHolder holder, ChatContent chatContent) {
        Log.d(Logging.debug, "image: " + chatContent.getImage());
        try {
            Uri uri = Uri.fromFile(new File(chatContent.getImage()));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            Log.d(Logging.debug, "bitmap.getWidth() " + bitmap.getWidth());
            Log.d(Logging.debug, "bitmap.getHeight() " + bitmap.getHeight());
            int newWight;
            int newHeight;
            double ratio = (double) bitmap.getWidth() / bitmap.getHeight();
//                    if (newWight > (screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity()) {
//                        newWight = (int) ((screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity());
//                    }
            if (bitmap.getHeight() > bitmap.getWidth()) {
                newWight = widthFixedPortrait;
            } else {
                newWight = widthFixedLandscape;
            }
            newHeight = (int) (newWight / ratio);
            Log.d(Logging.debug, "ratio " + ratio);
            Log.d(Logging.debug, "newHeight " + newHeight);
            Log.d(Logging.debug, "newWight " + newWight);

            Picasso.get().load(uri)
                    .resize(newWight, newHeight)
                    .centerCrop()
                    .into(holder.image);
            holder.image.setOnLongClickListener(v -> {
                popupMenu(context, holder.image, chatContent.getImage(), chatContent.isInner());
                return true;
            });
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasReadExternalStoragePermission() {
        int result = ContextCompat
                .checkSelfPermission(context.getApplicationContext(), READ_WRITE_EXTERNAL_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void setImageOuter(@NonNull PictureHolder holder, ChatContent chatContent) {


        Log.d(Logging.debug, "image: " + chatContent.getImage());
        Picasso.get().load(chatContent.getImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(Logging.debug, "onBitmapLoaded - bitmap.getByteCount(): " + bitmap.getByteCount());
                Log.d(Logging.debug, "bitmap.getWidth() " + bitmap.getWidth());
                Log.d(Logging.debug, "bitmap.getHeight() " + bitmap.getHeight());
                double ratio = (double) bitmap.getWidth() / bitmap.getHeight();
                int newWight;
                int newHeight;
//                    if (newWight > (screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity()) {
//                        newWight = (int) ((screenDimensions.getWidthDP() - 64) * screenDimensions.getScreenDensity());
//                    }
                if (bitmap.getHeight() > bitmap.getWidth()) {
                    newWight = widthFixedPortrait;
                } else {
                    newWight = widthFixedLandscape;
                }
                newHeight = (int) (newWight / ratio);
                Log.d(Logging.debug, "ratio " + ratio);
                Log.d(Logging.debug, "newHeight " + newHeight);
                Log.d(Logging.debug, "newWight " + newWight);
                //   java.lang.IllegalArgumentException: x + width must be <= bitmap.width()
                //holder.image.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, newWight, newHeight));
                Picasso.get().load(chatContent.getImage())
                        .resize(newWight, newHeight)
                        .into(holder.image);
                holder.image.setOnLongClickListener(v -> {
                    popupMenu(context, holder.image, chatContent.getImage(), chatContent.isInner());
                    return true;
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.d(Logging.debug, "onBitmapFailed " + e.toString());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
//                Bitmap bitmap = getBitmapFromURL(image);
//                int newWight = 0;
//                int newHeight = 0;
//                Log.d(Logging.debug, "bitmap.getWidth() " + bitmap.getWidth());
//                Log.d(Logging.debug, "bitmap.getHeight() " + bitmap.getHeight());
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
//                Log.d(Logging.debug, "ratio " + ratio);
//                Log.d(Logging.debug, "newHeight " + newHeight);
//                Log.d(Logging.debug, "newWight " + newWight);
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

    private void popupMenu(Context context, View view, String image, boolean inner) {
        Log.d(Logging.debug, "image: " + image);

        if (!hasReadExternalStoragePermission()){
            ActivityCompat.requestPermissions((Activity) context, READ_WRITE_EXTERNAL_PERMISSIONS, REQUEST_PERMISSIONS_READ_WRITE_STORAGE);
        }

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_menu);
        if (inner) {
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (hasReadExternalStoragePermission()) {
                    if (menuItem.getItemId() == R.id.save) {
                        new Thread(() -> {
                            Uri uri = Uri.fromFile(new File(image));
                            Log.d(Logging.debug, "uri: " + uri.getPath());
                            Bitmap bitmap;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                                saveImage(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(Logging.debug, "IOException: " + e.getMessage());
                            }
                        }).start();
                        Toast.makeText(context, context.getText(R.string.chatActivitySavedImage), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else {
                    showToast(context.getResources().getText(R.string.chatActivityRequestStoragePermissionForSaveImages).toString());
                }
                return true;
            });
        } else {
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (hasReadExternalStoragePermission()) {
                    if (menuItem.getItemId() == R.id.save) {
                        Picasso.get().load(image).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                saveImage(bitmap);
                                Toast.makeText(context, context.getText(R.string.chatActivitySavedImage), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                e.printStackTrace();
                                Log.d(Logging.debug, "IOException: " + e.getMessage());
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        });
                        return true;
                    }
                } else {
                    showToast(context.getResources().getText(R.string.chatActivityRequestStoragePermissionForSaveImages).toString());
                }
                return true;
            });
        }
        insertMenuItemIcons(context, popupMenu);
        popupMenu.show();
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
            Log.d(Logging.debug, "IOException: " + e.getMessage());
            Log.d(Logging.debug, "IOException: " + e.toString());
            return null;
        }
    }

    private void saveImage(Bitmap bitmap) {
        new Thread(() -> {
            FileOutputStream fileOutputStream = null;

            //File file = Environment.getExternalStorageDirectory();
            //Log.d(Logging.debug, "file.getAbsolutePath()" + file.getAbsolutePath());

            File dir = new File(Environment.getExternalStorageDirectory() + "/" + context.getText(R.string.app_name));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Log.d(Logging.debug, "dir.getAbsolutePath()" + dir.getAbsolutePath());

            String fileName = String.format("IMG_%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);

            try {
                fileOutputStream = new FileOutputStream(outFile);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(Logging.debug, " " + e.toString());
            }
            Log.d(Logging.debug, "outFile.getAbsolutePath()" + outFile.getAbsolutePath());

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
                            Log.d(Logging.debug, "Scanned " + path + ":");
                            Log.d(Logging.debug, "-> uri=" + uri);
                        }
                    });
        }).start();
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

    @Override
    public int getItemCount() {
        return chatContentList.size();
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {
        public TextView nameSender, date, message, details;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            nameSender = itemView.findViewById(R.id.nameSender);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            details = itemView.findViewById(R.id.details);
        }
    }

    public static class OrderHolder extends RecyclerView.ViewHolder {
        public TextView nameSender, date, message, details;

        public OrderHolder(@NonNull View itemView) {
            super(itemView);
            nameSender = itemView.findViewById(R.id.nameSender);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            details = itemView.findViewById(R.id.details);
        }
    }

    public static class PictureHolder extends RecyclerView.ViewHolder {
        public TextView nameSender, date;
        public ImageView image;

        public PictureHolder(@NonNull View itemView) {
            super(itemView);
            nameSender = itemView.findViewById(R.id.nameSender);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.image);
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        public TextView nameSender, date, description, priceStart, weight, priceFinal, discountProcent, countItemInCart;
        public ImageView image;
        public ImageButton removeProduct, addProduct;
        public ConstraintLayout countItemsLayout;
        public CardView cardProduct;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            nameSender = itemView.findViewById(R.id.nameSender);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.image);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            description = itemView.findViewById(R.id.description);
            priceStart = itemView.findViewById(R.id.priceStart);
            weight = itemView.findViewById(R.id.weight);
            priceFinal = itemView.findViewById(R.id.priceFinal);
            removeProduct = itemView.findViewById(R.id.removeProduct);
            addProduct = itemView.findViewById(R.id.addProduct);
            discountProcent = itemView.findViewById(R.id.discountProcent);
            countItemsLayout = itemView.findViewById(R.id.countItemsLayout);
            countItemInCart = itemView.findViewById(R.id.countItemInCart);
        }
    }

    public void showToast(String message) {
        Log.d(Logging.debug, "message: " + message);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(Logging.debug, "chatContentList.get(position).getType(): " + chatContentList.get(position).getType());

        switch (chatContentList.get(position).getType()) {
            case "items":
                return MSG_TYPE_ITEM;
            case "order":
                return MSG_TYPE_ORDER;
            case "message":
                if (chatContentList.get(position).getFrom_whom().equals(LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""))) {
                    return MSG_TYPE_MESSAGE_RIGHT;
                } else {
                    return MSG_TYPE_MESSAGE_LEFT;
                }
            case "images":
                if (chatContentList.get(position).getFrom_whom().equals(LoaderActivity.settings.getString(LoaderActivity.CLIENT_ID, ""))) {
                    return MSG_TYPE_PICTURE_RIGHT;
                } else {
                    return MSG_TYPE_PICTURE_LEFT;
                }
            default:
                return MSG_TYPE_MESSAGE_RIGHT;
        }
    }
}