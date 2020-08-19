package com.theyestech.yestechvideomeeting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.theyestech.yestechvideomeeting.R;

public class SignInActivity extends AppCompatActivity {

    private View view;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI(){
        findViewById(R.id.tv_SignUp).setOnClickListener(v -> {
            Intent intent = new Intent(context, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
