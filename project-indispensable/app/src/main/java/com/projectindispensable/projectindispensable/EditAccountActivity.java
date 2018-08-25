package com.projectindispensable.projectindispensable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditAccountActivity extends AppCompatActivity {

  private static final int REQUEST_CAMERA = 3;
  private static final int SELECT_FILE = 2;
  private FirebaseAuth mAuth;
  private ImageButton mProfilePic;
  private EditText mFirstName;
  private EditText mLastName;
  private Button mSaveButton;
  private DatabaseReference mUserDatabase;
  private StorageReference mStorageRef;
  Uri imageHoldUri = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_account);

    mProfilePic = (ImageButton) findViewById(R.id.profile_picture);

    mSaveButton = (Button) findViewById(R.id.save_account);
    mSaveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        saveUserAccount();
      }
    });
    mStorageRef = FirebaseStorage.getInstance().getReference();
    mFirstName = (EditText) findViewById(R.id.first_name);
    mLastName = (EditText) findViewById(R.id.last_name);
    mAuth = FirebaseAuth.getInstance();
    final String myUser = mAuth.getCurrentUser().getUid();
    mUserDatabase = FirebaseDatabase.getInstance().getReference
        ("users/" + myUser);

    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String firstName = dataSnapshot.child("firstName")
            .getValue(String.class);
        String lastName = dataSnapshot.child("lastName").getValue(String.class);
        mFirstName.setText(firstName);
        mLastName.setText(lastName);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    mProfilePic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        //LOGIC FOR PROFILE PICTURE
        profilePicSelection();

      }
    });
  }

  private void saveUserAccount() {
    final String sFirstName = mFirstName.getText().toString();
    final String sLastName = mLastName.getText().toString();
    final String myUser = mAuth.getCurrentUser().getUid();
    mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" +
        myUser);
    mUserDatabase.child("firstName").setValue(sFirstName);
    mUserDatabase.child("lastName").setValue(sLastName);
    if (imageHoldUri != null) {

      StorageReference mChildStorage = mStorageRef.child("users")
          .child(imageHoldUri.getLastPathSegment());
      String profilePicUrl = imageHoldUri.getLastPathSegment();

      mChildStorage.putFile(imageHoldUri).addOnSuccessListener(
          new OnSuccessListener<TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

              final Uri imageUrl = taskSnapshot.getDownloadUrl();

              mUserDatabase.child("firstName").setValue(sFirstName);
              mUserDatabase.child("lastName").setValue(sLastName);
              mUserDatabase.child("profilePic").setValue(imageUrl.toString());

              startActivity(new Intent(getApplicationContext(),
                  AccountActivity.class));
              finish();
            }
          });
    }
  }

  private void profilePicSelection() {

    //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

    final CharSequence[] items = {"Take a photo", "Choose from gallery",
        "Cancel"};
    AlertDialog.Builder builder = new AlertDialog.Builder(
        EditAccountActivity.this);
    builder.setTitle("Upload a profile picture");

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

        mProfilePic.setImageURI(imageHoldUri);
      } else if (resultCode
          == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Exception error = result.getError();
      }
    }

  }

}
