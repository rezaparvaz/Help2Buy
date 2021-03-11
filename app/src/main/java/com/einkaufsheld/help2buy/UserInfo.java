package com.einkaufsheld.help2buy;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
class UserInfo {

    private String mUserUid;
    private String mUserName;
    private String mUserEmail;
    private int mProfilePictureResourceId;
    private String mUserPhoneNumber;
    private int mUserIsCustomer;
    private int mUserIsSupplier;
    private int mUserIsStoreOwner;



    public UserInfo (String userUid, String userName, String userEmail, int userProfilePictureResourceId, String userPhoneNumber,  int userIsCustomer, int userIsSupplier, int userIsStoreOwner) {
        this.mUserUid = userUid;
        this.mUserEmail = userName;
        this.mUserEmail = userEmail;
        this.mProfilePictureResourceId = userProfilePictureResourceId;
        this.mUserPhoneNumber = userPhoneNumber;
        this.mUserIsCustomer = userIsCustomer;
        this.mUserIsSupplier = userIsSupplier;
        this.mUserIsStoreOwner = userIsStoreOwner;
    }

    public UserInfo() {

    }

    public String getUserUid(){ return mUserUid;}
    public void setUserUid(String userUid){this.mUserUid = userUid;}

    public String getUserName(){ return mUserName;}
    public void setUserName(String userName){this.mUserName = userName;}

    public String getUserEmail(){ return mUserEmail;}
    public void setUserEmail(String userEmail){this.mUserEmail = userEmail;}

    public int getUserProfilePictureResourceId(){ return mProfilePictureResourceId;}
    public void setUserProfilePictureResourceId(int userProfilePictureResourceId){this.mProfilePictureResourceId = userProfilePictureResourceId;}

    public String getUserPhoneNumber(){ return mUserPhoneNumber;}
    public void setUserPhoneNumber(String userPhoneNumber){this.mUserPhoneNumber = userPhoneNumber;}

    public int getUserIsCustomer(){ return mUserIsCustomer;}
    public void setUserIsCustomer(int userIsCustomer){this.mUserIsCustomer = userIsCustomer;}

    public int getUserIsSupplier(){ return mUserIsSupplier;}
    public void setUserIsSupplier(int userIsSupplier){this.mUserIsSupplier = userIsSupplier;}

    public int getUserIsStoreOwner(){ return mUserIsStoreOwner;}
    public void setUserIsStoreOwner(int userIsStoreOwner){this.mUserIsStoreOwner = userIsStoreOwner;}




}