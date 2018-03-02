package com.example.facedoor.model;

public class User {
    public String userID;
    public String name;
    public String groupId;

    public User() {

    }


    public User(String userID, String name) {
        this.userID = userID;
        this.name = name;
    }

    public User(String userID, String name, String groupId) {
        this.userID = userID;
        this.name = name;
        this.groupId = groupId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
