package com.projectindispensable.projectindispensable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ScheduleMedicationAdapter extends ArrayAdapter<Medication> {

    private Context mContext;
    private int resource;
    private List<Medication> medications;

    public ScheduleMedicationAdapter(@NonNull Context context, int resource, @NonNull List<Medication> medications) {
        super(context, resource, medications);
        this.mContext = context;
        this.resource = resource;
        this.medications = medications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(resource, parent, false);
        }

        TextView name = (TextView) listItem.findViewById(android.R.id.text1);
        Medication medication = medications.get(position);
        String scheduleLabel = medication.findAlarmTime(medication.findTimeIndex()) + " " +
            medication
            .toString();
        name.setText(scheduleLabel);

        return listItem;
    }
}
