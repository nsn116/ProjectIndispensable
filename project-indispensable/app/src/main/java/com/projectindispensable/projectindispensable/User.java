package com.projectindispensable.projectindispensable;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String groupId;
    private String profilePic;

    public User() {
    }

    public User(String firstName, String lastName, String email, String groupId,
        String profilePic) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.groupId = groupId;
        this.profilePic = profilePic;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
