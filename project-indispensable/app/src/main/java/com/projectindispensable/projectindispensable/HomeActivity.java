package com.projectindispensable.projectindispensable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

  private FirebaseAuth mAuth;
  private TextView mHelloUser;
  private DatabaseReference mUserDatabase;
  private ImageButton mProfilePic;
  private boolean acceptedInvite;

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
                  intent = new Intent(getApplicationContext(), ScanPrescriptionActivity.class);
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
    setContentView(R.layout.activity_home);

    BottomNavigationView navigation = findViewById(R.id.homeNavigation);
    BottomNavigationViewHelper.removeShiftMode(navigation);
    navigation
        .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
    mProfilePic = (ImageButton) findViewById(R.id.profile_picture);
    mProfilePic.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        startActivity(new Intent(getApplicationContext(), AccountActivity.class));
      }
    });
    mHelloUser = (TextView) findViewById(R.id.hello_user);
    mAuth = FirebaseAuth.getInstance();
    final String myUser = mAuth.getCurrentUser().getUid();
    mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + myUser);

    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String displayName = dataSnapshot.child("firstName").getValue(String.class);
        String greeting = "Hello, " + displayName + "!";
        String profilePicUri = dataSnapshot.child("profilePic")
            .getValue(String.class);
        mHelloUser.setText(greeting);
        Glide.with(HomeActivity.this).load(profilePicUri)
            .apply(new RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_person_black_24dp))
            .into(mProfilePic);

        final String groupId = dataSnapshot.child("groupId").getValue(String.class);

        if (groupId.equals("")) {
            acceptedInvite = false;
            final Query invitationQuery = FirebaseDatabase.getInstance().getReference()
                    .child("invitations").orderByChild("invitee").equalTo(myUser);
            invitationQuery.addValueEventListener(new ValueEventListener() {
//        private boolean acceptedInvite;

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
//                GroupInvitation invitation;
//                acceptedInvite = false;
                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (acceptedInvite) {
                                snapshot.getRef().removeValue();
                            }
                            final GroupInvitation invitation = snapshot.getValue(GroupInvitation.class);
                            final String groupId = invitation.getGroupId();
                            AlertDialog.Builder mAcceptInviteDialogue =
                                    new AlertDialog.Builder(HomeActivity.this);
                            View mView = getLayoutInflater().inflate(R.layout.dialog_accept_invitation, null);
                            final Button mAcceptBtn = mView.findViewById(R.id.btn_accept_invitation);
                            final Button mDeclineBtn = mView.findViewById(R.id.btn_decline_invitation);
                            final TextView mGroupInfoTV = mView.findViewById(R.id.tv_group_info);
                            final EditText mNameInGroup = mView.findViewById(R.id.et_name_in_group);

                            String groupInfo = "You've been invited to group " + invitation.getGroupName();
                            mGroupInfoTV.setText(groupInfo);

                            mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String nameInGroup = mNameInGroup.getText().toString();
                                    if (TextUtils.isEmpty(nameInGroup)) {
                                        mNameInGroup.setError("Required");
                                        return;
                                    }

                                    acceptedInvite = true;
                                    GroupMemberInfo memberInfo = new GroupMemberInfo(invitation.getInviter(), nameInGroup);
                                    FirebaseDatabase.getInstance()
                                            .getReference("groups/" + groupId + "/members")
                                            .child(myUser).setValue(memberInfo);
                                    FirebaseDatabase.getInstance().getReference("users/" + myUser)
                                            .child("groupId").setValue(groupId);
                                    final Query medicationQuery = FirebaseDatabase.getInstance().getReference()
                                            .child("medication_multi").orderByChild("userId").equalTo(myUser);
                                    medicationQuery.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                snapshot.getRef().child("groupId").setValue(groupId);
                                            }
                                            medicationQuery.removeEventListener(this);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    deleteInvitation(snapshot);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(HomeActivity.this, HomeActivity.class);
                                            startActivity(i);
                                        }
                                    }, 250);
                                }
                            });

                            mDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteInvitation(snapshot);
//                                    acceptedInvite = false;
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("groups/" + groupId + "/members/" + myUser).removeValue();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(HomeActivity.this, HomeActivity.class);
                                            startActivity(i);
                                        }
                                    }, 250);
                                }
                            });

                            mAcceptInviteDialogue.setView(mView);
                            AlertDialog addMemberDialogue = mAcceptInviteDialogue.create();
                            addMemberDialogue.show();

                        }
                    }
                    invitationQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            acceptedInvite = true;
            final Query invitationQuery = FirebaseDatabase.getInstance().getReference()
                    .child("invitations").orderByChild("invitee").equalTo(myUser);
            invitationQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("groupId").getValue(String.class).equals(groupId)) {
                            snapshot.getRef().removeValue();
                        }
                    }
                    invitationQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    TextView reminders = (TextView) findViewById(R.id.date_text_view);
    reminders.setText("Upcoming");

    LinearLayout morning = (LinearLayout) findViewById(R.id.morning);
    LinearLayout afternoon = (LinearLayout) findViewById(R.id.afternoon);
    LinearLayout evening = (LinearLayout) findViewById(R.id.evening);
    Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    if (hour >= 5 && hour < 13) {
      morning.setVisibility(View.VISIBLE);
      afternoon.setVisibility(View.GONE);
      evening.setVisibility(View.GONE);
    } else if (hour >= 13 && hour < 20) {
      morning.setVisibility(View.GONE);
      afternoon.setVisibility(View.VISIBLE);
      evening.setVisibility(View.GONE);
    } else {
      morning.setVisibility(View.GONE);
      afternoon.setVisibility(View.GONE);
      evening.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.app_bar_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.my_account:
        startActivity(new Intent(getApplicationContext(), AccountActivity
            .class));
        break;
      case R.id.log_out:
          final Query query = FirebaseDatabase.getInstance().getReference().
                  child("medication_multi").orderByChild("userId").
                  equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
          query.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                      Medication medication = snapshot.getValue(Medication.class);
                      if (medication.getIsReminderSet()) {
                          cancelReminders(medication);
                      }
                  }
                  query.removeEventListener(this);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });

          mAuth.signOut();
        startActivity(new Intent(getApplicationContext(),
            LoginActivity.class));
        finish();
        break;
    }
    return true;
  }

    private void cancelReminders(Medication medication) {

        int reqID = medication.getReqID();
        for (int i = 0; i < medication.getNumTimes(); i++) {
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

    private void deleteInvitation(DataSnapshot snapshot) {
      snapshot.getRef().removeValue();
  }
}
