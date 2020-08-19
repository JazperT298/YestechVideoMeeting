package com.theyestech.yestechvideomeeting.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theyestech.yestechvideomeeting.MainActivity;
import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private EditText et_Firstname, et_Lastname, et_Email, et_Password, et_ConfirmPassword;
    private MaterialButton btn_SignUp;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        context = this;
        preferenceManager = new PreferenceManager(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI() {
        findViewById(R.id.iv_Back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.tv_SignIn).setOnClickListener(v -> onBackPressed());
        et_Firstname = findViewById(R.id.et_Firstname);
        et_Lastname = findViewById(R.id.et_Lastname);
        et_Email = findViewById(R.id.et_Email);
        et_Password = findViewById(R.id.et_Password);
        et_ConfirmPassword = findViewById(R.id.et_ConfirmPassword);
        btn_SignUp = findViewById(R.id.btn_SignUp);
        progressBar = findViewById(R.id.progressBar);
        btn_SignUp.setOnClickListener(v -> {
            if (et_Firstname.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Enter first name", Toast.LENGTH_LONG).show();
            } else if (et_Lastname.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Enter last name", Toast.LENGTH_LONG).show();
            } else if (et_Email.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Enter email", Toast.LENGTH_LONG).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(et_Email.getText().toString()).matches()) {
                Toast.makeText(context, "Enter valid email", Toast.LENGTH_LONG).show();
            } else if (et_Password.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Enter password", Toast.LENGTH_LONG).show();
            } else if (et_ConfirmPassword.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Confirm your password", Toast.LENGTH_LONG).show();
            } else if (!et_Password.getText().toString().equals(et_ConfirmPassword.getText().toString())) {
                Toast.makeText(context, "Password didn't match", Toast.LENGTH_LONG).show();
            } else {
                signUp();
            }
        });

    }

    private void signUp(){
        btn_SignUp.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, et_Firstname.getText().toString());
        user.put(Constants.KEY_LAST_NAME, et_Lastname.getText().toString());
        user.put(Constants.KEY_EMAIL, et_Email.getText().toString());
        user.put(Constants.KEY_PASSWORD, et_Password.getText().toString());

        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, et_Firstname.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME, et_Lastname.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, et_Email.getText().toString());
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    btn_SignUp.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }
}
