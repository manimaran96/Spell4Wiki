package com.manimarank.spell4wiki.activities;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.auth.AccountUtils;
import com.manimarank.spell4wiki.models.WikiLogin;
import com.manimarank.spell4wiki.models.WikiToken;
import com.manimarank.spell4wiki.models.WikiUser;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;

import org.jetbrains.annotations.NotNull;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    // Views
    EditText editUserName, editPassword;
    CircularProgressButton btnLogin;
    TextView btnSkipLogin, btnJoinWikipedia, btnForgotPassword;

    PrefManager pref;
    ApiInterface api;

    private boolean isDuringLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new PrefManager(getApplicationContext());

        /*
         * Check Already login or not
         * If yes - Open Main screen
         * Else - Ask to login
         */
        if (pref.isLoggedIn() || pref.getIsAnonymous()) {
            launchActivity();
        } else {
            init();
            GeneralUtils.hideKeyboard(LoginActivity.this);
            api = ApiClient.getCommonsApi(getApplicationContext()).create(ApiInterface.class);

            /*
             * Hit Login Button
             */
            btnLogin.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(editUserName.getText()) && !TextUtils.isEmpty(editPassword.getText())) {
                    if(NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                        GeneralUtils.hideKeyboard(LoginActivity.this);
                        btnLogin.startAnimation();
                        isDuringLogin = true;
                        callToken(editUserName.getText().toString(), editPassword.getText().toString());
                    }else
                        showMsg(getString(R.string.check_internet));
                } else
                    showMsg(getString(R.string.invalid_credential));
            });

            /*
             *  Hit Skip Button
             */
            btnSkipLogin.setOnClickListener(v -> {
                if (isDuringLogin()) {
                    showMsg(getString(R.string.please_wait));
                } else {
                    pref.setIsAnonymous(true);
                    launchActivity();
                }
            });

            /*
             *  Hit Forgot Password Button
             */
            btnForgotPassword.setOnClickListener(v -> {
                openUrl(Urls.FORGOT_PASSWORD, getString(R.string.forgot_password));
            });

            /*
             *  Hit Join Wikipedia Button
             */
            btnJoinWikipedia.setOnClickListener(v -> {
                openUrl(Urls.JOIN_WIKI, getString(R.string.join_wiki));
            });


        }
    }

    /**
     * Getting Token from wiki server before login
     *
     * @param username - username of the user
     * @param password - password of the user
     */
    private void callToken(final String username, final String password) {
        Call<WikiToken> call = api.getLoginToken();
        call.enqueue(new Callback<WikiToken>() {
            @Override
            public void onResponse(@NonNull Call<WikiToken> call, @NonNull Response<WikiToken> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String lgToken = response.body().getQuery().getTokenValue().getLoginToken();
                        /*
                         * Once getting login token then call client login api
                         */
                        completeLogin(username, password, lgToken);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorMsg(getString(R.string.something_went_wrong));
                    }
                } else {
                    showErrorMsg(getString(R.string.something_went_wrong_try_again));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WikiToken> call, @NonNull Throwable t) {
                t.printStackTrace();
                showErrorMsg(getString(R.string.check_internet));
            }
        });
    }

    /**
     * Call client login api after getting login token
     *
     * @param username   - username of the user
     * @param password   - password of the user
     * @param loginToken - Login token
     */
    private void completeLogin(String username, String password, String loginToken) {

        Call<WikiLogin> call = api.clientLogin(username, password, loginToken);
        call.enqueue(new Callback<WikiLogin>() {
            @Override
            public void onResponse(@NonNull Call<WikiLogin> call, @NonNull Response<WikiLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        WikiLogin.ClientLogin login = response.body().getClientLogin();
                        if (login != null && login.getStatus() != null) {
                            switch (login.getStatus()) {
                                case AppConstants.PASS:
                                    Bundle extras = getIntent().getExtras();
                                    AccountAuthenticatorResponse accountAuthenticatorResponse = extras == null ? null : extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
                                    WikiUser wikiUser = new WikiUser(username, password);
                                    AccountUtils.updateAccount(accountAuthenticatorResponse, wikiUser);

                                    showMsg(String.format(getString(R.string.welcome_user), login.getUsername()));
                                    //  Write to shared preferences
                                    pref.setUserSession(login.getUsername());
                                    btnLogin.doneLoadingAnimation(
                                            ContextCompat.getColor(LoginActivity.this, R.color.w_green),
                                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_done));

                                    new Handler().postDelayed(() -> {
                                        // Move to new activity
                                        launchActivity();
                                    }, 1500);
                                    break;
                                case AppConstants.FAIL:
                                    showErrorMsg(login.getMessage());
                                    break;
                                case AppConstants.TWO_FACTOR:
                                    showErrorMsg(getString(R.string.two_factor_login) + (login.getMessage() != null ? "\n" + login.getMessage() : ""));
                                default:
                                    showErrorMsg(getString(R.string.server_misbehaved));
                                    break;
                            }
                        } else
                            showErrorMsg(getString(R.string.something_went_wrong));
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorMsg(getString(R.string.something_went_wrong));
                    }
                } else
                    showErrorMsg(getString(R.string.something_went_wrong));
            }

            @Override
            public void onFailure(@NotNull Call<WikiLogin> call, @NotNull Throwable t) {
                showErrorMsg("Please check your connection!");
            }
        });

    }

    /**
     * Launch activity
     */
    private void launchActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showErrorMsg(String msg) {
        showMsg(msg);
        btnLogin.revertAnimation();
        isDuringLogin = false;
    }

    private void showMsg(String msg) {
        SnackBarUtils.INSTANCE.showLong(btnLogin, msg);
    }

    private void init() {
        editUserName = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSkipLogin = findViewById(R.id.btn_skip_login);
        btnJoinWikipedia = findViewById(R.id.btn_join_wikipedia);
        btnForgotPassword = findViewById(R.id.btn_forgot_password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btnLogin != null)
            btnLogin.dispose();
    }

    private void openUrl(String url, String title) {
        if (isDuringLogin()) {
            showMsg(getString(R.string.please_wait));
        } else {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
                GeneralUtils.openUrl(LoginActivity.this, url, title);
            else
                showMsg(getString(R.string.check_internet));
        }
    }

    private boolean isDuringLogin() {
        return btnLogin.isAnimating() || isDuringLogin;
    }
}
