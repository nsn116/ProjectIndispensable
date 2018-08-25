package com.projectindispensable.projectindispensable;

import java.util.List;
import java.util.Map;

public class GroupInfo {
    private String groupName;
    private Map<String, GroupMemberInfo> members;
    private String admin;

    public GroupInfo() {
    }

    public GroupInfo(String groupName, Map<String, GroupMemberInfo> members, String admin) {
        this.groupName = groupName;
        this.members = members;
        this.admin = admin;
    }

    public String getGroupName() {
        return groupName;
    }

    public Map<String, GroupMemberInfo> getMembers() {
        return members;
    }

    public String getAdmin() {
        return admin;
    }
}
