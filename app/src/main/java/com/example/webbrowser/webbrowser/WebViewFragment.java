package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Asus on 6/15/2017.
 */

public class WebViewFragment extends Fragment implements View.OnTouchListener {

    public static final String TAG = WebViewFragment.class.getName();
    public static final String WEBVIEW_STATE_KEY = "webViewStateKey";

    private WebView webView;
    private ProgressBar webViewProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_web_view, container, false);
        webView = (WebView) content.findViewById(R.id.fragment_web_view);

        WebSettings wbset = webView.getSettings();
        wbset.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.setOnTouchListener(this);

        webViewProgressBar = (ProgressBar) content.findViewById(R.id.fragment_progress_bar);
        webViewProgressBar.setMax(100);
        webViewProgressBar.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            Log.d(TAG, "restoreWebViewState");
        } else {
            reload();
        }

        return content;
    }

    public String getFragmentURL() {
        if (webView == null || webView.getUrl() == null) {
            return "";
        }
        return webView.getUrl().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle webViewInstanceState = new Bundle();
        webView.saveState(webViewInstanceState);
        outState.putBundle(WEBVIEW_STATE_KEY, webViewInstanceState);
        super.onSaveInstanceState(outState);
        Log.d(TAG, "saveFragmentInstanceState");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");
    }

    public String getPageTitle() {
        if (webView == null || TextUtils.isEmpty(webView.getTitle())) {
            return "New Tab";
        } else {
            return webView.getTitle();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Intent intent = new Intent(Constants.HIDE_KEYBOARD_ACTION);
        getContext().sendBroadcast(intent);

        return false;
    }

    public boolean onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return false;
    }

    public void loadURL(String url) {
        webViewProgressBar.setProgress(2);
        webViewProgressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(formattedURL(url));
    }

    public void reload() {
        if (webView == null || webView.getUrl() == null) {
            return;
        }

        webViewProgressBar.setProgress(2);
        webViewProgressBar.setVisibility(View.VISIBLE);
        webView.reload();
    }

    private String formattedURL(String input) {
        String formattedURL = input.toLowerCase();
        if (!formattedURL.startsWith("http://") && !formattedURL.startsWith("https://")) {
            formattedURL = "http://" + input;
        }
        return formattedURL;
    }

    private void updateProgressBar(int progress) {
        webViewProgressBar.setProgress(progress);

        if (progress >= 100) {
            Intent intent = new Intent(Constants.UPDATE_PAGE_TITLE_ACTION);
            getContext().sendBroadcast(intent);

            webViewProgressBar.setVisibility(View.GONE);

            Intent didLoadIntent = new Intent(Constants.WEB_VIEW_DID_LOAD_ACTION);
            didLoadIntent.putExtra(Constants.WEB_VIEW_FRAGMENT_URL_KEY, webView.getUrl().toString());
            getContext().sendBroadcast(didLoadIntent);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            updateProgressBar(newProgress);
        }
    }
}
