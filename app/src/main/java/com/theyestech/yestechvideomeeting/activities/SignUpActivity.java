package com.theyestech.yestechvideomeeting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.theyestech.yestechvideomeeting.R;

public class SignUpActivity extends AppCompatActivity {
    private View view;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }
    private void initializeUI(){
        findViewById(R.id.iv_Back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.tv_SignIn).setOnClickListener(v -> onBackPressed());
    }
}
