package com.example.webbrowser.webbrowser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private WebView webView;
    private ProgressBar webViewProgressBar;
    private WeakReference<HideKeyboardListener> hideKeyboardListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_web_view, container);
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

    public void saveWebViewState(Bundle outState) {
        webView.saveState(outState);
    }

    public void setHideKeyboardListener(HideKeyboardListener hideKeyboardListener) {
        this.hideKeyboardListener = new WeakReference<HideKeyboardListener>(hideKeyboardListener);
    }

    public interface HideKeyboardListener {
        void hideKeyboard();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideKeyboardListener.get().hideKeyboard();
        return false;
    }

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
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

        if (progress>=100) {
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
