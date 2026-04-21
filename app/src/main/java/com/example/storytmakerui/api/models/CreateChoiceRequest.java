package com.example.storytmakerui.api.models;

public class CreateChoiceRequest {
    private String option1Text;
    private String option2Text;
    private int durationInMinutes;

    public CreateChoiceRequest() {}

    public CreateChoiceRequest(String option1Text, String option2Text, int durationInMinutes) {
        this.option1Text = option1Text;
        this.option2Text = option2Text;
        this.durationInMinutes = durationInMinutes;
    }

    public String getOption1Text() {
        return option1Text;
    }

    public void setOption1Text(String option1Text) {
        this.option1Text = option1Text;
    }

    public String getOption2Text() {
        return option2Text;
    }

    public void setOption2Text(String option2Text) {
        this.option2Text = option2Text;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}
