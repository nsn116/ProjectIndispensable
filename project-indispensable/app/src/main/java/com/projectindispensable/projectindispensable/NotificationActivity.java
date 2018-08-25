package com.projectindispensable.projectindispensable;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.r0adkll.slidr.Slidr;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

  private static final int REQUEST_CAMERA = 3;
  private static final int SELECT_FILE = 2;
  private FirebaseAuth mAuth;
  private DatabaseReference mUserDatabase;
  private DatabaseReference mMedDatabase;
  private StorageReference mStorageRef;
  private FloatingActionButton mDeleteButton;
  private FloatingActionButton mCameraButton;
  private ImageView mMedicationImage;
  private int numTimes;
  private List<String> allTimes;
  Uri imageHoldUri = null;
  private Switch switch1;
  private Calendar calendar;
  private boolean isReminderSet;
  private TextView startTime;

  private String medicineName;
  private String dosage;
  private String startDate;
  private String numDays;
  private String notes;
  private String key;
  private String groupId;
  private int reqID;
  private String userId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notification);
    Slidr.attach(this);

    mStorageRef = FirebaseStorage.getInstance().getReference();
    mMedDatabase = FirebaseDatabase.getInstance().getReference
        ("medication_multi/" +
            getIntent().getStringExtra("key"));

    medicineName = getIntent().getStringExtra("medicineName");
    dosage = getIntent().getStringExtra("dosage");
    startDate = getIntent().getStringExtra("startDate");
    numDays = getIntent().getStringExtra("numDays");
    notes = getIntent().getStringExtra("notes");
    allTimes = getIntent().getStringArrayListExtra("allTimes");
    key = getIntent().getStringExtra("key");
    groupId = getIntent().getStringExtra("groupId");
    userId = getIntent().getStringExtra("userId");
    isReminderSet = getIntent().getBooleanExtra("isReminderSet", true);


    switch1 = (Switch) findViewById(R.id.toggle_switch);
//    if (allTimes.get(0).equals("")) {
//      isReminderSet = false;
//    }

    if (!isReminderSet) {
      switch1.setChecked(false);
    } else {
      switch1.setChecked(true);
    }

//    if (isSetToFalse) {
//      switch1.setChecked(false);
//    } else {
//      switch1.setChecked(true);
//    }

    switch1.setOnCheckedChangeListener(this);

    numTimes = getIntent().getIntExtra("numTimes", 0);

    mMedicationImage = (ImageView) findViewById(R.id.medication_photo);

    mMedDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String medicationPicUri = dataSnapshot.child("medicationPic")
            .getValue(String.class);
        Glide.with(NotificationActivity.this).load(medicationPicUri)
            .apply(new RequestOptions().centerCrop()
                .placeholder(R.drawable.medication_default))
            .into(mMedicationImage);

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    calendar = Calendar.getInstance();
    mCameraButton = (FloatingActionButton) findViewById(R.id.camera_button);
    mCameraButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        medicationPicSelection();
      }
    });

    mDeleteButton = (FloatingActionButton) findViewById(R.id.delete_medication);
    mDeleteButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        deleteFromDatabase();
      }
    });

    TextView medName = (TextView) findViewById(R.id.med_name);
    medName.setText(medicineName);

    final TextView user = (TextView) findViewById(R.id.person_name);
    mAuth = FirebaseAuth.getInstance();
    final String myUser = mAuth.getCurrentUser().getUid();
    if (myUser.equals(userId)) {
      mUserDatabase = FirebaseDatabase.getInstance().getReference
              ("users/" + myUser);

      mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          String displayName = dataSnapshot.child("firstName")
                  .getValue(String.class);
          user.setText(displayName);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    } else {
      DatabaseReference mGroupDatabase = FirebaseDatabase.getInstance().getReference("groups/" + groupId + "/members/" + userId);
      mGroupDatabase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          String displayName = dataSnapshot.child("nameInGroup")
                  .getValue(String.class);
          user.setText(displayName);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });

    }

    TextView dos = (TextView) findViewById(R.id.dos);
    String text = dosage + "mg";

    dos.setText(text);

    TextView date = (TextView) findViewById(R.id.start_date_notif);
    String text2 = startDate;
    date.setText(text2);

    TextView days = (TextView) findViewById(R.id.num_days_notif);
    String text3 = numDays;
    days.setText(text3);

    startTime = (TextView) findViewById(R.id.time_notif);

    if (allTimes == null) {
      System.out.println("It is null");
    }

    displayAllTimes();

    TextView mNotes = (TextView) findViewById(R.id.notes_notif);
    String text4 = notes;
    mNotes.setText(text4);

    reqID = getIntent().getIntExtra("reqID", 0);
    }

  private void saveMedicationPic() {
    if (imageHoldUri != null) {

      StorageReference mChildStorage = mStorageRef.child("medication")
          .child(imageHoldUri.getLastPathSegment());

      mChildStorage.putFile(imageHoldUri).addOnSuccessListener(
          new OnSuccessListener<TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

              final Uri imageUrl = taskSnapshot.getDownloadUrl();
              mMedDatabase.child("medicationPic").setValue(imageUrl.toString
                  ());
              finish();
              startActivity(getIntent());
            }
          });
    }
  }


  private void medicationPicSelection() {

    //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

    final CharSequence[] items = {"Take a photo", "Choose from gallery",
        "Cancel"};
    AlertDialog.Builder builder = new AlertDialog.Builder(
        NotificationActivity.this);
    builder.setTitle("Upload a photo of your medication");

    //SET ITEMS AND THERE LISTENERS
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {

        if (items[item].equals("Take a photo")) {
          cameraIntent();
        } else if (items[item].equals("Choose from gallery")) {
          galleryIntent();
        } else if (items[item].equals("Cancel")) {
          dialog.dismiss();
        }
      }
    });
    builder.show();
  }

  private void cameraIntent() {

    //CHOOSE CAMERA
    Log.d("gola", "entered here");
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(intent, REQUEST_CAMERA);
  }

  private void galleryIntent() {

    //CHOOSE IMAGE FROM GALLERY
    Log.d("gola", "entered here");
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    startActivityForResult(intent, SELECT_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
      Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    //SAVE URI FROM GALLERY
    if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
      Uri imageUri = data.getData();

      CropImage.activity(imageUri)
          .setGuidelines(CropImageView.Guidelines.ON)
          .setAspectRatio(1, 1)
          .start(this);

    } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
      //SAVE URI FROM CAMERA

      Uri imageUri = data.getData();

      CropImage.activity(imageUri)
          .setGuidelines(CropImageView.Guidelines.ON)
          .setAspectRatio(1, 1)
          .start(this);

    }

    //image crop library code
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (resultCode == RESULT_OK) {
        imageHoldUri = result.getUri();

        mMedicationImage.setImageURI(imageHoldUri);
      } else if (resultCode
          == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Exception error = result.getError();
      }
    }
    saveMedicationPic();

  }

  public void deleteFromDatabase() {

      AlertDialog.Builder alertDialog = new AlertDialog.Builder(NotificationActivity.this);
      alertDialog.setTitle("Delete");
      alertDialog.setMessage("Are you sure you want to delete this medication?");

      alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

          }
      }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              int pos = getIntent().getIntExtra("pos", 0);
              String key = getIntent().getStringExtra("key");
              Intent intent = new Intent(getApplicationContext(), MedicationActivity.class);
              intent.putExtra("pos", pos);
              intent.putExtra("key", key);
              intent.putExtra("method", 1);

              for (int i = 0; i < numTimes; i++) {
                  Intent notifIntent = new Intent(getApplicationContext(), NotificationReciever.class);
                  reqID += 100;
                  PendingIntent pendingIntent = PendingIntent
                          .getBroadcast(getBaseContext(), reqID, notifIntent,
                                  PendingIntent.FLAG_ONE_SHOT);
                  AlarmManager alarmManagerStop = (AlarmManager) getSystemService(
                          Context.ALARM_SERVICE);
                  if (alarmManagerStop != null) {
                      alarmManagerStop.cancel(pendingIntent);
                  }
              }
              startActivity(intent);
          }
      });
      alertDialog.show();
  }

  public void editFromDatabase(View view) {

    String medicineName = getIntent().getStringExtra("medicineName");
    String dosage = getIntent().getStringExtra("dosage");
    String startDate = getIntent().getStringExtra("startDate");
    String numDays = getIntent().getStringExtra("numDays");
    String key = getIntent().getStringExtra("key");
    String notes = getIntent().getStringExtra("notes");
    String groupId = getIntent().getStringExtra("groupId");
    String userId = getIntent().getStringExtra("userId");

    Intent intent = new Intent(this, EditActivity.class);

    intent.putExtra("medicineName", medicineName);
    intent.putExtra("dosage", dosage);
    intent.putExtra("startDate", startDate);
    intent.putExtra("numDays", numDays);
    intent.putExtra("key", key);
    intent.putExtra("notes", notes);
    intent.putExtra("reqID", reqID);
    intent.putExtra("numTimes", numTimes);
    intent.putStringArrayListExtra("allTimes", (ArrayList<String>) allTimes);
    intent.putExtra("groupId", groupId);
    intent.putExtra("isReminderSet",isReminderSet);
    intent.putExtra("userId", userId);
    startActivity(intent);

  }

  public void cancelReminders() {

    Medication medication = new Medication(medicineName, Integer.valueOf(dosage), startDate, Integer.valueOf(numDays), userId, key, notes, reqID, numTimes, allTimes,
            null, groupId, isReminderSet);
    FirebaseDatabase.getInstance().getReference("medication_multi/" + key).setValue(medication);

    for (int i = 0; i < numTimes; i++) {
      Intent notifIntent = new Intent(this, NotificationReciever.class);
      reqID += 100;
      PendingIntent pendingIntent = PendingIntent
          .getBroadcast(getBaseContext(), reqID, notifIntent,
              PendingIntent.FLAG_ONE_SHOT);
      AlarmManager alarmManagerStop = (AlarmManager) getSystemService(
          Context.ALARM_SERVICE);
      if (alarmManagerStop != null) {
        alarmManagerStop.cancel(pendingIntent);
      }
      cancelRepeatedReminder(reqID + 10);
    }
  }

  private void cancelRepeatedReminder(int reqID) {
    Intent notifIntent = new Intent(this, NotificationReciever.class);
    PendingIntent pendingIntent = PendingIntent
            .getBroadcast(getBaseContext(), reqID, notifIntent,
                    PendingIntent.FLAG_ONE_SHOT);
    AlarmManager alarmManagerStop = (AlarmManager) getSystemService(
            Context.ALARM_SERVICE);
    if (alarmManagerStop != null) {
      alarmManagerStop.cancel(pendingIntent);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    boolean isEmpty = allTimes.get(0).equals("");
    if (switch1.isChecked()) {
      if (isEmpty) {
          createPopupAndSetRems();
      } else {
          setMultipleReminders();
          isReminderSet = true;
          Medication medication = new Medication(medicineName, Integer.valueOf(dosage), startDate, Integer.valueOf(numDays), userId, key, notes, reqID, numTimes, allTimes,
                null, groupId, isReminderSet);
          FirebaseDatabase.getInstance().getReference("medication_multi/" + key).setValue(medication);

      }
    } else {
        isReminderSet = false;
        cancelReminders();
    }

//    Intent intent = new Intent(this, NotificationActivity.class);
//    startActivity(intent);
  }

  private void createPopupAndSetRems() {

    AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
    builder.setTitle("Alarms");
    builder.setMessage("Enter time to take the medicine");

    final LinearLayout layout =  new LinearLayout(getApplicationContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    for(int i = 0; i < numTimes; i++) {

      final EditText input = new EditText(NotificationActivity.this);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
      input.setLayoutParams(lp);
      input.setHint("Enter time");
      input.setFocusable(false);
      input.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Calendar mcurrentTime = Calendar.getInstance();
          int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
          int minute = mcurrentTime.get(Calendar.MINUTE);
          TimePickerDialog mTimePicker;
          mTimePicker = new TimePickerDialog(NotificationActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

              String text;
              if (selectedMinute < 10 && selectedHour < 10) {
                text = "0" + selectedHour +":" + "0" + selectedMinute;
              } else if (selectedMinute < 10) {
                text = selectedHour + ":0" + selectedMinute;
              } else if (selectedHour < 10) {
                text = "0" + selectedHour + ":" + selectedMinute;
              }
              else {
                text = selectedHour + ":" + selectedMinute;
              }
              input.setText(text);
            }
          }, hour, minute, true);//Yes 24 hour time
          mTimePicker.setTitle("Select Start Time");
          mTimePicker.show();
        }
      });
      layout.addView(input);
    }
    layout.isFocusable();
    layout.isFocusableInTouchMode();
    builder.setView(layout);

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch1.setChecked(false);
      }
    }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        allTimes = new ArrayList<>();
        for (int i = 0; i < numTimes; i++) {
          EditText children = (EditText) layout.getChildAt(i);
          String time = "";
          time = children.getText().toString();
          if (time.equals("")) {
            isReminderSet = false;
          } else {
            isReminderSet = true;
          }
          allTimes.add(time);
        }
        displayAllTimes();
        if (isReminderSet) {
          setMultipleReminders();
        }
        Medication medication = new Medication(medicineName, Integer.valueOf(dosage), startDate, Integer.valueOf(numDays), userId, key, notes, reqID, numTimes, allTimes,
                null, groupId, isReminderSet);
        FirebaseDatabase.getInstance().getReference("medication_multi/" + key).setValue(medication);
      }
    });
    builder.show();
  }

  private void setMultipleReminders() {
    for(String time : allTimes) {
      reqID += 100;
      String[] hourMin = time.split(":");
      int hour = Integer.parseInt(hourMin[0]);
      int min = Integer.parseInt(hourMin[1]);
      setSingleReminder(hour, min, reqID);
      setSecondReminder(hour, min, reqID + 10);
    }
  }

  private void setSecondReminder(int hour, int min, int reqID) {
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, min + 10);
    calendar.set(Calendar.SECOND, 0);

    Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
    intent.putExtra("medicineName", medicineName);
    intent.putExtra("dosage", dosage);
    intent.putExtra("startDate", startDate);
    intent.putExtra("numDays", numDays);
    intent.putExtra("notes", notes);
    intent.putExtra("hour", hour);
    intent.putExtra("min", min);
    intent.putExtra("reqID", reqID);
    intent.putStringArrayListExtra("allTimes", (ArrayList<String>) allTimes);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reqID, intent, PendingIntent.FLAG_ONE_SHOT);

    if(calendar.before(Calendar.getInstance())) {
      calendar.add(Calendar.DATE, 1);
    }

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
  }

  private void setSingleReminder(int hour, int min, int reqID) {
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, min);
    calendar.set(Calendar.SECOND, 0);

    Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
      intent.putExtra("medicineName", medicineName);
      intent.putExtra("dosage", dosage);
      intent.putExtra("startDate", startDate);
      intent.putExtra("numDays", numDays);
      intent.putExtra("notes", notes);
      intent.putExtra("hour", hour);
      intent.putExtra("min", min);
      intent.putExtra("reqID", reqID);
      intent.putStringArrayListExtra("allTimes", (ArrayList<String>) allTimes);


      PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reqID, intent, PendingIntent.FLAG_ONE_SHOT);

    if(calendar.before(Calendar.getInstance())) {
      calendar.add(Calendar.DATE, 1);
    }

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
  }

  private void displayAllTimes() {
    if (!allTimes.isEmpty()) {
      StringBuilder text1 = new StringBuilder();
      for (String time : allTimes) {
        text1.append(time + "\n");
      }
      startTime.setText(text1.toString().trim());
    }
  }
}
