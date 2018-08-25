package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanMedicationActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoURI;
    String mCurrentPhotoPath;

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
    public void onBackPressed() {
        super.onBackPressed();
        ScannedInfo scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
        scannedInfo.setDefaultValues();
        Intent intent = new Intent(ScanMedicationActivity.this, ScanPrescriptionActivity.class);
        intent.putExtra("scannedInfo", scannedInfo);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_medication);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation
            .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_scan).setChecked(true);
    }


    public void dispatchAddMedicationIntent(View view) {
        Intent addMedicationIntent = new Intent(ScanMedicationActivity.this, AddMedicationActivity.class);
        startActivity(addMedicationIntent);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Intent errorIntent = new Intent(ScanMedicationActivity.this, ErrorActivity.class);
                errorIntent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
                startActivity(errorIntent);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
//                photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
                photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(ScanMedicationActivity.this, CropActivity.class);
            Uri uri = photoURI;
            intent.setData(uri);
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            if (imageBitmap != null) {
//                imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
//            }
//            byte[] image = stream.toByteArray();
//            intent.putExtra("original", image);
            intent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
            startActivity(intent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
