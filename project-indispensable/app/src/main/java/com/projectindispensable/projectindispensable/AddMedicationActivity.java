package com.projectindispensable.projectindispensable;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddMedicationActivity extends AppCompatActivity {

    private boolean isReminderSet = true;

    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mMedicationName;
    private EditText mDosage;
    private EditText mStartDate;
    private EditText mNumDays;

    private EditText mNotes;
    private EditText mNumTimes;

    private Button mSaveButton;
    private int numTimes;
    private List<String> allTimes = new ArrayList<>();

    private final String TIME_24_HOURS_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    private final Pattern timePattern = Pattern.compile(TIME_24_HOURS_FORMAT);

    private Calendar calendar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
        = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    intent = new Intent(getApplicationContext(), HomeActivity.class);
                    break;
                case R.id.navigation_scan:
                    intent = new Intent(getApplicationContext(),
                        ScanPrescriptionActivity.class);
                    break;
                default:
                    intent = new Intent(getApplicationContext(), MedicationActivity.class);
                    break;
            }
            startActivity(intent);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        BottomNavigationView navigation = findViewById(R.id.medNavigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_scan).setChecked(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMedicationName = findViewById(R.id.medication_name);
        mDosage = findViewById(R.id.dosage);
        mStartDate = findViewById(R.id.start_date);
        mNumDays = findViewById(R.id.num_days);
        mNotes = findViewById(R.id.notes);
        mNumTimes = findViewById(R.id.num_times);

        mSaveButton = findViewById(R.id.save_medication);

        int method = getIntent().getIntExtra("method", 0);

        if (method == 1) {
            String medName = getIntent().getStringExtra("medName");
            String res = "";
            char ch = medName.charAt(0);
            ch = Character.toUpperCase(ch);
            String rem = medName.substring(1);
            res = ch + rem;
            mMedicationName.setText(res);
            String dosage = getIntent().getStringExtra("dosage");
            if(!dosage.equals("")) {
                mDosage.setText(dosage);
            }
        }

        mStartDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DateDialog dialog = new DateDialog(v);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "Select Start Date");
                }
            }
        });


        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String medicationName = mMedicationName.getText().toString();
                final String dosage = mDosage.getText().toString();
                final String startDate = mStartDate.getText().toString();
                final String numDays = mNumDays.getText().toString();
                if (TextUtils.isEmpty(medicationName)) {
                    mMedicationName.setError(REQUIRED);
                    return;
                }

                if (TextUtils.isEmpty(dosage)) {
                    mDosage.setError(REQUIRED);
                    return;
                }

                if (TextUtils.isEmpty(startDate)) {
                    mStartDate.setError(REQUIRED);
                    return;
                } else if (!isValidDate(startDate)) {
                    mStartDate.setError("INVALID");
                    return;
                }

                if (TextUtils.isEmpty(numDays)) {
                    mNumDays.setError(REQUIRED);
                    return;
                }

                setEditingEnabled(false);
                numTimes = Integer.parseInt(mNumTimes.getText().toString());
                createPopupWindow(numTimes);
            }
        });
    }

    private void createPopupWindow(int numTimes) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddMedicationActivity.this);
        alertDialog.setTitle("Alarms");
        alertDialog.setMessage("Enter time to take the medicine");

        final LinearLayout layout =  new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < numTimes; i++) {

            final EditText input = new EditText(AddMedicationActivity.this);
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
                    mTimePicker = new TimePickerDialog(AddMedicationActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
        alertDialog.setView(layout);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for (int i = 0; i < layout.getChildCount(); i++) {
                    EditText children = (EditText) layout.getChildAt(i);
                    String time = "";
                    time = children.getText().toString();
                    if (time.equals("")) {
                        isReminderSet = false;
                    } else if (!isValidTime(time)) {
                        children.setError("INVALID");
                        return;
                    }
                    allTimes.add(time);
                }
                saveMedication();
            }
        });
        alertDialog.show();
    }


    private void saveMedication() {
        final String medicationName = mMedicationName.getText().toString();
        final String dosage = mDosage.getText().toString();
        final String startDate = mStartDate.getText().toString();
        final String numDays = mNumDays.getText().toString();
        final String notes = mNotes.getText().toString();

        //TODO: Add checks for the format of the time


        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        final DatabaseReference newRef = mDatabase.child("medication_multi").push();
        final String key = newRef.getKey();

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + userId);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupId = dataSnapshot.child("groupId").getValue(String.class);
                calendar = Calendar.getInstance();

                int reqID = (int) calendar.getTimeInMillis();

                Medication medication = new Medication(medicationName, Integer.valueOf(dosage), startDate, Integer.valueOf(numDays), userId, key, notes, reqID, numTimes, allTimes,
                        null, groupId, isReminderSet);
                newRef.setValue(medication);

                if (isReminderSet) {
                    setMultipleReminders(medication);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent i =new Intent(AddMedicationActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMultipleReminders(Medication medication) {

        int reqID = medication.getReqID();
        for(String time : allTimes) {
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
//        intent.putExtra("time", medication.getAllTimes());
        intent.putExtra("numDays", String.valueOf(medication.getNumDays()));
        intent.putExtra("notes", medication.getNotes());
        intent.putExtra("hour", hour);
        intent.putExtra("min", min);
        intent.putExtra("reqID", reqID);
        intent.putStringArrayListExtra("allTimes", (ArrayList<String>) medication.getAllTimes());
        intent.putExtra("isReminderSet", isReminderSet);

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
//        intent.putExtra("time", medication.getAllTimes());
        intent.putExtra("numDays", String.valueOf(medication.getNumDays()));
        intent.putExtra("notes", medication.getNotes());
        intent.putExtra("hour", hour);
        intent.putExtra("min", min);
        intent.putExtra("reqID", reqID);
        intent.putStringArrayListExtra("allTimes", (ArrayList<String>) medication.getAllTimes());
        intent.putExtra("isReminderSet", isReminderSet);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reqID, intent, PendingIntent.FLAG_ONE_SHOT);

        if(calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private boolean isValidTime(String timeOfAlarm) {
        Matcher matcher = timePattern.matcher(timeOfAlarm);
        return matcher.matches();
    }

    private boolean isValidDate(String date) {
        String dateFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        sdf.setLenient(false);

        try {
            sdf.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private void setEditingEnabled(boolean enabled) {
        mMedicationName.setEnabled(enabled);
        mDosage.setEnabled(enabled);
        mStartDate.setEnabled(enabled);
        mNumDays.setEnabled(enabled);
        mNotes.setEnabled(enabled);
        mNumTimes.setEnabled(enabled);
        if (enabled) {
            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mSaveButton.setVisibility(View.GONE);
        }
    }

}