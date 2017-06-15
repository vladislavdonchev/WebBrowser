package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TextWatcher, WebViewFragment.HideKeyboardListener, View.OnKeyListener {

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private ImageButton menuButton;
    private Button goButton;
    private WebViewFragment activeFragment;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (ImageButton) findViewById(R.id.activity_main_menu_button);
        goButton = (Button) findViewById(R.id.activity_main_go_button);
        container = (LinearLayout) findViewById(R.id.activity_main_web_view_container);

        goButton.setEnabled(false);

        addNewTab();

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        goButton.setOnClickListener(this);

        addressBarEditText.setOnKeyListener(this);
        addressBarEditText.addTextChangedListener(this);

        if (savedInstanceState != null) {
            addressBarEditText.setText(savedInstanceState.getString(Constants.ADDRESS_BAR_TEXT_KEY, ""));
        }
    }

    private void addNewTab() {
        activeFragment = new WebViewFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction  = fragmentManager.beginTransaction();
        transaction.replace(R.id.activity_main_web_view_container,activeFragment, "Fragment" + fragmentManager.getFragments().size());

        transaction.commit();

        activeFragment.setHideKeyboardListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        activeFragment.saveWebViewState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_new_tab_button:
                addNewTab();
                break;
            case R.id.activity_main_menu_button:
                Toast.makeText(this, "Menu button clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.activity_main_go_button:
                activeFragment.loadURL(addressBarEditText.getText().toString());
                break;
        }

        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        activeFragment.onBackPressed();
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(addressBarEditText.getWindowToken(), 0);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        goButton.setEnabled(editable.length() != 0);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (i) {
                case KeyEvent.KEYCODE_ENTER:
                    activeFragment.loadURL(addressBarEditText.getText().toString());
                    hideKeyboard();
                    return true;
                default:
                    break;
            }
        }

        return false;
    }
}
