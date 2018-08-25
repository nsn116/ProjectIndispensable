package com.projectindispensable.projectindispensable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

  private static final String TAG = "EmailPassword";
  private ProgressDialog mProgressDialog;

  // UI references.
  private EditText mEmailView;
  private EditText mFirstNameView;
  private EditText mLastNameView;
  private EditText mPasswordView;
  private Button mRegisterButton;
  private Button mLoginButton;
  private View mProgressView;
  private View mLoginFormView;

  private FirebaseAuth mAuth;

  private DatabaseReference mUserDatabase;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    // Set up the login form.
    mEmailView = (EditText) findViewById(R.id.email);
    mFirstNameView = (EditText) findViewById(R.id.first_name);
    mLastNameView = (EditText) findViewById(R.id.last_name);
    mPasswordView = (EditText) findViewById(R.id.password);
    mRegisterButton = (Button) findViewById(R.id.email_register_button);
    mLoginButton = (Button) findViewById(R.id.email_sign_in_button);

    mAuth = FirebaseAuth.getInstance();
    mUserDatabase = FirebaseDatabase.getInstance().getReference();

    mRegisterButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (TextUtils.isEmpty(email)) {
          Toast.makeText(getApplicationContext(),
              "Please fill in the required fields", Toast.LENGTH_SHORT).show();
          return;
        }
        if (TextUtils.isEmpty(password)) {
          Toast.makeText(getApplicationContext(),
              "Please fill in the required fields", Toast.LENGTH_SHORT).show();
        }

        if (password.length() < 6) {
          Toast.makeText(getApplicationContext(),
              "Password must be at least 6 characters", Toast.LENGTH_SHORT)
              .show();
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  Log.d(TAG, "Successfully registered");

                  User user = new User(mFirstNameView.getText().toString(),
                          mLastNameView.getText().toString(), email, "",
                      null);
                  mUserDatabase.child("users")
                          .child(mAuth.getCurrentUser().getUid())
                          .setValue(user);
                  String uid = mAuth.getCurrentUser().getUid();
                  // Important: Firebase keys cannot contain '.', and must be replaced with ','
                  DatabaseReference emails = mUserDatabase.child("emails").child(email.replace(".", ","));
                  emails.setValue(uid);
                  Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                  startActivity(intent);
                  finish();
                } else {
                  Toast.makeText(getApplicationContext(),
                      "E-mail or password is wrong", Toast.LENGTH_SHORT).show();
                }
              }
            });
      }
    });

    mLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
      }
    });

    if (mAuth.getCurrentUser() != null) {
      Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
      startActivity(intent);
    }


  }

}


