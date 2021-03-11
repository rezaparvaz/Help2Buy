package com.einkaufsheld.help2buy;


import android.os.Parcel;
import android.os.Parcelable;

import com.einkaufsheld.help2buy.CustomerApp.ItemDetails;

import java.util.ArrayList;

public class ShoppingOrderDetails implements Parcelable {

    private String mSupplierUid;
    private String mCustomerUid;
    private Double mSupplierLat;
    private Double mSupplierLng;
    private Double mCustomerLat;
    private Double mCustomerLng;
    private Double mSupermarketLat;
    private Double mSupermarketLng;
    private String mSupermarketName;
    private Double mSupermarketRating;
    private String mSupermarketOpenNow;
    private String mSupermarketDistance;
    private String mSupermarketAddress;
    private ArrayList<ItemDetails> mShoppingList;
    private String mOrderStatus;
    private String mOrderPlaceTime;
    private String mOrderAcceptTime;
    private String mOrderEndTime;
    private String mShoppingListSuppliersPrice;
    private String mShoppingListCustomersPrice;
    private String mDeliveryFee;
    private String mOrderID;
    private String mPlaceID;
    private String mInCartTime;


    public ShoppingOrderDetails(Double supplierLat, Double supplierLng, String orderID, String supplierUid, String customerUid, Double customerLat,
                                Double customerLng, Double supermarketLat, Double supermarketLng, String supermarketName, String orderStatus, String inCartTime,
                                String orderPlaceTime, String orderAcceptTime, String orderEndTime, String shoppingListSuppliersPrice, String shoppingListCustomersPrice ,String deliveryFee,
                                ArrayList<ItemDetails> shoppingList, Double supermarketRating, String supermarketOpenNow, String supermarketDistance,
                                String supermarketAddress, String placeID) {
        this.mInCartTime = inCartTime;
        this.mOrderID = orderID;
        this.mSupplierLat = supplierLat;
        this.mSupplierLng = supplierLng;
        this.mSupplierUid = supplierUid;
        this.mCustomerUid = customerUid;
        this.mCustomerLat = customerLat;
        this.mCustomerLng = customerLng;
        this.mSupermarketLat = supermarketLat;
        this.mSupermarketLng = supermarketLng;
        this.mSupermarketName = supermarketName;
        this.mShoppingList = shoppingList;
        this.mOrderStatus = orderStatus;
        this.mOrderPlaceTime = orderPlaceTime;
        this.mOrderAcceptTime = orderAcceptTime;
        this.mOrderEndTime = orderEndTime;
        this.mShoppingListSuppliersPrice = shoppingListSuppliersPrice;
        this.mShoppingListCustomersPrice = shoppingListCustomersPrice;
        this.mDeliveryFee = deliveryFee;
        this.mSupermarketRating = supermarketRating;
        this.mSupermarketOpenNow = supermarketOpenNow;
        this.mSupermarketDistance = supermarketDistance;
        this.mSupermarketAddress = supermarketAddress;
        this.mPlaceID = placeID;


    }
    public ShoppingOrderDetails() {
    }


    protected ShoppingOrderDetails(Parcel in) {
        mSupplierUid = in.readString();
        mCustomerUid = in.readString();
        if (in.readByte() == 0) {
            mSupplierLat = null;
        } else {
            mSupplierLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            mSupplierLng = null;
        } else {
            mSupplierLng = in.readDouble();
        }
        if (in.readByte() == 0) {
            mCustomerLat = null;
        } else {
            mCustomerLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            mCustomerLng = null;
        } else {
            mCustomerLng = in.readDouble();
        }
        if (in.readByte() == 0) {
            mSupermarketLat = null;
        } else {
            mSupermarketLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            mSupermarketLng = null;
        } else {
            mSupermarketLng = in.readDouble();
        }
        mSupermarketName = in.readString();
        if (in.readByte() == 0) {
            mSupermarketRating = null;
        } else {
            mSupermarketRating = in.readDouble();
        }
        mSupermarketOpenNow = in.readString();
        mSupermarketDistance = in.readString();
        mSupermarketAddress = in.readString();
        mShoppingList = in.createTypedArrayList(ItemDetails.CREATOR);
        mOrderStatus = in.readString();
        mOrderPlaceTime = in.readString();
        mOrderAcceptTime = in.readString();
        mOrderEndTime = in.readString();
        mShoppingListSuppliersPrice = in.readString();
        mShoppingListCustomersPrice = in.readString();
        mDeliveryFee = in.readString();
        mOrderID = in.readString();
        mPlaceID = in.readString();
        mInCartTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSupplierUid);
        dest.writeString(mCustomerUid);
        if (mSupplierLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mSupplierLat);
        }
        if (mSupplierLng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mSupplierLng);
        }
        if (mCustomerLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mCustomerLat);
        }
        if (mCustomerLng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mCustomerLng);
        }
        if (mSupermarketLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mSupermarketLat);
        }
        if (mSupermarketLng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mSupermarketLng);
        }
        dest.writeString(mSupermarketName);
        if (mSupermarketRating == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(mSupermarketRating);
        }
        dest.writeString(mSupermarketOpenNow);
        dest.writeString(mSupermarketDistance);
        dest.writeString(mSupermarketAddress);
        dest.writeTypedList(mShoppingList);
        dest.writeString(mOrderStatus);
        dest.writeString(mOrderPlaceTime);
        dest.writeString(mOrderAcceptTime);
        dest.writeString(mOrderEndTime);
        dest.writeString(mShoppingListSuppliersPrice);
        dest.writeString(mShoppingListCustomersPrice);
        dest.writeString(mDeliveryFee);
        dest.writeString(mOrderID);
        dest.writeString(mPlaceID);
        dest.writeString(mInCartTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShoppingOrderDetails> CREATOR = new Creator<ShoppingOrderDetails>() {
        @Override
        public ShoppingOrderDetails createFromParcel(Parcel in) {
            return new ShoppingOrderDetails(in);
        }

        @Override
        public ShoppingOrderDetails[] newArray(int size) {
            return new ShoppingOrderDetails[size];
        }
    };

    public Double getSupplierLat() { return mSupplierLat; }
    public Double getSupplierLng() { return mSupplierLng; }
    public String getInCartTime() { return mInCartTime; }
    public String getPlaceID() { return mPlaceID; }
    public String getOrderID() { return mOrderID; }
    public String getSupplierUid() {return mSupplierUid;}
    public String getCustomerUid() {return mCustomerUid;}
    public String getOrderStatus() { return mOrderStatus; }
    public Double getCustomerLat() {return mCustomerLat;}
    public Double getCustomerLng() {return mCustomerLng;}
    public Double getSupermarketLat() {return mSupermarketLat;}
    public Double getSupermarketLng() {return mSupermarketLng;}
    public String getSupermarketName() {return mSupermarketName;}
    public Double getSupermarketRating() {return mSupermarketRating;}
    public String getSupermarketOpenNow() {return mSupermarketOpenNow;}
    public String getSupermarketDistance() {return mSupermarketDistance;}
    public String getSupermarketAddress() {return mSupermarketAddress;}
    public ArrayList<ItemDetails> getShoppingList() {return mShoppingList;}
    public String getOrderPlaceTimeTime() { return mOrderPlaceTime; }
    public String getOrderAcceptTime() { return mOrderAcceptTime; }
    public String getOrderEndTime() { return mOrderEndTime; }
    public String getDeliveryFee() { return mDeliveryFee; }
    public String getShoppingListCustomersPrice() { return mShoppingListCustomersPrice; }
    public String getShoppingListSuppliersPrice() { return mShoppingListSuppliersPrice; }

    public void setSupplierLat(Double supplierLat) { this.mSupplierLat = supplierLat; }
    public void setSupplierLng(Double supplierLng) { this.mSupplierLng = supplierLng; }
    public void setInCartTime(String inCartTime) { this.mInCartTime = inCartTime; }
    public void setPlaceID(String placeID) { this.mPlaceID = placeID; }
    public void setDeliveryFee(String deliveryFee) { this.mDeliveryFee = deliveryFee; }
    public void setOrderEndTime(String orderEndTime) { this.mOrderEndTime = orderEndTime; }
    public void setOrderAcceptTime(String orderAcceptTime) { this.mOrderAcceptTime = orderAcceptTime; }
    public void setOrderID(String orderID) { this.mOrderID = orderID;}
    public void setOrderStatus(String orderStatus) { this.mOrderStatus = orderStatus;}
    public void setSupplierUid(String supplierUid) {this.mSupplierUid = supplierUid;}
    public void setCustomerUid(String customerUid) { this.mCustomerUid = customerUid; }
    public void setCustomerLat(Double customerLat) {this.mCustomerLat = customerLat;}
    public void setCustomerLng(Double customerLng) {this.mCustomerLng = customerLng;}
    public void setSupermarketLat(Double supermarketLat) {this.mSupermarketLat = supermarketLat;}
    public void setSupermarketLng(Double supermarketLng) {this.mSupermarketLng = supermarketLng;}
    public void setSupermarketName(String supermarketName) { this.mSupermarketName = supermarketName; }
    public void setSupermarketRating(Double supermarketRating) { this.mSupermarketRating = supermarketRating; }
    public void setSupermarketOpenNow(String supermarketOpenNow) { this.mSupermarketOpenNow = supermarketOpenNow; }
    public void setSupermarketDistance(String supermarketDistance) { this.mSupermarketDistance = supermarketDistance; }
    public void setSupermarketAddress(String supermarketAddress) { this.mSupermarketAddress = supermarketAddress; }
    public void setShoppingList(ArrayList<ItemDetails> mShoppingList) { this.mShoppingList = mShoppingList; }
    public void setOrderPlaceTime(String orderPlaceTime) { this.mOrderPlaceTime = orderPlaceTime; }

    public void setShoppingListCustomersPrice(String shoppingListCustomersPrice) { this.mShoppingListCustomersPrice = shoppingListCustomersPrice; }

    public void setShoppingListSuppliersPrice(String shoppingListSuppliersPrice) { this.mShoppingListSuppliersPrice = shoppingListSuppliersPrice; }
}

