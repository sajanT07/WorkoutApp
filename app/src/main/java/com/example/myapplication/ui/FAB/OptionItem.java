package com.example.myapplication.ui.FAB;

public class OptionItem {
    public String label;
    public int iconRes;
    private Runnable methodAction;
    private Class<?> activityClass;

    // Constructor for methods (Runnable)
    public OptionItem(String label, int iconRes, Runnable methodAction) {
        this.label = label;
        this.iconRes = iconRes;
        this.methodAction = methodAction;
        this.activityClass = null;  // No activity to open
    }

    // Constructor for activities (Class)
    public OptionItem(String label, int iconRes, Class<?> activityClass) {
        this.label = label;
        this.iconRes = iconRes;
        this.activityClass = activityClass;
        this.methodAction = null;  // No method to run
    }

    public String getLabel() {
        return label;
    }

    public int getIconRes() {
        return iconRes;
    }

    public Runnable getMethodAction() {
        return methodAction;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }

    // Check if the option is for a method or activity
    public boolean isMethodOption() {
        return methodAction != null;
    }

    public boolean isActivityOption() {
        return activityClass != null;
    }
}