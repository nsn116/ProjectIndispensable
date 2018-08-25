package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ScannedInfo scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
                Intent intent;
                if (scannedInfo.prescriptionInfoIsNull()) {
                    intent = new Intent(ErrorActivity.this, ScanPrescriptionActivity.class);
                    scannedInfo.setDefaultValues();
                } else {
                    intent = new Intent(ErrorActivity.this, ScanMedicationActivity.class);
                    scannedInfo.setMedicationInfo("");
                }
                intent.putExtra("scannedInfo", scannedInfo);
                startActivity(intent);
            }
        }, 2000);
    }
}

