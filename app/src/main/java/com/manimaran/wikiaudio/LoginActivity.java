package com.manimaran.wikiaudio;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    // Views
    EditText editUserName, editPassword;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(editUserName.getText()) && !TextUtils.isEmpty(editPassword.getText()))
                {
                    /*Wiki wiki = new Wiki("https://en.wikipedia.org/w/api.php");
                    Boolean res = wiki.login(editUserName.getText().toString(), editPassword.getText().toString());
                    Log.w("Login", "Res " + res);*/
                }else
                    Snackbar.make(btnLogin, "Provide Valid Username & Password", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        editUserName = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
    }
}
