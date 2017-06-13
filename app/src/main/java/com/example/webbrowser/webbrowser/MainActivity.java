package com.example.webbrowser.webbrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText addresdsBarEditText;
    private Button addNewTabButton;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addresdsBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (ImageButton) findViewById(R.id.activity_main_menu_button);

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_new_tab_button:
                Toast.makeText(this, "New tab button clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.activity_main_menu_button:
                Toast.makeText(this, "Menu button clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
