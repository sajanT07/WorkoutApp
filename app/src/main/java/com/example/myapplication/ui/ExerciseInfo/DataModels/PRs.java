package com.example.myapplication.ui.ExerciseInfo.DataModels;

import java.util.Date;

public class PRs {
    int PR;
    String date;

    public PRs(int PR, String date) {
        this.PR = PR;
        this.date = date;
    }

    public int getPR() {
        return PR;
    }

    public String getDate() {
        return date;
    }

}
