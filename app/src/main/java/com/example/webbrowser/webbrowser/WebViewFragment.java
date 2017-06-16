package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
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

import java.lang.ref.WeakReference;

/**
 * Created by Asus on 6/15/2017.
 */

public class WebViewFragment extends Fragment implements View.OnTouchListener {

    public static final String TAG = WebViewFragment.class.getName();

    private WebView webView;
    private ProgressBar webViewProgressBar;
    private WeakReference<HideKeyboardListener> hideKeyboardListener;
    private WeakReference<WebViewNavigationListener> navigationListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_web_view, container, false);
        webView = (WebView) content.findViewById(R.id.fragment_web_view);

        webViewProgressBar = (ProgressBar) content.findViewById(R.id.fragment_progress_bar);
        webViewProgressBar.setMax(100);
        webViewProgressBar.setVisibility(View.GONE);

        webView.setOnTouchListener(this);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }

        return content;
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

    public String getPageTitle() {
        if (webView == null) {
            return "No Title";
        } else {
            return webView.getTitle();
        }
    }

    public void saveWebViewState(Bundle outState) {
        webView.saveState(outState);
    }

    public void setHideKeyboardListener(HideKeyboardListener hideKeyboardListener) {
        this.hideKeyboardListener = new WeakReference<HideKeyboardListener>(hideKeyboardListener);
    }

    public void setNavigationListener(WebViewNavigationListener webViewNavigationListener) {
        this.navigationListener = new WeakReference<WebViewNavigationListener>(webViewNavigationListener);
    }

    public interface HideKeyboardListener {
        void hideKeyboard();
    }

    public interface WebViewNavigationListener {
        void updatePageTitle();
        void closeApp();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideKeyboardListener.get().hideKeyboard();
        return false;
    }

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            navigationListener.get().closeApp();
        }
    }

    public void loadURL(String url) {
        WebSettings wbset = webView.getSettings();
        wbset.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.loadUrl(formattedURL(url));
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
            navigationListener.get().updatePageTitle();
            webViewProgressBar.setVisibility(View.GONE);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webViewProgressBar.setProgress(0);
            webViewProgressBar.setVisibility(View.VISIBLE);

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
