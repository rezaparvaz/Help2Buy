package com.einkaufsheld.help2buy.CustomerApp.home;

import android.graphics.Bitmap;

import com.einkaufsheld.help2buy.CustomerApp.home.model.Result;

public class ResultsWithPhoto {

    private Result mResult;
    private Bitmap mBitmap;

    public ResultsWithPhoto(Result result, Bitmap bitmap){
    this.mResult = result;
    this.mBitmap = bitmap;
    }

    public ResultsWithPhoto() {

    }


    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public Result getResult() {
        return mResult;
    }

    public void setResult(Result result) {
        this.mResult = result;
    }
}


