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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.theyestech.yestechvideomeeting.MainActivity;
import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;


public class SignInActivity extends AppCompatActivity {

    private View view;
    private Context context;

    private EditText et_Email, et_Password;
    private MaterialButton btn_SignIn;

    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        context = this;

        preferenceManager = new PreferenceManager(context);

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI() {
        findViewById(R.id.tv_SignUp).setOnClickListener(v -> {
            Intent intent = new Intent(context, SignUpActivity.class);
            startActivity(intent);
        });
        et_Email = findViewById(R.id.et_Email);
        et_Password = findViewById(R.id.et_Password);
        btn_SignIn = findViewById(R.id.btn_SignIn);
        progressBar = findViewById(R.id.progressBar);
        btn_SignIn.setOnClickListener(v -> {
            if(et_Email.getText().toString().trim().isEmpty()){
                Toast.makeText(context, "Enter email", Toast.LENGTH_LONG).show();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(et_Email.getText().toString()).matches()){
                Toast.makeText(context, "Enter valid email", Toast.LENGTH_LONG).show();
            }else if(et_Password.getText().toString().trim().isEmpty()){
                Toast.makeText(context, "Enter password", Toast.LENGTH_LONG).show();
            }else{
                signIn();
            }
        });
    }
    private void signIn(){
        btn_SignIn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, et_Email.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, et_Password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocumentChanges().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        btn_SignIn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(context, "Unable to sign in", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
