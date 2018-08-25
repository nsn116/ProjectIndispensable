package com.projectindispensable.projectindispensable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class OCRActivity extends AppCompatActivity {

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        byte[] byteArrayExtra = getIntent().getByteArrayExtra("photo");
        imageBitmap = BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length, new BitmapFactory.Options());
//        Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show();
        doRecognize();
    }

    @SuppressLint("SetTextI18n")
    public void doRecognize() {
        try {
            new doRequest().execute();
        } catch (Exception e) {
            Intent errorIntent = new Intent(OCRActivity.this, ErrorActivity.class);
            errorIntent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
            startActivity(errorIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ScannedInfo scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
        Intent intent;
        if (scannedInfo.prescriptionInfoIsNull()) {
            scannedInfo.setDefaultValues();
            intent = new Intent(OCRActivity.this, ScanPrescriptionActivity.class);
        } else {
            scannedInfo.setMedicationInfo("");
            intent = new Intent(OCRActivity.this, ScanMedicationActivity.class);
        }
        intent.putExtra("scannedInfo", scannedInfo);
        startActivity(intent);
        finish();
    }

    private String process() {
        String scanned = "";
        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (txtRecognizer.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
            SparseArray items = txtRecognizer.detect(frame);
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = (TextBlock) items.valueAt(i);
                strBuilder.append(item.getValue());
                strBuilder.append("/");
                for (Text line : item.getComponents()) {
                    //extract scanned text lines here
                    for (Text element : line.getComponents()) {
                        //extract scanned text words here

                    }
                }
            }
            scanned = strBuilder.toString().substring(0, strBuilder.toString().length() - 1);
        }
        return scanned;
    }

    @SuppressLint("StaticFieldLeak")
    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                this.e = null;
                Intent errorIntent = new Intent(OCRActivity.this, ErrorActivity.class);
                errorIntent.putExtra("scannedInfo", getIntent().getSerializableExtra("scannedInfo"));
                startActivity(errorIntent);
            } else {
                ScannedInfo scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
                if (scannedInfo.prescriptionInfoIsNull()) {
                    scannedInfo.setPrescriptionInfo(data);
                } else {
                    scannedInfo.setMedicationInfo(data);
                }
                final Intent nextActivityIntent;
                if (scannedInfo.medicationInfoIsNull()) {
                    nextActivityIntent = new Intent(OCRActivity.this, ScanMedicationActivity.class);
                } else {
                    scannedInfo = (ScannedInfo) getIntent().getSerializableExtra("scannedInfo");
                    MedicationInfo medicationInfo = getValidityOrDosage(scannedInfo);
                    if (medicationInfo == null) {
                        nextActivityIntent = new Intent(OCRActivity.this, MismatchActivity.class);
                    } else {
                        nextActivityIntent = new Intent(OCRActivity.this, MatchActivity.class);
                        nextActivityIntent.putExtra("medicationInfo", medicationInfo);
                    }
                    scannedInfo.setDefaultValues();
                }
                nextActivityIntent.putExtra("scannedInfo", scannedInfo);
                nextActivityIntent.putExtra("originalImage", getIntent().getByteArrayExtra("original"));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        startActivity(nextActivityIntent);
                    }
                }, 500);
//                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

    private List<String> modifyList(List<String> oldList) {
        int idx = -1;
        String s = "";
        List<String> l = new ArrayList<>();
        for (String w : oldList) {
            l.add(w.replaceAll("[^A-Za-z0-9]", ""));
        }
        for (int i = 0; i < l.size(); i++) {
            s = l.get(i);
            if ((s.lastIndexOf("mg") == (s.length() - 2)) && s.length() > 2) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            l.remove(idx);
            l.add(s.substring(0, s.lastIndexOf("mg")));
            l.add("mg");
        }
        Set<String> commonMedWords = ScannedInfo.getCommonWords();
        for (String word : commonMedWords) {
            l.remove(word);
        }
        return l;
    }

    private MedicationInfo getValidityOrDosage(ScannedInfo scannedInfo) {
        String medication = scannedInfo.getMedicationInfo().trim();
        String prescription = scannedInfo.getPrescriptionInfo().trim();

        String[] medicationArray = medication.toLowerCase().split("\\s+");
        List<String> medicationList = modifyList(new ArrayList<>(Arrays.asList(medicationArray)));

        String[] prescriptionArray = prescription.toLowerCase().split("\\s+");
        List<String> prescriptionList = modifyList(new ArrayList<>(Arrays.asList(prescriptionArray)));

        for (int i = 0 ; i < medicationList.size(); i++) {
            if (!prescriptionList.contains(medicationList.get(i))) {
                return null;
            }
        }

        String s;
        String prescriptionDosage = "";
        int mgLocation = -1;
        int magnitudeLocation = -1;

        for (int j = 0; j < prescriptionList.size(); j++) {
            s = prescriptionList.get(j);
            if (s.equals("mg")) {
                mgLocation = j;
                magnitudeLocation = j - 1;
                prescriptionDosage = prescriptionList.get(j - 1);
            }
            if (!medicationList.contains(s)) {
                return null;
            }
        }

        if (mgLocation != -1) {
            prescriptionList.remove(mgLocation);
        }
        if (magnitudeLocation != -1) {
            prescriptionList.remove(magnitudeLocation);
        }

        StringBuilder name = new StringBuilder();
        for (String temp : prescriptionList) {
            name.append(temp).append(" ");
        }

        String medicationName = name.toString().trim();
        if (mgLocation == -1) {
            return new MedicationInfo(medicationName, "");
        } else {
            return new MedicationInfo(medicationName, prescriptionDosage);
        }
    }
}
