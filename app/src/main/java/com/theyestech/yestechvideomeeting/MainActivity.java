package com.theyestech.yestechvideomeeting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.theyestech.yestechvideomeeting.activities.OutgoingInvitationActivity;
import com.theyestech.yestechvideomeeting.activities.SignInActivity;
import com.theyestech.yestechvideomeeting.adapters.UsersAdapter;
import com.theyestech.yestechvideomeeting.listeners.UsersListener;
import com.theyestech.yestechvideomeeting.models.Users;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private View view;
    private Context context;

    private PreferenceManager preferenceManager;
    private TextView tv_Title, tv_Logout, tv_ErrorMessage;

    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private RecyclerView rv_Users;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView iv_Conference;

    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    //private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        preferenceManager = new PreferenceManager(context);
        initializeUI();
        checkFOrBatteryOptimizations();
    }

    private void initializeUI() {
        tv_Title = findViewById(R.id.tv_Title);
        tv_Title.setText(String.format("%s %s", preferenceManager.getString(Constants.KEY_FIRST_NAME), preferenceManager.getString(Constants.KEY_LAST_NAME)));
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMTokenToDatabase(task.getResult().getToken());
            }
        });
        findViewById(R.id.tv_Logout).setOnClickListener(v -> logout());
        tv_ErrorMessage = findViewById(R.id.tv_ErrorMessage);
        //progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rv_Users = findViewById(R.id.rv_Users);
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, this);
        rv_Users.setAdapter(usersAdapter);
        swipeRefreshLayout.setOnRefreshListener(this::getAllUsers);

        iv_Conference = findViewById(R.id.iv_Conference);

        getAllUsers();
    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> Toast.makeText(context, "Unable to send Token : " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void logout() {
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

    private void getAllUsers() {
        //progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    //progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    String userId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        usersList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (userId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            Users users = new Users();
                            users.firstname = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            users.lastname = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            users.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            users.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            usersList.add(users);
                        }
                        if (usersList.size() > 0) {
                            usersAdapter.notifyDataSetChanged();
                        } else {
                            tv_ErrorMessage.setText(String.format("%s", "No users available"));
                            tv_ErrorMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tv_ErrorMessage.setText(String.format("%s", "No users available"));
                        tv_ErrorMessage.setVisibility(View.VISIBLE);
                    }
                });

    }

    @Override
    public void initiateVideoMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(this, users.firstname + " " + users.lastname + "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(this, users.firstname + " " + users.lastname + "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected) {
            iv_Conference.setVisibility(View.VISIBLE);
            iv_Conference.setOnClickListener(v -> {
                Intent intent = new Intent(context, OutgoingInvitationActivity.class);
                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra("type", "video");
                intent.putExtra("isMultiple", true);
                startActivity(intent);
            });
        } else {
            iv_Conference.setVisibility(View.GONE);
        }
    }

    private void checkFOrBatteryOptimizations(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled, It can interrupt running background services.");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkFOrBatteryOptimizations();
        }
    }
}
