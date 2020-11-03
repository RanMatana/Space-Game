package com.example.myapplication;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Comparator;

public class User implements Comparable<User> {

    private String userID, userName,uri_image;
    private int score;


    public User(String userID, String userName, int score, String uri_image) {
        this.userID = userID;
        this.userName = userName;
        this.score = score;
        this.uri_image = uri_image;
    }

    public User() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUri_image() {
        return uri_image;
    }

    public void setUri_image(String uri_image) {
        this.uri_image = uri_image;
    }

    @Override
    public int compareTo(User o) {
        if (this.getScore() < o.getScore()) return 1;
        else return -1;
    }
}
