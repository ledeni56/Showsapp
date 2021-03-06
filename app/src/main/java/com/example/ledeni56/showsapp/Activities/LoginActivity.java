package com.example.ledeni56.showsapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.ledeni56.showsapp.Entities.Episode;
import com.example.ledeni56.showsapp.Networking.ApiEpisode;
import com.example.ledeni56.showsapp.Entities.Show;
import com.example.ledeni56.showsapp.Networking.ApiShowDescription;
import com.example.ledeni56.showsapp.Networking.ApiShowId;
import com.example.ledeni56.showsapp.Networking.ResponseData;
import com.example.ledeni56.showsapp.Static.ApplicationShows;
import com.example.ledeni56.showsapp.Static.InputValidations;
import com.example.ledeni56.showsapp.Networking.ApiServiceFactory;
import com.example.ledeni56.showsapp.Networking.ResponseLogin;
import com.example.ledeni56.showsapp.R;
import com.example.ledeni56.showsapp.Networking.UserLogin;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BasicActivity {
    private static final int RESULT_CODE_EMAIL = 1;

    public static final String SENDING_TOKEN_KEY = "user token SENDING";
    public static final java.lang.String REMEMBER_KEY = "remember key";

    private TextInputLayout emailWrapper;
    private TextInputLayout passwordWrapper;
    private Button loginButton;
    private View createAnAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        if (sharedPreferences.getString(MainActivity.TOKEN_KEY, null) != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        setContentView(R.layout.activity_login);

        emailWrapper = findViewById(R.id.emailWrapper);
        passwordWrapper = findViewById(R.id.passwordWrapper);
        loginButton = findViewById(R.id.loginButton);
        createAnAccount = findViewById(R.id.createAnAccount);

        createAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(i, RESULT_CODE_EMAIL);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                String email = emailWrapper.getEditText().getText().toString();
                final String password = passwordWrapper.getEditText().getText().toString();
                if (!InputValidations.validateEmail(email)) {
                    emailWrapper.setError("Not a valid email address!");
                } else {
                    emailWrapper.setErrorEnabled(false);
                }
                if (!InputValidations.validatePassword(password)) {
                    passwordWrapper.setError("Password must have at least 5 characters!");
                } else {
                    passwordWrapper.setErrorEnabled(false);
                }
                if (InputValidations.validateEmail(email) && InputValidations.validatePassword(password)) {
                    showProgress();

                    ApiServiceFactory.get().login(new UserLogin(email, password)).enqueue(new Callback<ResponseLogin>() {
                        @Override
                        public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                            hideProgress();
                            if (response.isSuccessful()) {
                                startMain(response.body().getData().getToken());
                            } else {
                                showError("Have you created an account?\n\nYou can create an account bellow.");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseLogin> call, Throwable t) {
                            hideProgress();
                            showError("Have you created an account?\n\nYou can create an account bellow.");
                        }
                    });
                }
            }
        });
    }


    private void startMain(String token) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(SENDING_TOKEN_KEY, token);
        i.putExtra(REMEMBER_KEY, ((CheckBox) findViewById(R.id.checkBox)).isChecked());
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_EMAIL && resultCode == Activity.RESULT_OK) {
            emailWrapper.getEditText().setText(data.getExtras().getString(RegisterActivity.EMAIL_STRING));
            passwordWrapper.getEditText().setText("");
        }
    }


}
