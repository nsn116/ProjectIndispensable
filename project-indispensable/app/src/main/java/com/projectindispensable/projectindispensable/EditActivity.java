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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {

    private boolean isReminderSet;

    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mMedicationName;
    private EditText mDosage;
    private EditText mStartDate;
    private EditText mNumDays;
    private EditText mNotes;
    private EditText mNumTimes;

    private Calendar calendar;

    private List<String> allTimes;

    String medicineName;
    String dosage;
    String startDate;
    String numDays;
    String notes;
    int numTimes;

    private Button mSaveButton;

    private final String TIME_24_HOURS_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    private final Pattern timePattern = Pattern.compile(TIME_24_HOURS_FORMAT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMedicationName = (EditText) findViewById(R.id.edit_medication_name);
        mDosage = (EditText) findViewById(R.id.edit_dosage);
        mStartDate = (EditText) findViewById(R.id.edit_start_date);
        mNumDays = (EditText) findViewById(R.id.edit_num_days);
        mNotes = (EditText) findViewById(R.id.edit_notes);
        mNumTimes = (EditText) findViewById(R.id.edit_num_times);

        medicineName = getIntent().getStringExtra("medicineName");
        dosage = getIntent().getStringExtra("dosage");
        startDate = getIntent().getStringExtra("startDate");
        numDays = getIntent().getStringExtra("numDays");
        notes = getIntent().getStringExtra("notes");
        numTimes = getIntent().getIntExtra("numTimes", 0);
        isReminderSet = getIntent().getBooleanExtra("isReminderSet", true);
        allTimes = getIntent().getStringArrayListExtra("allTimes");

        mMedicationName.setText(medicineName);
        mDosage.setText(dosage);
        mStartDate.setText(startDate);
        mNumTimes.setText(String.valueOf(numTimes));
        mNumDays.setText(numDays);
        mNotes.setText(notes);

        mSaveButton = findViewById(R.id.edit_save_medication);

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
                if (numTimes != Integer.parseInt(mNumTimes.getText().toString())) {
                    allTimes = new ArrayList<>();
                }
                numTimes = Integer.parseInt(mNumTimes.getText().toString());
                createPopupWindow(numTimes);
            }
        });
    }

    private void createPopupWindow(int numTimes) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditActivity.this);
        alertDialog.setTitle("Alarms");
        alertDialog.setMessage("Enter time to take the medicine");

        final LinearLayout layout =  new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        if (allTimes.isEmpty() || allTimes.get(0).equals("")) {
            for (int i = 0; i < numTimes; i++) {
                final EditText input = new EditText(EditActivity.this);
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
                        mTimePicker = new TimePickerDialog(EditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                                String text;
                                if (selectedMinute < 10 && selectedHour < 10) {
                                    text = "0" + selectedHour + ":" + "0" + selectedMinute;
                                } else if (selectedMinute < 10) {
                                    text = selectedHour + ":0" + selectedMinute;
                                } else if (selectedHour < 10) {
                                    text = "0" + selectedHour + ":" + selectedMinute;
                                } else {
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
        } else {
            for (int i = 0; i < numTimes; i++) {
                final EditText input = new EditText(EditActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                input.setLayoutParams(lp);
                input.setText(allTimes.get(i));
                input.setFocusable(false);
                input.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(EditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                                String text;
                                if (selectedMinute < 10 && selectedHour < 10) {
                                    text = "0" + selectedHour + ":" + "0" + selectedMinute;
                                } else if (selectedMinute < 10) {
                                    text = selectedHour + ":0" + selectedMinute;
                                } else if (selectedHour < 10) {
                                    text = "0" + selectedHour + ":" + selectedMinute;
                                } else {
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

//                allTimes = new String[layout.getChildCount()];
                allTimes = new ArrayList<>();
                for (int i = 0; i < layout.getChildCount(); i++) {
                    EditText children = (EditText) layout.getChildAt(i);
                    String time = "";
                    time = children.getText().toString();
                    if (time.equals("")) {
                        isReminderSet = false;
                    } else if (!isValidTime(time)) {
                        children.setError("INVALID");
                        return;
                    } else {
                        isReminderSet = true;
                    }
                    allTimes.add(time);
                }
                saveMedication();
            }
        });
        alertDialog.show();

    }

    private void saveMedication() {
        final String sMedicationName = mMedicationName.getText().toString();
        final String sDosage = mDosage.getText().toString();
        final String sStartDate = mStartDate.getText().toString();
        final String sNumDays = mNumDays.getText().toString();
        final String notes = mNotes.getText().toString();

        //TODO: Add checks for the format of the time

        if (TextUtils.isEmpty(sMedicationName)) {
            mMedicationName.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(sDosage)) {
            mDosage.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(sStartDate)) {
            mStartDate.setError(REQUIRED);
            return;
        } else if (!isValidDate(sStartDate)) {
            mStartDate.setError("INVALID");
            return;
        }

        if (TextUtils.isEmpty(sNumDays)) {
            mNumDays.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        String key = getIntent().getStringExtra("key");
        String groupId = getIntent().getStringExtra("groupId");

        String userId = getIntent().getStringExtra("userId");

        calendar = Calendar.getInstance();

        int reqID = getIntent().getIntExtra("reqID", 0);

        Medication medication = new Medication(sMedicationName, Integer.valueOf(sDosage), sStartDate, Integer.valueOf(sNumDays), userId, key, notes, reqID, numTimes, allTimes,
            null, groupId, isReminderSet);
        FirebaseDatabase.getInstance().getReference("medication_multi/" + key).setValue(medication);

        if (isReminderSet) {
            setMultipleReminders(medication);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(EditActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        }, 2000);

    }

    private void setMultipleReminders(Medication medication) {

        int reqID = getIntent().getIntExtra("reqID", 0);
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
        mNumTimes.setEnabled(enabled);
        mNotes.setEnabled(enabled);
        if (enabled) {
            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mSaveButton.setVisibility(View.GONE);
        }
    }

}