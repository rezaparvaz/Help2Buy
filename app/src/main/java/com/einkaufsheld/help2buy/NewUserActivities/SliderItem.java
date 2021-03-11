package com.einkaufsheld.help2buy.NewUserActivities;

public class SliderItem {

    private String description;
    private int mImageResourceId;

    public SliderItem(int imageResourceId){
        this.mImageResourceId = imageResourceId;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResourceId() {
        return mImageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.mImageResourceId = imageResourceId;
    }
}
