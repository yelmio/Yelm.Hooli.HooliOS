package yelm.io.yelm.fragments.settings_fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClass;

public class SettingsContactAdapter extends RecyclerView.Adapter<SettingsContactAdapter.ContactHolder> {

    private Context context;
    private List<CompanyClass> contacts;

    private Listener listener;

    public interface Listener {
        void onClick(int id, CompanyClass contact);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public SettingsContactAdapter(Context context, List<CompanyClass> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ContactHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, final int position) {
        final CompanyClass companyContact = contacts.get(position);
        Integer imageUrl = R.drawable.ic_warning_24;
        Log.d("AlexDebug", "companyContact.getContent(): " + companyContact.getContent());
        switch (companyContact.getName()) {
            case "Позвонить":
                imageUrl = R.drawable.ic_phone_settings_24;
                holder.textContact.setText(R.string.call);
                holder.textContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + companyContact.getContent()));
                        context.startActivity(callIntent);
                    }
                });
                break;
            case "Почта":
                imageUrl = R.drawable.ic_mail_24;
                holder.textContact.setText(R.string.mail);
                holder.textContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + companyContact.getContent()));
                        context.startActivity(intent);
                    }
                });
                break;
            case "Whatsapp":
                imageUrl = R.drawable.ic_whatsapp_24;
                holder.textContact.setText(R.string.whatsapp);
//                String url = "https://api.whatsapp.com/send?phone=" + companyContact.getContent();
//                holder.textContact.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(url));
//                        context.startActivity(i);
//                //    i.setPackage("com.whatsapp");  //need or not ???
//                    }
//                });
                setOnClickListener(holder, "https://api.whatsapp.com/send?phone=" + companyContact.getContent());
                break;
            case "Instagram":
                imageUrl = R.drawable.ic_instagram_24;
                holder.textContact.setText(R.string.instagram);
                holder.imageContact.setPadding(4, 4, 4, 4);
                setOnClickListener(holder, companyContact.getContent());
                break;
            case "Сайт":
                imageUrl = R.drawable.ic_link_settings_24;
                holder.textContact.setText(R.string.website);
                setOnClickListener(holder, companyContact.getContent());
                break;
            case "Адрес магазина":
                imageUrl = R.drawable.ic_location_settings_24;
                holder.textContact.setText(companyContact.getContent());
                boolean isInstalled = isPackageInstalled("ru.yandex.yandexmaps", context.getPackageManager());
                String address = companyContact.getContent().replace(" ", "%20");
                if (isInstalled) {
                    setOnClickListener(holder, "yandexmaps://maps.yandex.ru/?text=" + address);
                } else {
                    setOnClickListener(holder, "https://yandex.ru/maps/?text=" + address);
                }
                break;
        }
        holder.imageContact.setImageResource(imageUrl);
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private void setOnClickListener(ContactHolder holder, final String uri) {
        holder.textContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        public TextView textContact;
        public ImageView imageContact;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            imageContact = itemView.findViewById(R.id.imageContact);
            textContact = itemView.findViewById(R.id.textContact);
        }
    }
}
