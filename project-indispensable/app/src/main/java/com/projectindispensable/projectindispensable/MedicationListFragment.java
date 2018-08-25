package com.projectindispensable.projectindispensable;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import java.util.List;


public class MedicationListFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

    private List<Medication> medicationList = new ArrayList<>();
    private ListView mMedicationList;
    private ArrayAdapter<Medication> adapter;
    private FirebaseAuth mAuth;
    private String groupId;
    private Query queryToRefresh;


    public MedicationListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medication_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        final String myUser = mAuth.getCurrentUser().getUid();

        mMedicationList = (ListView) getView().findViewById(R.id.medication_list);
        adapter = new ArrayAdapter<Medication>(getActivity(), android.R.layout.simple_list_item_1, medicationList);
        mMedicationList.setAdapter(adapter);
//        final DatabaseReference mMedicationDatabase = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + myUser);
//
//        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                groupId = dataSnapshot.child("groupId").getValue(String.class);
//                if (groupId.equals("")) {
//                    queryToRefresh = mMedicationDatabase.child("medication_multi")
//                            .orderByChild("userId").equalTo(myUser);
//                } else {
//                    queryToRefresh = mMedicationDatabase.child("medication_multi")
//                            .orderByChild("groupId").equalTo(groupId);
//                }
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

//        Query query = mMedicationDatabase.child("medication_groups")
//                .orderByChild("userId").equalTo(mAuth.getCurrentUser().getUid());
//        query.addChildEventListener(new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Medication medication = dataSnapshot.getValue(Medication.class);
//                adapter.add(medication);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        mMedicationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });
    }

    private void displayMedications(DataSnapshot dataSnapshot) {
        Medication medication;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            medication = snapshot.getValue(Medication.class);
            adapter.add(medication);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        final String myUser = mAuth.getCurrentUser().getUid();
        final DatabaseReference mMedicationDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("users/" + myUser);

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupId = dataSnapshot.child("groupId").getValue(String.class);
                if (groupId.equals("")) {
                    queryToRefresh = mMedicationDatabase.child("medication_multi")
                            .orderByChild("userId").equalTo(myUser);
                } else {
                    queryToRefresh = mMedicationDatabase.child("medication_multi")
                            .orderByChild("groupId").equalTo(groupId);
                }
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
                mUserDatabase.removeEventListener(this);
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
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
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
