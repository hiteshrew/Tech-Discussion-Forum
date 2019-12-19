package com.hitesh.techforum;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {



    private ProgressBar loginProgress;
    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;

   private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        //Email text
        mAuth = FirebaseAuth.getInstance();

        loginProgress = (ProgressBar) findViewById(R.id.reg_progress);

        loginEmailText = (EditText) findViewById(R.id.reg_email);

        loginPassText = (EditText) findViewById(R.id.reg_pass);

        loginBtn = (Button) findViewById(R.id.reg_btn);

        loginRegBtn = (Button) findViewById(R.id.reg_login_btn);
        loginProgress.setVisibility(View.INVISIBLE);

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regIntent = new Intent(LoginActivity.this,register_activity.class);
                startActivity(regIntent);
            }
        });



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String loginEmail = loginEmailText.getText().toString();
                String loginPassword = loginPassText.getText().toString();

               // System.out.println(loginEmial);

               if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword))
               {
                  loginProgress.setVisibility(View.VISIBLE);
                  System.out.println(loginEmail);


                  mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {

                          if(task.isSuccessful())
                          {

                             /* Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                              startActivity(mainIntent);
                              finish();

                              */
                             sendToMain();

                          }

                          else
                          {
                              String errorMessage = task.getException().getMessage();
                              Toast.makeText(LoginActivity.this,"Error : "+ errorMessage,Toast.LENGTH_LONG).show();
                          }


                          loginProgress.setVisibility(View.INVISIBLE);


                      }
                  });


               }


            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
                /*Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();
                            */
                sendToMain();

        }

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
