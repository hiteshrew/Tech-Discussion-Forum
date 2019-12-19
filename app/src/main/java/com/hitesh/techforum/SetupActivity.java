package com.hitesh.techforum;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {


    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private String user_id;
    private EditText setupName;
    private Button setupBtn;
    private boolean isChanged = false;
    private ProgressBar setupProgress;
    private StorageReference storageReference;
   private FirebaseAuth firebaseAuth;
   private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Toolbar setupToolbar = (Toolbar) findViewById(R.id.setupToolbar);

        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Profile Setup");

        setupImage =  findViewById(R.id.setup_image);
        setupName =  findViewById(R.id.s_name);
        setupBtn = findViewById(R.id.s_btn);
        setupProgress = findViewById(R.id.setup_progress);
        firebaseAuth =  FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user_id = firebaseAuth.getCurrentUser().getUid();


        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                       String name = task.getResult().getString("name");
                       String image = task.getResult().getString("image");
                       mainImageURI = Uri.parse(image);
                       setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);
                       Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                    }

                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FIRESTORE Retrive Error : " + error,Toast.LENGTH_LONG).show();

                }
                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }

        });


       /* setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(setupName.getText().toString());
            }
        });

*/



        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String user_name = setupName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {
                if(isChanged ==true) {



                        setupProgress.setVisibility(View.VISIBLE);
                        user_id = firebaseAuth.getCurrentUser().getUid();

                        final StorageReference image_path = storageReference.child("Profile_Images").child(user_id + ".jpg");

                 /*
                     image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                               Uri download_uri = task.getResult().getDownloadUrl();
                                 //  Uri download_uri = task.getResult().getStorage().getDownloadUrl().getResult();
                                Toast.makeText(SetupActivity.this,"Image has been Uploaded",Toast.LENGTH_LONG).show();
                                System.out.println(download_uri.toString());

                            }

                            else{

                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this,"Error : " + error,Toast.LENGTH_LONG).show();
                            }

                        }
                    });


*/


                        final Task<Uri> urlTask = image_path.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);


                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error : " + error, Toast.LENGTH_LONG).show();
                                }

                            }
                        });


                    } else{

                    storeFirestore(null,user_name);
                }



                }
            }
        });






        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){


                        Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }

                    else{
                        //Toast.makeText(SetupActivity.this,"You already have Permission",Toast.LENGTH_LONG).show();

                       BringImagePicker();
                    }

                }else{
                    BringImagePicker();
                }


            }
        });

    }

    private void storeFirestore(@NonNull Task<Uri> task,String user_name) {

        Uri downloadUrl;
if(task != null) {
     downloadUrl = task.getResult();

}
else{
        downloadUrl = mainImageURI;
}
        Map<String,String> userMap =  new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",downloadUrl.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(SetupActivity.this,"User setting Updated",Toast.LENGTH_LONG).show();

                    Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                }else{


                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FIRESTORE Error : " + error,Toast.LENGTH_LONG).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();

                setupImage.setImageURI(mainImageURI);
                isChanged = true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }



}
