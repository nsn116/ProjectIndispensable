package com.projectindispensable.projectindispensable;

public class GroupMemberInfo {
    private String invitedBy;
    private String nameInGroup;

    public GroupMemberInfo() {
    }

    public GroupMemberInfo(String invitedBy, String nameInGroup) {
        this.invitedBy = invitedBy;
        this.nameInGroup = nameInGroup;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public String getNameInGroup() {
        return nameInGroup;
    }

    @Override
    public String toString() {
        return nameInGroup;
    }
}
