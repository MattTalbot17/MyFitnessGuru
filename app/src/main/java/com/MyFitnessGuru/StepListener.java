package com.MyFitnessGuru;

//taken from Gadget Saints
//availabel at: http://www.gadgetsaint.com/android/create-pedometer-step-counter-android/#.XcFVoOgzbIU

// Will listen to step alerts
public interface StepListener {

    public void step(long timeNs);

}