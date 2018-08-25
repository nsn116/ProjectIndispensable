package com.projectindispensable.projectindispensable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial") //This annotation hides compiler warnings
public class ScannedInfo implements Serializable {

    private String prescriptionInfo;
    private String medicationInfo;

    public ScannedInfo() {
        this.prescriptionInfo = "";
        this.medicationInfo = "";
    }

    public static Set<String> getCommonWords () {
        Set<String> commonWords = new HashSet<>();
        commonWords.add("dose");
        commonWords.add("doses");
        commonWords.add("medicine");
        commonWords.add("medicines");
        commonWords.add("tablet");
        commonWords.add("tablets");
        commonWords.add("bolus");
        commonWords.add("boli");
        commonWords.add("boluses");
        commonWords.add("lozenge");
        commonWords.add("lozenges");
        commonWords.add("pellet");
        commonWords.add("pellets");
        commonWords.add("troche");
        commonWords.add("trouches");
        commonWords.add("pilule");
        commonWords.add("pilules");
        commonWords.add("capsule");
        commonWords.add("capsules");
        commonWords.add("cap");
        commonWords.add("caps");
        commonWords.add("drop");
        commonWords.add("drops");
        commonWords.add("tab");
        commonWords.add("tabs");
        commonWords.add("hydrochloride");
        commonWords.add("filmcoated");

        return commonWords;
    }

    public boolean prescriptionInfoIsNull() {
        return this.prescriptionInfo.equals("");
    }

    public boolean medicationInfoIsNull() {
        return this.medicationInfo.equals("");
    }

    public void setPrescriptionInfo(String prescriptionInfo) {
        this.prescriptionInfo = prescriptionInfo;
    }

    public void setMedicationInfo(String medicationInfo) {
        this.medicationInfo = medicationInfo;
    }

    public String getPrescriptionInfo() {
        return prescriptionInfo;
    }

    public String getMedicationInfo() {
        return medicationInfo;
    }

    public void setDefaultValues() {
        this.setMedicationInfo("");
        this.setPrescriptionInfo("");
    }

}
