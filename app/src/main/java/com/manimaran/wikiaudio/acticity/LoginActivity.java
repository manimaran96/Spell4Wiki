package com.manimaran.wikiaudio.acticity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.util.PrefManager;
import com.manimaran.wikiaudio.util.UrlType;
import com.manimaran.wikiaudio.wiki.MediaWikiClient;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    // Views
    EditText editUserName, editPassword;
    CircularProgressButton btnLogin;

    PrefManager pref;
    MediaWikiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new PrefManager(getApplicationContext());

        if(pref.isIsLogin())
        {
            launchActivity();
        }
        else {
            init();
            hideKeyboard();
            api = ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext(), UrlType.COMMONS);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editUserName.getText()) && !TextUtils.isEmpty(editPassword.getText())) {
                        hideKeyboard();
                        btnLogin.startAnimation();
                        callToken(editUserName.getText().toString(), editPassword.getText().toString());
                    } else
                        Snackbar.make(btnLogin, getString(R.string.invalid_credential), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void callToken(final String username, final String password)
    {
        Call<ResponseBody> call =  api.getLoginToken();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        String lgToken;
                        JSONObject reader;
                        JSONObject tokenJSONObject;

                        reader = new JSONObject(responseStr);
                        tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                        lgToken = tokenJSONObject.getString("logintoken");
                        completeLogin(username, password, lgToken);

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        showMsg("Please check your connection!");
                        btnLogin.revertAnimation();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                btnLogin.revertAnimation();
                showMsg(getString(R.string.check_internet));
            }
        });
    }

    private void hideKeyboard() {
        try {
            View focus = LoginActivity.this.getCurrentFocus();
            if (focus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completeLogin(String username, String password, String lgToken) {

        Call<ResponseBody> call = api.clientLogin ("clientlogin", "json", ServiceGenerator.getUrl(UrlType.COMMONS, getApplicationContext()), lgToken, username, password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject reader;
                        JSONObject loginJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            loginJSONObject = reader.getJSONObject("clientlogin");
                            String result = loginJSONObject.getString("status");
                            if (result.equals("PASS")) {

                                showMsg("Welcome " + loginJSONObject.getString("username"));

                                //  Write to shared preferences
                                pref.setSession(loginJSONObject.getString("username"), true);

                                btnLogin.doneLoadingAnimation(
                                        ContextCompat.getColor(LoginActivity.this, R.color.w_green),
                                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_done));

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Move to new activity
                                        launchActivity();
                                    }
                                }, 2000);

                            } else if (result.equals("FAIL")) {
                                showMsg(loginJSONObject.getString("message"));
                                btnLogin.revertAnimation();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("Server misbehaved! Please try again later.");
                            btnLogin.revertAnimation();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMsg("Please check your connection!");
                        btnLogin.revertAnimation();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showMsg("Please check your connection!");
                btnLogin.revertAnimation();
            }
        });

    }

    private void launchActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMsg(String msg)
    {
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG).show();
    }

    private void init() {
        editUserName = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(btnLogin != null)
            btnLogin.dispose();
    }
}
