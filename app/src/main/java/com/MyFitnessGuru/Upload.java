package com.MyFitnessGuru;

import android.net.Uri;

//object class created in order to upload and download an image to the firebase

public class Upload
{
    private Uri mImageUrl;

    public Upload(){
        //empty constructor
    }

    public Upload(Uri MImageUrl){
        mImageUrl = MImageUrl;
    }

    public Uri getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(Uri mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
