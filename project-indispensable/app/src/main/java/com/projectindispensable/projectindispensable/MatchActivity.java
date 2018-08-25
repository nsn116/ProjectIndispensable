package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        final MedicationInfo medicationInfo = (MedicationInfo) getIntent().getSerializableExtra("medicationInfo");

//        final String medicationName = getIntent().getStringExtra("medicationName");
        //TODO: Do whatever needs to be done with the originalImage object
        byte[] originalImage = getIntent().getByteArrayExtra("originalImage");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(MatchActivity.this, AddMedicationActivity.class);
                i.putExtra("medName", medicationInfo.getName());

                if (medicationInfo.getDosage().equals("")) {
                    i.putExtra("dosage", "");
                } else {
                    i.putExtra("dosage", medicationInfo.getDosage());
                }

                i.putExtra("method", 1);
                startActivity(i);
            }
        }, 3000);
    }
}
