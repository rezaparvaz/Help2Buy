package com.einkaufsheld.help2buy.CustomerApp.map;

import com.google.android.gms.maps.model.Marker;

public class MapMarker {

    private String mOrderID;
    private Marker mMarker;

    public MapMarker(String orderID, Marker marker){
    this.mOrderID = orderID;
    this.mMarker = marker;
    }

    public MapMarker() {

    }

    public Marker getMarker() {
        return mMarker;
    }

    public String getOrderID() {
        return mOrderID;
    }

    public void setMarker(Marker marker) {
        this.mMarker = marker;
    }

    public void setOrderID(String orderID) {
        this.mOrderID = orderID;
    }
}


