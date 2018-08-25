package com.projectindispensable.projectindispensable;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.system.ErrnoException;
import android.view.View;
import android.widget.Toast;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class  CropActivity extends AppCompatActivity {

    private CropImageView mCropImageView;
    private Uri mCropImageUri;
    private Bitmap croppedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        mCropImageView = (CropImageView)  findViewById(R.id.CropImageView);
        Uri imageUri =  getIntent().getData();

        // For API >= 23 we need to check specifically that we have permissions to read external storage,
        // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
        boolean requirePermissions = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                isUriRequiresPermissions(imageUri)) {

            // request permissions and handle the result in onRequestPermissionsResult()
            requirePermissions = true;
            mCropImageUri = imageUri;
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        if (!requirePermissions) {
            mCropImageView.setImageUriAsync(imageUri);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ScannedInfo scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
        Intent intent;
        if (scannedInfo.prescriptionInfoIsNull()) {
            scannedInfo.setDefaultValues();
            intent = new Intent(CropActivity.this, ScanPrescriptionActivity.class);
        } else {
            scannedInfo.setMedicationInfo("");
            intent = new Intent(CropActivity.this, ScanMedicationActivity.class);
        }
        intent.putExtra("scannedInfo", scannedInfo);
        startActivity(intent);
        finish();
    }

    /**
     * Crop the image and set it back to the cropping view.
     */
    public void onCropImageClick(View view) {
        croppedBitmap =  mCropImageView.getCroppedImage(500, 500);
        if (croppedBitmap != null) {
            mCropImageView.setImageBitmap(croppedBitmap);
            Intent intent = new Intent(CropActivity.this, OCRActivity.class);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (croppedBitmap != null) {
                croppedBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            }
            byte[] image = stream.toByteArray();
            intent.putExtra("photo", image);
            intent.putExtra("original", getIntent().getByteArrayExtra("original"));
            intent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCropImageView.setImageUriAsync(mCropImageUri);
        } else {
            Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Test if we can open the given Android URI to test if permission required error is thrown.<br>
     */
    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (e.getCause() instanceof ErrnoException) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
