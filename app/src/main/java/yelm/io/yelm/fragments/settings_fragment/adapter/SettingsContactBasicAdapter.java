package yelm.io.yelm.fragments.settings_fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yelm.io.yelm.R;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClassBasic;

public class SettingsContactBasicAdapter extends RecyclerView.Adapter<SettingsContactBasicAdapter.ContactBasicHolder> {

    private Context context;
    private List<CompanyClassBasic> contacts;

    public SettingsContactBasicAdapter(Context context, List<CompanyClassBasic> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactBasicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.contact_item_basic, parent, false);
        return new ContactBasicHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactBasicHolder holder, final int position) {
        final CompanyClassBasic companyContactBasic = contacts.get(position);
        holder.textContact.setText(companyContactBasic.getName());
        holder.textContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(companyContactBasic.getContent()));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactBasicHolder extends RecyclerView.ViewHolder {
        public TextView textContact;

        public ContactBasicHolder(@NonNull View itemView) {
            super(itemView);
            textContact = itemView.findViewById(R.id.categoryExpand);


        }
    }
}
