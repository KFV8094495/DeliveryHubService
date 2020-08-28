package com.uk.ac.teesdie.dhs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uk.ac.teesdie.dhs.Utils.UserUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 7171 ;
    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private Uri imageUri;
    private ImageView img_avatar;



    //Image Upload
    private AlertDialog waitingDialog;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_bar);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        init();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imageUri = data.getData();
                img_avatar.setImageURI(imageUri);

                showDialogUpload();

            }
        }
    }

    private void showDialogUpload() {

        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
        builder.setTitle("Change Avatar")
                .setMessage("Are you Sure ,You Want to Change your Avatar ?")
                .setNegativeButton("CANCELL", (dialog, i) -> dialog.dismiss())

                .setPositiveButton("UPLOAD", (dialog, which) -> {

                    if (imageUri != null)
                    {
                        waitingDialog.setMessage("Uploading...");
                        waitingDialog.show();

                        String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        StorageReference avatarFolder = storageReference.child("avatas/"+unique_name);

                        avatarFolder.putFile(imageUri)
                                .addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    Snackbar.make(drawer,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                            {
                                avatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Map<String,Object> updateData = new HashMap<>();
                                    updateData.put("avatar",uri.toString());

                                    UserUtils.updateUser(drawer,updateData);
                                });
                            }

                            waitingDialog.dismiss();
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            waitingDialog.setMessage(new StringBuilder("Uploading:").append(progress).append("%"));

                        });
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));

        });

        dialog.show();

    }

    private void init() {


        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waiting")
                .create();

        storageReference = FirebaseStorage.getInstance().getReference();


        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId()== R.id.sign_out){

                AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Are you Sure ,You Want To Sign Out?")
                        .setNegativeButton("Cancell", (dialog, i) -> dialog.dismiss())

                        .setPositiveButton("SIGN OUT ", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(DriverHomeActivity.this, DriverLogin.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false);

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialog1 -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.colorAccent));

                });

                dialog.show();
            }
            return true;
        });


        //set Data for User
        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = (TextView)headerView.findViewById(R.id.txt_name);
        TextView txt_email = (TextView)headerView.findViewById(R.id.txt_email);
        TextView txt_star = (TextView)headerView.findViewById(R.id.txt_star);
        img_avatar = (ImageView)headerView.findViewById(R.id.img_avatar);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_email.setText(Common.currentUser != null ? Common.currentUser.getEmailAddress():"");
        txt_star.setText(Common.currentUser != null ? String.valueOf(Common.currentUser.getRating()):"0.0");

        img_avatar.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
        });
        if (Common.currentUser != null && Common.currentUser.getAvatar() != null &&
                !TextUtils.isEmpty(Common.currentUser.getAvatar()))
        {
            Glide.with(this)
                    .load(Common.currentUser.getAvatar())
                    .into(img_avatar);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}