package com.MyFitnessGuru;

//class used to upload a group of values (current date, steps and weight) into the firebase
//and to download the same group from a firebase folder and into the application

public class DateObject
{
    String Steps;
    String Date;
    String weightCurrent;

    public DateObject(String date, String weightCurrent, String steps) {
        Date = date;
        this.weightCurrent = weightCurrent;
        Steps = steps;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getWeightCurrent() {
        return weightCurrent;
    }

    public void setWeightCurrent(String weightCurrent) {
        this.weightCurrent = weightCurrent;
    }

    public String getSteps() {
        return Steps;
    }

    public void setSteps(String steps) {
        Steps = steps;
    }
}
