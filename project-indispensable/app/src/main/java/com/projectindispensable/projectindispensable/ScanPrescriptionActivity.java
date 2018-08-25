package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanPrescriptionActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    private Uri photoURI;
    private ScannedInfo scannedInfo;

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
                    intent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
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
        setContentView(R.layout.activity_scan_prescription);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation
            .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_scan).setChecked(true);
        scannedInfo = new ScannedInfo();
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

    public void dispatchAddMedicationIntentPres(View view) {
        Intent addMedicationIntent = new Intent(ScanPrescriptionActivity.this, AddMedicationActivity.class);
        startActivity(addMedicationIntent);
    }

    public void dispatchTakePictureIntentPres(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Intent errorIntent = new Intent(ScanPrescriptionActivity.this, ErrorActivity.class);
                errorIntent.putExtra("scannedInfo", scannedInfo);
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
            Intent intent = new Intent(ScanPrescriptionActivity.this, CropActivity.class);
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
            intent.putExtra("scannedInfo", scannedInfo);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("picURI", photoURI);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        photoURI = savedInstanceState.getParcelable("picURI");
    }
}
