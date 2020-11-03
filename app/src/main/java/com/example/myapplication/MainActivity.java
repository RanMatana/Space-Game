package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static int localImages;
    private boolean isMute;
    private long backPressedTime;
    private Toast backToast;
    FirebaseAuth mAuth;
    private View decorView;
    DatabaseReference myRefResultUser, myRefResultScores;
    TextView[] arrTVP, arrTVS;
    TextView txtOnMOde;
    ImageView photoCurrent, player1;
    private List<User> listUsersScore;
    final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
    CircleImageView profileImageCurrent;
    Bitmap imageBitmap;
    FirebaseUser userOnMode;
    TextView highScoreTxt;
    ImageView[] arrImg;

    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog.startLoadingDialog();

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    decorView.setSystemUiVisibility(hideSystemBars());
                }
            }
        });

        player1 = findViewById(R.id.profile_image_current_1);
        photoCurrent = findViewById(R.id.photoCurrent);

        arrImg = new ImageView[5];
        arrImg[0] = findViewById(R.id.profile_image_current_1);
        arrImg[1] = findViewById(R.id.profile_image_current_2);
        arrImg[2] = findViewById(R.id.profile_image_current_3);
        arrImg[3] = findViewById(R.id.profile_image_current_4);
        arrImg[4] = findViewById(R.id.profile_image_current_5);

        arrTVP = new TextView[5];
        arrTVP[0] = findViewById(R.id.playerNO1);
        arrTVP[1] = findViewById(R.id.playerNO2);
        arrTVP[2] = findViewById(R.id.playerNO3);
        arrTVP[3] = findViewById(R.id.playerNO4);
        arrTVP[4] = findViewById(R.id.playerNO5);

        arrTVS = new TextView[5];
        arrTVS[0] = findViewById(R.id.highScoreNO1);
        arrTVS[1] = findViewById(R.id.highScoreNO2);
        arrTVS[2] = findViewById(R.id.highScoreNO3);
        arrTVS[3] = findViewById(R.id.highScoreNO4);
        arrTVS[4] = findViewById(R.id.highScoreNO5);

        highScoreTxt = findViewById(R.id.highScoreTxt);
        txtOnMOde = findViewById(R.id.txtOnMode);
        profileImageCurrent = findViewById(R.id.profile_image_current);

        mAuth = FirebaseAuth.getInstance();
        userOnMode = mAuth.getCurrentUser();
        updateTableScore();
        updateDetailsUser();
        try {
            updatePhotoCurrentUser();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Navigation
        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadingDialog();
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });
        findViewById(R.id.btnOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        final SharedPreferences preferences = getSharedPreferences("game", MODE_PRIVATE);
//        highScoreTxt.setText("HighScore " + preferences.getInt("highScore", 0));

        // Control Mute
        isMute = preferences.getBoolean("isMute", false);
        final ImageView volumeCtrl = findViewById(R.id.volumeCtrl);
        if (isMute)
            volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp);
        else
            volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp);

        // If the player press the button
        volumeCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMute = !isMute;

                if (isMute)
                    volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp);
                else
                    volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isMute", isMute);
                editor.apply();
            }
        });
    }

    private void updatePhotoCurrentUser() throws IOException {
        final File localFile = File.createTempFile("images", "png");
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("images/" + userOnMode.getUid() + ".png");
        ref.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        imageBitmap = BitmapFactory.decodeFile(localFile.getPath());
                        profileImageCurrent.setImageBitmap(imageBitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void updateDetailsUser() {
        // Read from the database
        myRefResultUser = FirebaseDatabase.getInstance().getReference("Users");
        myRefResultUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User tempUser = dataSnapshot.child(userOnMode.getUid()).getValue(User.class);
                if (tempUser != null) {
                    highScoreTxt.setText("High Score " + tempUser.getScore());
                    txtOnMOde.setText(userOnMode.getDisplayName());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void updateTableScore() {
        // table score
        myRefResultScores = FirebaseDatabase.getInstance().getReference("Score");
        myRefResultScores.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUsersScore = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    listUsersScore.add(dataSnapshot.child("" + (i + 1)).getValue(User.class));
                }
                Collections.sort(listUsersScore);
                int i = 0;
                localImages = 0;
                for (User user : listUsersScore) {
                    arrTVP[i].setText("" + user.getUserName());
                    arrTVS[i++].setText("" + user.getScore());
                    String url = user.getUri_image();
                    LoadImage loadImage = new LoadImage(user.getUri_image());
                    loadImage.execute(url);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        userOnMode = mAuth.getCurrentUser();
    }

    // If I press the "Back" button on the phone once,
    // I will have an instance of a Toast object
    // that will ask me "Am I sure?"
    // If I press the button twice,
    // it will exit the game. Otherwise it will continue
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return;
        } else {
            backToast = Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_LONG);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int hideSystemBars() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {

        String path;

        public LoadImage(String p) {
            this.path = p;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlLink = strings[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new URL(urlLink).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (localImages == 0) {
                arrImg[localImages++].setImageBitmap(bitmap);
            } else if (localImages == 1) {
                arrImg[localImages++].setImageBitmap(bitmap);
            } else if (localImages == 2) {
                arrImg[localImages++].setImageBitmap(bitmap);
            } else if (localImages == 3) {
                arrImg[localImages++].setImageBitmap(bitmap);
            } else {
                arrImg[localImages].setImageBitmap(bitmap);
                loadingDialog.dismissDialog();
            }
        }
    }
}
