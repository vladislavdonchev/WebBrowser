package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public static final String LOG_TAG = WebViewFragment.class.getName();
    public static final String WEBVIEW_STATE_KEY = "webViewStateKey";

    private WebView webView;
    private ProgressBar webViewProgressBar;
    private Bundle webViewState;
    private String uid;

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
        if (webViewState != null) {
            webView.restoreState(webViewState);
        }

        webViewProgressBar = (ProgressBar) content.findViewById(R.id.fragment_progress_bar);
        webViewProgressBar.setMax(100);
        webViewProgressBar.setVisibility(View.GONE);

        return content;
    }

    public String getFragmentURL() {
        if (webView == null || webView.getUrl() == null) {
            return "";
        }
        return webView.getUrl().toString();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate" + " " + uid);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach" + " " + uid);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart" + " " + uid);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume" + " " + uid);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause" + " " + uid);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop" + " " + uid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "onDestroyView" + " " + uid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy" + " " + uid);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach" + " " + uid);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated" + " " + uid);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle webViewInstanceState = new Bundle();
        webView.saveState(webViewInstanceState);
        outState.putBundle(WEBVIEW_STATE_KEY, webViewInstanceState);
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "saveFragmentInstanceState" + " " + uid);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(LOG_TAG, "onViewStateRestored" + " " + uid);
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
        Log.d(WebViewFragment.class.getName(), "Loading url: " + url);
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
            Log.d(LOG_TAG, "URL load finished." + " " + uid);

            webViewProgressBar.setVisibility(View.GONE);

            Intent pageLoadedIntent = new Intent(Constants.WEB_PAGE_LOADED_ACTION);
            pageLoadedIntent.putExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_TITLE_KEY, webView.getTitle());
            pageLoadedIntent.putExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_URL_KEY, webView.getUrl().toString());
            pageLoadedIntent.putExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_UID, uid);

            getContext().sendBroadcast(pageLoadedIntent);
        }
    }

    public void setWebViewState(Bundle webViewState) {
        this.webViewState = webViewState;
    }

    public Bundle getWebViewState() {
        Bundle webViewState = new Bundle();
        webView.saveState(webViewState);
        return webViewState;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
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

            if (getContext() != null) {
                updateProgressBar(newProgress);
            }
        }
    }
}
