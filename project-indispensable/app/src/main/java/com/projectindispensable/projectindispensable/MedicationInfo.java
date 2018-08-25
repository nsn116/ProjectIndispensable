package com.projectindispensable.projectindispensable;

import java.io.Serializable;

public class MedicationInfo implements Serializable {

    private final String name;
    private final String dosage;

    public MedicationInfo(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }
}
