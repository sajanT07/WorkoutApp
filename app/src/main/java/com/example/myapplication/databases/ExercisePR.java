package com.example.myapplication.databases;

public class ExercisePR {
    private String date;
    private int prValue;

    public ExercisePR(String date, int prValue) {
        this.date = date;
        this.prValue = prValue;
    }

    public String getDate() {
        return date;
    }

    public int getPrValue() {
        return prValue;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", PR: " + prValue;
    }
}