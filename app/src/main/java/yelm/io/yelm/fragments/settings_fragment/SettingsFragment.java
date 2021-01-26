package yelm.io.yelm.fragments.settings_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.fragments.settings_fragment.adapter.SettingsContactAdapter;
import yelm.io.yelm.fragments.settings_fragment.adapter.SettingsContactBasicAdapter;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClass;
import yelm.io.yelm.fragments.settings_fragment.model.CompanyClassBasic;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.old_version.user.UserDataActivity;

public class SettingsFragment extends Fragment {

    RecyclerView recyclerContacts;
    private SettingsContactAdapter settingsContactAdapter;

    RecyclerView recyclerContactsBasic;
    private SettingsContactBasicAdapter settingsContactBasicAdapter;

    TextView userData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        userData = view.findViewById(R.id.userData);
        userData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserDataActivity.class);
                startActivity(intent);
            }
        });
        recyclerContacts = view.findViewById(R.id.recyclerContacts);
        recyclerContactsBasic = view.findViewById(R.id.recyclerContactsBasic);

        recyclerContacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerContactsBasic.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        initCompanyContacts();
        initCompanyContactBasic();

        return view;
    }

    private void initCompanyContacts() {
        Log.d("AlexDebug", "LoaderActivity: initCompanyContacts");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getCompany(DynamicURL.getURL(API.URL_API_COMPANY_INFO)).
                enqueue(new Callback<ArrayList<CompanyClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<CompanyClass>> call, Response<ArrayList<CompanyClass>> response) {
                        if (response.isSuccessful()) {
                            settingsContactAdapter = new SettingsContactAdapter(getContext(), response.body());
                            recyclerContacts.setAdapter(settingsContactAdapter);
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<CompanyClass>> call, Throwable t) {
                    }
                });
    }

    private void initCompanyContactBasic() {
        Log.d("AlexDebug", "LoaderActivity: initCompanyContactBasic");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getCompanyBasis(DynamicURL.getURL(API.URL_API_ALL_COMPANY_INFO_BASIC)).
                enqueue(new Callback<ArrayList<CompanyClassBasic>>() {
                    @Override
                    public void onResponse(Call<ArrayList<CompanyClassBasic>> call, Response<ArrayList<CompanyClassBasic>> response) {
                        if (response.isSuccessful()) {
                            Log.d("AlexThread", "Thread initCompanyContactBasic: " + Thread.currentThread().getName());
                            settingsContactBasicAdapter = new SettingsContactBasicAdapter(getContext(), response.body());
                            recyclerContactsBasic.setAdapter(settingsContactBasicAdapter);
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<CompanyClassBasic>> call, Throwable t) {
                    }
                });
    }
}
