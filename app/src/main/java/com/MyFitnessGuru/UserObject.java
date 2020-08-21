package com.MyFitnessGuru;

//object class to store the users details

public class UserObject
{
    String emailAddress, password,gender,age,  setting;
    int stepGoal;
    int steps;
    double weight, weightGoal,height;


    public UserObject(String EmailAddress, String Password)
    {
        emailAddress = EmailAddress;
        password = Password;
    }

    public UserObject(String EmailAddress, String Age, double Height, double Weight, String Gender, double WeightGoal, int StepGoal, int Steps, String Setting)
    {
        weightGoal = WeightGoal;
        stepGoal = StepGoal;
        emailAddress = EmailAddress;
        age =  Age;
        height = Height;
        weight = Weight;
        gender = Gender;
        steps = Steps;
        setting = Setting;
    }

    public String getEmailAddress() {return emailAddress;}
    public String getPassword() {return password;}
    public double getHeight() {return height;}
    public double getWeight() {return weight;}
    public String getGender() {return gender;}
    public String getAge() {return age;}
    public double getWeightGoal() {return weightGoal;}
    public int getStepGoal() {return stepGoal;}
    public int getSteps() {return steps;}
    public String getSetting() {return setting;}

    public UserObject(){}
}

