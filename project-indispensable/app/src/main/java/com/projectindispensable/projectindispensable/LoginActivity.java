package com.projectindispensable.projectindispensable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

  private static final String TAG = "EmailPassword";
  private ProgressDialog mProgressDialog;
  // UI references.
  private TextView mStatusTextView;
  private EditText mEmailField;
  private EditText mPasswordField;
  private Button mLoginButton;
  private Button mRegisterButton;
  private Button mForgotPWButton;
  private View mProgressView;
  private View mLoginFormView;

  private FirebaseAuth mAuth;
  private Calendar calendar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    // Set up the login form.
    mEmailField = (EditText) findViewById(R.id.email);
    mPasswordField = (EditText) findViewById(R.id.password);
    mLoginButton = (Button) findViewById(R.id.email_sign_in_button);
    mRegisterButton = (Button) findViewById(R.id.email_register_button);
    mForgotPWButton = (Button) findViewById(R.id.email_forgot_pw_button);
    calendar = Calendar.getInstance();

    mAuth = FirebaseAuth.getInstance();

    mLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String email = mEmailField.getText().toString();
        final String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email)) {
          Toast.makeText(getApplicationContext(), "Enter email address",
              Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(password)) {
          Toast.makeText(getApplicationContext(), "Enter password",
              Toast.LENGTH_SHORT).show();
          return;
        }

        //authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(LoginActivity.this,
                new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                      // there was an error
                      Log.d(TAG, "Failed");
                      Toast.makeText(LoginActivity.this, "Authorisation failed",
                          Toast
                              .LENGTH_LONG).show();
                    } else {
                      Log.d(TAG, "Success");
                      Intent intent = new Intent(LoginActivity.this,
                          HomeActivity.class);
                      final Query query = FirebaseDatabase.getInstance().getReference().
                              child("medication_multi").orderByChild("userId").
                              equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                      query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                         for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                           Medication medication = snapshot.getValue(Medication.class);
                           if (medication.getIsReminderSet()) {
                             setMultipleReminders(medication);
                           }
                         }
                         query.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                      });

                      startActivity(intent);
                      finish();
                    }
                  }
                });
      }
    });

    mRegisterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
      }
    });

    if (mAuth.getCurrentUser() != null) {
      Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
      startActivity(intent);
    }

  }

  private void setMultipleReminders(Medication medication) {

    int reqID = medication.getReqID();
    for(String time : medication.getAllTimes()) {
      reqID += 100;
      String[] hourMin = time.split(":");
      int hour = Integer.parseInt(hourMin[0]);
      int min = Integer.parseInt(hourMin[1]);
      setSingleReminder(medication, hour, min, reqID);
      setSecondReminder(medication, hour, min, reqID + 10);
    }
  }

  private void setSecondReminder(Medication medication, int hour, int min, int reqID) {
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, min + 10);
    calendar.set(Calendar.SECOND, 0);

    Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
    //TODO: Consider addding name of user as well as time at which medicine has to be taken
    intent.putExtra("medicineName", medication.getMedicationName());
    String dosage = String.valueOf(medication.getDosage());
    intent.putExtra("dosage", dosage);
    intent.putExtra("startDate", medication.getStartDate());
    intent.putExtra("numDays", String.valueOf(medication.getNumDays()));
    intent.putExtra("notes", medication.getNotes());
    intent.putExtra("hour", hour);
    intent.putExtra("min", min);
    intent.putExtra("reqID", reqID);
    intent.putStringArrayListExtra("allTimes", (ArrayList<String>) medication.getAllTimes());
    intent.putExtra("isReminderSet", medication.getIsReminderSet());

    PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reqID, intent, PendingIntent.FLAG_ONE_SHOT);

    if(calendar.before(Calendar.getInstance())) {
      calendar.add(Calendar.DATE, 1);
    }

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
  }

  private void setSingleReminder(Medication medication, int hour, int min, int reqID) {
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, min);
    calendar.set(Calendar.SECOND, 0);

    Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
    //TODO: Consider addding name of user as well as time at which medicine has to be taken
    intent.putExtra("medicineName", medication.getMedicationName());
    String dosage = String.valueOf(medication.getDosage());
    intent.putExtra("dosage", dosage);
    intent.putExtra("startDate", medication.getStartDate());
    intent.putExtra("numDays", String.valueOf(medication.getNumDays()));
    intent.putExtra("notes", medication.getNotes());
    intent.putExtra("hour", hour);
    intent.putExtra("min", min);
    intent.putExtra("reqID", reqID);
    intent.putStringArrayListExtra("allTimes", (ArrayList<String>) medication.getAllTimes());
    intent.putExtra("isReminderSet", medication.getIsReminderSet());

    PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reqID, intent, PendingIntent.FLAG_ONE_SHOT);

    if(calendar.before(Calendar.getInstance())) {
      calendar.add(Calendar.DATE, 1);
    }

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
  }
}

