package com.theyestech.yestechvideomeeting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.theyestech.yestechvideomeeting.activities.SignInActivity;
import com.theyestech.yestechvideomeeting.adapters.UsersAdapter;
import com.theyestech.yestechvideomeeting.models.Users;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View view;
    private Context context;

    private PreferenceManager preferenceManager;
    private TextView tv_Title,tv_Logout, tv_ErrorMessage;

    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private RecyclerView rv_Users;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        preferenceManager = new PreferenceManager(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI(){
        tv_Title = findViewById(R.id.tv_Title);
        tv_Title.setText(String.format("%s %s", preferenceManager.getString(Constants.KEY_FIRST_NAME),preferenceManager.getString(Constants.KEY_LAST_NAME)));
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                sendFCMTokenToDatabase(task.getResult().getToken());
            }
        });
        findViewById(R.id.tv_Logout).setOnClickListener(v -> logout());
        tv_ErrorMessage = findViewById(R.id.tv_ErrorMessage);
        progressBar = findViewById(R.id.progressBar);
        rv_Users = findViewById(R.id.rv_Users);
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList);
        rv_Users.setAdapter(usersAdapter);

        getAllUsers();
    }

    private void sendFCMTokenToDatabase(String token){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> Toast.makeText(context, "Unable to send Token : " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void logout(){
        Toast.makeText(context, "Signing out...", Toast.LENGTH_LONG).show();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(aVoid -> {
            preferenceManager.clearPreferences();
            startActivity(new Intent(context, SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(context, "Unable to sign out", Toast.LENGTH_LONG).show());
    }

    private void getAllUsers(){
       progressBar.setVisibility(View.VISIBLE);
       FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
       firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
               .get()
               .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    String userId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(userId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            Users users = new Users();
                            users.firstname = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            users.lastname = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            users.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            users.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            usersList.add(users);
                        }
                        if(usersList.size() > 0){
                            usersAdapter.notifyDataSetChanged();
                        }else{
                            tv_ErrorMessage.setText(String.format("%s", "No users available"));
                            tv_ErrorMessage.setVisibility(View.VISIBLE);
                        }
                    }else{
                        tv_ErrorMessage.setText(String.format("%s", "No users available"));
                        tv_ErrorMessage.setVisibility(View.VISIBLE);
                   }
               });

    }
}
