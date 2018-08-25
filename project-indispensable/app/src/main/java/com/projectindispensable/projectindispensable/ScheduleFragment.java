package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link ScheduleFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// */
public class ScheduleFragment extends Fragment {
    private FirebaseAuth mAuth;
    private ListView mMorningMedicationList;
    private ListView mAfternoonMedicationList;
    private ListView mEveningMedicationList;
    private ArrayAdapter<Medication> morningAdapter;
    private ArrayAdapter<Medication> afternoonAdapter;
    private ArrayAdapter<Medication> eveningAdapter;
    private String groupId;
    private Query queryToRefresh;
    private Comparator<Medication> comparator;

    //    private OnFragmentInteractionListener mListener;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab1_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
//        TextView mDateTV = (TextView) getView().findViewById(R.id.date_text_view);
//        Date date = Calendar.getInstance().getTime();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        mDateTV.setText(sdf.format(date));

//        queryToRefresh = null;

        mMorningMedicationList = (ListView) getView().findViewById(R.id.morning_medication_list);
        mAfternoonMedicationList = (ListView) getView().findViewById(R.id.afternoon_medication_list);
        mEveningMedicationList = (ListView) getView().findViewById(R.id.evening_medication_list);
        morningAdapter = new ScheduleMedicationAdapter(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<Medication>());
        afternoonAdapter = new ScheduleMedicationAdapter(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<Medication>());
        eveningAdapter = new ScheduleMedicationAdapter(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<Medication>());
        mMorningMedicationList.setAdapter(morningAdapter);
        mAfternoonMedicationList.setAdapter(afternoonAdapter);
        mEveningMedicationList.setAdapter(eveningAdapter);

//        Utility.setListViewHeightBasedOnChildren(mMorningMedicationList);
//        Utility.setListViewHeightBasedOnChildren(mAfternoonMedicationList);
//        Utility.setListViewHeightBasedOnChildren(mEveningMedicationList);


        final DatabaseReference mMedicationDatabase = FirebaseDatabase.getInstance().getReference();
        final String myUser = mAuth.getCurrentUser().getUid();
        final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + myUser);

        comparator = new Comparator<Medication>() {
            @Override
            public int compare(Medication medication1, Medication medication2) {
                String alarmTime1 = medication1.findAlarmTime(medication1.findTimeIndex());
                int hour1 = getHour(alarmTime1);
                String alarmTime2 = medication2.findAlarmTime(medication2.findTimeIndex());
                int hour2 = getHour(alarmTime2);
                int minutes1 = getMinute(alarmTime1);
                int minutes2 = getMinute(alarmTime2);
                boolean hourLess = hour1 < hour2;
                boolean hourEq = hour1 == hour2;
                boolean minsLess = minutes1 < minutes2;
                boolean minsEq = minutes1 == minutes2;
                return (hourLess || (hourEq && minsLess)) ?
                        -1 :
                        ((hourEq && minsEq) ? 0 : 1);
            }
        };
//        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                groupId = dataSnapshot.child("groupId").getValue(String.class);
////                Query query;
//                if (groupId.equals("")) {
//                    queryToRefresh = mMedicationDatabase.child("medication_multi")
//                            .orderByChild("userId").equalTo(myUser);
//                } else {
//                    queryToRefresh = mMedicationDatabase.child("medication_multi")
//                            .orderByChild("groupId").equalTo(groupId);
//                }
////                final Query finalQuery = query;
//                queryToRefresh.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        displayMedications(dataSnapshot);
//                        queryToRefresh.removeEventListener(this);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });

        mMorningMedicationList.setOnItemClickListener(createOnItemClickListener(morningAdapter));
        mAfternoonMedicationList.setOnItemClickListener(createOnItemClickListener(afternoonAdapter));
        mEveningMedicationList.setOnItemClickListener(createOnItemClickListener(eveningAdapter));

        mMorningMedicationList.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        mAfternoonMedicationList.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        mEveningMedicationList.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    private void displayMedications(DataSnapshot dataSnapshot) {
        int hour;
        Medication medication;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            medication = snapshot.getValue(Medication.class);
            for (int i = 0; i < medication.numberOfRepeats(); i++) {
                String alarmTime = medication.findAlarmTime(i);
                if (alarmTime.equals("")) {
                    continue;
                }
                hour = getHour(medication.findAlarmTime(i));
                medication.setTimeIndex(i);
                if (hour >= 0 && hour < 11) {
                    morningAdapter.add(medication);
                    morningAdapter.sort(comparator);
                    morningAdapter.notifyDataSetChanged();
                } else if (hour >= 11 && hour < 18) {
                    afternoonAdapter.add(medication);
                    afternoonAdapter.sort(comparator);
                    afternoonAdapter.notifyDataSetChanged();
                } else {
                    eveningAdapter.add(medication);
                    eveningAdapter.sort(comparator);
                    eveningAdapter.notifyDataSetChanged();
                }
                medication = new Medication(medication.getMedicationName(),
                        medication.getDosage(), medication.getStartDate(),
                        medication.getNumDays(), medication.getUserId(),
                        medication.getKey(), medication.getNotes(),
                        medication.getReqID(), medication.getNumTimes(),
                        medication.getAllTimes(), medication.getMedicationPic(), medication.getGroupId(),
                        medication.getIsReminderSet());
            }
        }
    }

    private static int getHour(String time) {
        return Integer.parseInt(time.split(":")[0]);
    }

    private static int getMinute(String time) {
        return Integer.parseInt(time.split(":")[1]);
    }

    private AdapterView.OnItemClickListener createOnItemClickListener(final ArrayAdapter<Medication> adapter) {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Medication medication = adapter.getItem(position);
                Intent intent = new Intent(view.getContext(), NotificationActivity.class);
                if (medication != null) {
                    intent.putExtra("medicineName", medication.getMedicationName());
                    String dosage = String.valueOf(medication.getDosage());
                    intent.putExtra("dosage", dosage);
                    intent.putExtra("startDate", medication.getStartDate());
                    intent.putStringArrayListExtra("allTimes",
                        (ArrayList<String>) medication.getAllTimes());
                    intent.putExtra("numDays", String.valueOf(medication.getNumDays()));
                    intent.putExtra("pos", position);
                    intent.putExtra("key", medication.getKey());
                    intent.putExtra("notes", medication.getNotes());
                    intent.putExtra("reqID", medication.getReqID());
                    intent.putExtra("numTimes", medication.getNumTimes());
                    intent.putExtra("groupId", medication.getGroupId());
                    intent.putExtra("userId", medication.getUserId());
                    intent.putExtra("isReminderSet", medication.getIsReminderSet());
                }
                adapter.clear();
                startActivity(intent);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference mMedicationDatabase = FirebaseDatabase.getInstance().getReference();
        final String myUser = mAuth.getCurrentUser().getUid();
        final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + myUser);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupId = dataSnapshot.child("groupId").getValue(String.class);
//                Query query;
                if (groupId.equals("")) {
                    queryToRefresh = mMedicationDatabase.child("medication_multi")
                            .orderByChild("userId").equalTo(myUser);
                } else {
                    queryToRefresh = mMedicationDatabase.child("medication_multi")
                            .orderByChild("groupId").equalTo(groupId);
                }
//                final Query finalQuery = query;
                queryToRefresh.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        displayMedications(dataSnapshot);
                        queryToRefresh.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
