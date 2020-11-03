package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    EditText userEmail, userName, userPass;
    Button btnSign;
    private View decorView;
    FirebaseDatabase database;
    DatabaseReference myRef;
    boolean checkUsersName;
    final LoadingDialog loadingDialog = new LoadingDialog(SignActivity.this);
    private CircleImageView profileImage;
    private static final int PICK_IMAGE = 1;
    Uri imageUri;
    Bitmap imageBitmap;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseUser user;
    ImageView btnBack;
    User newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign);
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
        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        btnBack = findViewById(R.id.btnBack);
        profileImage = findViewById(R.id.profile_image);
        userEmail = findViewById(R.id.signUserEmail);
        userName = findViewById(R.id.signUserName);
        userPass = findViewById(R.id.signUserPass);
        btnSign = findViewById(R.id.btnSignPage);
        database = FirebaseDatabase.getInstance();
        imageBitmap = null;

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                if (checkPermission()) {
                    Intent gallery = new Intent();
                    gallery.setType("image/*");
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
                } else {
                    requestPermission();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignActivity.this, LoginActivity.class));
            }
        });

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                if ((!userName.getText().toString().equals("")) && (!(userEmail.getText().toString().equals(""))) && (!(userPass.getText().toString().equals(""))) && imageUri != null && userPass.getText().length() >= 6) {
                    checkUsersName = false;
                    myRef = database.getReference("Users");

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String tempUserName = ds.child("userName").getValue(String.class);
                                assert tempUserName != null;
                                if (tempUserName.equals(userName.getText().toString())) {
                                    checkUsersName = true;
                                    break;
                                }
                            }
                            if (!checkUsersName) {
                                mAuth.createUserWithEmailAndPassword(userEmail.getText().toString(), btnMD5())
                                        .addOnCompleteListener(SignActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    uploadImageAndSaveUri();
                                                    updateDisplayAndUrl();
                                                    updateUI(user);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    updateUI(null);
                                                }
                                            }
                                        });
                            } else {
                                loadingDialog.dismissDialog();
                                userName.setText("User Name Exist !");
                                userName.setBackgroundColor(Color.parseColor("#FD1D1D"));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), "Not all fields are full", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(SignActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SignActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        } else {
            ActivityCompat.requestPermissions(SignActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                }
                break;
        }
    }

    private void updateDisplayAndUrl() {
        user = mAuth.getCurrentUser();
        assert user != null;
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName.getText().toString())
                .setPhotoUri(imageUri)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void uploadImageAndSaveUri() {
        if (imageUri != null) {
            final StorageReference ref = storageReference.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".png");
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    newUser = new User(userEmail.getText().toString(), userName.getText().toString(), 0, downloadUri.toString());
                                    user = mAuth.getCurrentUser();
                                    assert user != null;
                                    myRef = database.getReference("Users").child(user.getUid());
                                    myRef.setValue(newUser);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            loadingDialog.dismissDialog();
            startActivity(new Intent(SignActivity.this, MainActivity.class));
        } else {
            Toast.makeText(SignActivity.this, "Email OR Password Invalid !", Toast.LENGTH_SHORT).show();
        }
    }

    public String btnMD5() {
        byte[] md5Input = userPass.getText().toString().getBytes();
        BigInteger md5Data = null;

        try {
            md5Data = new BigInteger(1, md5.encryptMD5(md5Input));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert md5Data != null;
        String md5Str = md5Data.toString(16);
        if (md5Str.length() < 32) {
            md5Str = 0 + md5Str;
        }
        return md5Str;
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
}

