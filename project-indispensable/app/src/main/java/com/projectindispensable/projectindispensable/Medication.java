package com.projectindispensable.projectindispensable;

import java.util.List;

public class Medication {

  private String medicationName;
  private int dosage;
  private String startDate;
  private int numDays;
  //    private String timeAlarm;
  private String key;
  private String notes;
  private int numTimes;
  private List<String> allTimes;
  private String medicationPic;

  private int reqID;

  private String userId;
  private String groupId;

  private int timeIndex;
  private boolean isReminderSet;

  public Medication() {
  }

  public Medication(String medicationName, int dosage, String startDate,
                    int numDays, String userId, String key, String notes, int reqID,
                    int numTimes, List<String> allTimes, String medicationPic, String groupId, boolean isReminderSet) {
    this.medicationName = medicationName;
    this.dosage = dosage;
    this.startDate = startDate;
    this.numDays = numDays;
    this.key = key;
    this.userId = userId;
    this.notes = notes;
    this.reqID = reqID;
    this.numTimes = numTimes;
    this.allTimes = allTimes;
    this.medicationPic = medicationPic;
    this.groupId = groupId;
    this.isReminderSet = isReminderSet;
  }

  public String getMedicationName() {
    return medicationName;
  }

  public String getStartDate() {
    return startDate;
  }

  public int getNumDays() {
    return numDays;
  }


  public int getDosage() {
    return dosage;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getUserId() {
    return userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getNotes() {
    return notes;
  }

  public int getReqID() {
    return reqID;
  }

  public int getNumTimes() {
    return numTimes;
  }

  public List<String> getAllTimes() {
    return allTimes;
  }

  @Override
  public String toString() {
    return this.medicationName + " " + this.dosage + "mg";
  }


  public int findTimeIndex() {
    return timeIndex;
  }

  public int numberOfRepeats() {
    return allTimes.size();
  }

  public String findAlarmTime(int index) {
    return allTimes.get(index);
  }

  public void setTimeIndex(int timeIndex) {
    this.timeIndex = timeIndex;
  }

  public String getMedicationPic() {
    return medicationPic;
  }

  public boolean getIsReminderSet() {
    return isReminderSet;
  }
}