package com.projectindispensable.projectindispensable;

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
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountActivity extends AppCompatActivity {

  private FirebaseAuth mAuth;
  private TextView mFullName;
  private DatabaseReference mUserDatabase;
  private Button mEditButton;
  private ImageButton mProfilePic;
  private Menu mMenu;
  private String groupId;
  private String groupName;

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
          intent = new Intent(getApplicationContext(),
              MedicationActivity.class);
          break;
      }
      startActivity(intent);
      return true;

    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account);

    BottomNavigationView navigation = findViewById(R.id.accNavigation);
    BottomNavigationViewHelper.removeShiftMode(navigation);
    navigation
        .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    mEditButton = (Button) findViewById(R.id.edit_account);
    mEditButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getApplicationContext(),
            EditAccountActivity.class));
      }
    });

    mAuth = FirebaseAuth.getInstance();
    groupId = "";
    groupName = "";

      // Get the fields
    final TextView mGroupInfoTV = (TextView) findViewById(R.id.tv_group_info);
    final ListView mGroupMembersList = (ListView) findViewById(R.id.group_members);
    final TextView mGroupMembersTV = (TextView) findViewById(R.id.tv_group_members);

    // Create and set adapter
    final ArrayAdapter<GroupMemberInfo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<GroupMemberInfo>());
    mGroupMembersList.setAdapter(adapter);

    final String myUser = mAuth.getCurrentUser().getUid();
    mProfilePic = (ImageButton) findViewById(R.id.profile_picture);
    mFullName = (TextView) findViewById(R.id.full_name);
    mUserDatabase = FirebaseDatabase.getInstance().getReference
        ("users/" + myUser);

    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String firstName = dataSnapshot.child("firstName")
            .getValue(String.class);
        String lastName = dataSnapshot.child("lastName").getValue(String.class);
        String fullName = firstName + " " + lastName;
        String profilePicUri = dataSnapshot.child("profilePic")
            .getValue(String.class);
        mFullName.setText(fullName);
        Glide.with(AccountActivity.this).load(profilePicUri)
            .apply(new RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_person_black_24dp))
            .into(mProfilePic);

        groupId = dataSnapshot.child("groupId").getValue(String.class);
        if (!groupId.equals("")) {
          // If the user is part of a group, then set load the list of members from the database
          mGroupMembersTV.setVisibility(View.VISIBLE);
          mGroupMembersList.setVisibility(View.VISIBLE);

          int createGroupItemindex = 0;
          int addMemberItemIndex = 1;
          int leaveGroupItemIndex = 2;
          mMenu.getItem(createGroupItemindex).setVisible(false);
          mMenu.getItem(addMemberItemIndex).setVisible(true);
          mMenu.getItem(leaveGroupItemIndex).setVisible(true);

          DatabaseReference mGroupDatabase = FirebaseDatabase.getInstance().getReference("groups/" + groupId);

          // Retrieve group information from the database
          mGroupDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  GroupInfo groupInfo = dataSnapshot.getValue(GroupInfo.class);
                  groupName = groupInfo.getGroupName();
                  Map<String, GroupMemberInfo> members = groupInfo.getMembers();
                  mGroupInfoTV.setText(groupName);
                  for (GroupMemberInfo member : members.values()) {
                      adapter.add(member);
                  }
                  adapter.notifyDataSetChanged();
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
  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu2, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.create_group_menu_item:

                AlertDialog.Builder mCreateGroupDialogueBuilder = new AlertDialog.Builder(AccountActivity.this);
                View mCreateGroupView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
                final EditText mGroupName = mCreateGroupView.findViewById(R.id.et_group_name);
                final EditText mNameInGroup = mCreateGroupView.findViewById(R.id.et_name_in_group);
                final Button mCreateGroupButton = mCreateGroupView.findViewById(R.id.btn_create_group);

                mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String groupName = mGroupName.getText().toString();
                        if (TextUtils.isEmpty(groupName)) {
                            mGroupName.setError("Required");
                            return;
                        }

                        final String nameInGroup = mNameInGroup.getText().toString();
                        if (TextUtils.isEmpty(nameInGroup)) {
                            mNameInGroup.setError("Required");
                            return;
                        }

                        mGroupName.setEnabled(false);
                        mNameInGroup.setEnabled(false);

                        String groupAdmin = mAuth.getCurrentUser().getUid();
                        Map<String, GroupMemberInfo> members = new HashMap<>();
                        final String groupId = UUID.randomUUID().toString();

                        GroupMemberInfo memberInfo = new GroupMemberInfo(groupAdmin, nameInGroup);
                        members.put(groupAdmin, memberInfo);

                        GroupInfo group = new GroupInfo(groupName, members, groupAdmin);

                        FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).setValue(group);
                        FirebaseDatabase.getInstance().getReference("users/" + groupAdmin).child("groupId").setValue(groupId);

                        String userId = mAuth.getCurrentUser().getUid();
                        final Query medicationQuery = FirebaseDatabase.getInstance().getReference()
                                .child("medication_multi").orderByChild("userId").equalTo(userId);
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

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent i = new Intent(AccountActivity.this, HomeActivity.class);
                                startActivity(i);
                            }
                        }, 250);
                    }
                });

                mCreateGroupDialogueBuilder.setView(mCreateGroupView);
                AlertDialog createGroupDialogue = mCreateGroupDialogueBuilder.create();
                createGroupDialogue.show();
                break;

            case R.id.add_member_menu_item:
                AlertDialog.Builder mAddMemberDialogueBuilder = new AlertDialog.Builder(AccountActivity.this);
                View mAddMemberView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
                final EditText mEmailET = mAddMemberView.findViewById(R.id.et_email_address);
                final Button mInviteBtn = mAddMemberView.findViewById(R.id.btn_invite_member);

                mInviteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String email = mEmailET.getText().toString();
                        if (TextUtils.isEmpty(email)) {
                            mEmailET.setError("Email not found");
                            return;
                        }

    //                    mEmailET.setEnabled(false);
                        final String emailKey = email.replace(".", ",");
                        final DatabaseReference emails = FirebaseDatabase.getInstance().getReference().child("emails/" + emailKey);
                        emails.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    mEmailET.setError("No such email found");
                                    emails.removeEventListener(this);
                                    return;
                                }
                                mEmailET.setEnabled(false);
                                mInviteBtn.setEnabled(false);
                                String inviteeUID = dataSnapshot.getValue(String.class);
                                String myUser = mAuth.getCurrentUser().getUid();
                                GroupInvitation invitation = new GroupInvitation(groupId, groupName, myUser, inviteeUID);

//                                GroupMemberInfo groupMemberInfo = new GroupMemberInfo(false, myUser, "");
                                DatabaseReference invitations = FirebaseDatabase.getInstance().getReference().child("invitations")
                                        .push();
                                invitations.setValue(invitation);

//                                DatabaseReference groupDatabase = FirebaseDatabase.getInstance()
//                                        .getReference("groups/" + groupId + "/members");
//                                groupDatabase.child(inviteeUID).setValue(groupMemberInfo);

                                emails.removeEventListener(this);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        Intent i = new Intent(AccountActivity.this, HomeActivity.class);
                                        startActivity(i);
                                    }
                                }, 250);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                mAddMemberDialogueBuilder.setView(mAddMemberView);
                AlertDialog addMemberDialogue = mAddMemberDialogueBuilder.create();
                addMemberDialogue.show();
                break;
            case R.id.leave_group_menu_item:
                String userId = mAuth.getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("users/" + userId)
                        .child("groupId").setValue("");
                FirebaseDatabase.getInstance()
                        .getReference("groups/" + groupId + "/members/" + userId).removeValue();
                final Query medicationQuery = FirebaseDatabase.getInstance().getReference()
                        .child("medication_multi").orderByChild("userId").equalTo(userId);
                medicationQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().child("groupId").setValue("");
                        }
                        medicationQuery.removeEventListener(this);
                        Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                break;

        }
        return true;
    }
}
