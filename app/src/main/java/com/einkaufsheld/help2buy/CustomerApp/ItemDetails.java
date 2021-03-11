package com.einkaufsheld.help2buy.CustomerApp;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemDetails implements Parcelable {

    private String mQuantityMain;
    private String mPieceOrKgMain;
    private String mDetailsMain;
    private String mQuantityOptional;
    private String mPieceOrKgOptional;
    private String mDetailsOptional;
    private String mBioOrCheapestOrNone;

    public ItemDetails(String quantityMain, String pieceOrKgMain, String detailsMain, String quantityOptional, String pieceOrKgOptional, String detailsOptional, String bioOrCheapestOrNone){
    this.mQuantityMain = quantityMain;
    this.mPieceOrKgMain = pieceOrKgMain;
    this.mDetailsMain = detailsMain;
    this.mQuantityOptional = quantityOptional;
    this.mPieceOrKgOptional = pieceOrKgOptional;
    this.mDetailsOptional = detailsOptional;
    this.mBioOrCheapestOrNone = bioOrCheapestOrNone;

    }

    public ItemDetails(){

    }

    protected ItemDetails(Parcel in) {
        mQuantityMain = in.readString();
        mPieceOrKgMain = in.readString();
        mDetailsMain = in.readString();
        mQuantityOptional = in.readString();
        mPieceOrKgOptional = in.readString();
        mDetailsOptional = in.readString();
        mBioOrCheapestOrNone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mQuantityMain);
        dest.writeString(mPieceOrKgMain);
        dest.writeString(mDetailsMain);
        dest.writeString(mQuantityOptional);
        dest.writeString(mPieceOrKgOptional);
        dest.writeString(mDetailsOptional);
        dest.writeString(mBioOrCheapestOrNone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ItemDetails> CREATOR = new Creator<ItemDetails>() {
        @Override
        public ItemDetails createFromParcel(Parcel in) {
            return new ItemDetails(in);
        }

        @Override
        public ItemDetails[] newArray(int size) {
            return new ItemDetails[size];
        }
    };

    public String getQuantityMain() {
        return mQuantityMain;
    }
    public String getPieceOrKgMain() {
        return mPieceOrKgMain;
    }
    public String getDetailsMain() {
        return mDetailsMain;
    }
    public String getQuantityOptional() {
        return mQuantityOptional;
    }
    public String getPieceOrKgOptional() {
        return mPieceOrKgOptional;
    }
    public String getDetailsOptional() {
        return mDetailsOptional;
    }
    public String getBioOrCheapestOrNone() {
        return mBioOrCheapestOrNone;
    }

    public void setQuantityMain(String quantityMain) { this.mQuantityMain = quantityMain; }
    public void setPieceOrKgMain(String pieceOrKgMain) { this.mPieceOrKgMain = pieceOrKgMain; }
    public void setDetailsMain(String detailsMain) { this.mDetailsMain = detailsMain; }
    public void setQuantityOptional(String quantityOptional) { this.mQuantityOptional = quantityOptional; }
    public void setPieceOrKgOptional(String pieceOrKgOptional) { this.mPieceOrKgOptional = pieceOrKgOptional; }
    public void setDetailsOptional(String detailsOptional) { this.mDetailsOptional = detailsOptional; }
    public void setBioOrCheapestOrNone(String bioOrCheapestOrNone) { this.mBioOrCheapestOrNone = bioOrCheapestOrNone; }


}


