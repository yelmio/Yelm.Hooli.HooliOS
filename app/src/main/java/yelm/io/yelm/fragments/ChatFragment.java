package yelm.io.yelm.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.DynamicURL;

public class ChatFragment extends Fragment {

    private WebView chat;
    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        chat = v.findViewById(R.id.chat);
        chat.getSettings().setJavaScriptEnabled(true);
        String url = "https://webview.yelm.io/" + userID + "/"+DynamicURL.getPlatformValue();
        chat.loadUrl(url);
        return v;
    }
}