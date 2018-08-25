package com.projectindispensable.projectindispensable;

public class GroupInvitation {
    private String groupId;
    private String groupName;
    private String inviter;
    private String invitee;

    public GroupInvitation() {
    }

    public GroupInvitation(String groupId, String groupName, String inviter, String intvitee) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.inviter = inviter;
        this.invitee = intvitee;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getInviter() {
        return inviter;
    }

    public String getInvitee() {
        return invitee;
    }

    public String getGroupName() {
        return groupName;
    }
}
