package com.manimaran.wikiaudio.acticity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manimaran.wikiaudio.R;
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

    SharedPreferences pref;
    MediaWikiClient api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = getApplicationContext().getSharedPreferences(
                getString(R.string.pref_file_key),
                Context.MODE_PRIVATE
        );

        if(pref.getBoolean(getString(R.string.pref_is_logged_in), false))
        {
            // Todo : start home screen
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        else {
            init();
            hideKeyboard();
            api = ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext());

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editUserName.getText()) && !TextUtils.isEmpty(editPassword.getText())) {
                    /*Wiki wiki = new Wiki("https://en.wikipedia.org/w/api.php");
                    Boolean res = wiki.login(editUserName.getText().toString(), editPassword.getText().toString());
                    Log.w("Login", "Res " + res);*/
                        hideKeyboard();
                        btnLogin.startAnimation();
                        callToken(editUserName.getText().toString(), editPassword.getText().toString());

                    } else
                        Snackbar.make(btnLogin, "Provide Valid Username & Password", Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void callToken(final String username, final String password)
    {
        Call<ResponseBody> call =  api.getToken();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        String lgToken;
                        JSONObject reader;
                        JSONObject tokenJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                            //noinspection SpellCheckingInspection
                            lgToken = tokenJSONObject.getString("logintoken");

                            //Log.w("Wiki", " Token " + lgToken);
                            //Toast.makeText(getApplicationContext(), "Token " + lgToken , Toast.LENGTH_LONG).show();
                            completeLogin(username, password, lgToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //loginFailed("Server misbehaved! Please try again later.");
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
                t.printStackTrace();
                btnLogin.revertAnimation();
                showMsg("Please check your connection!.. out");
            }
        });
    }

    private void hideKeyboard()
    {
        try {
            View focus = LoginActivity.this.getCurrentFocus();
            if (focus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void completeLogin(String username, String password, String lgToken) {

        Call<ResponseBody> call = api.clientLogin ("clientlogin", "json", "https://www.google.com/", lgToken, username, password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject reader;
                        JSONObject loginJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            Log.w("Log", "Val " + new Gson().toJson(reader));
                            loginJSONObject = reader.getJSONObject("clientlogin");
                            String result = loginJSONObject.getString("status");
                            if (result.equals("PASS")) {
                                //  Write to shared preferences
                                showMsg("Welcome " + loginJSONObject.getString("username"));
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("username", loginJSONObject.getString("username"));
                                editor.putBoolean(getString(R.string.pref_is_logged_in), true);
                                editor.apply();

                                btnLogin.doneLoadingAnimation(
                                        ContextCompat.getColor(LoginActivity.this, R.color.green),
                                BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_name));

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Move to new activity
                                        //launchSearchActivity();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
