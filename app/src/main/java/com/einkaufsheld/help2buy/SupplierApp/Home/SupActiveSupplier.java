package com.einkaufsheld.help2buy.SupplierApp.Home;

public class SupActiveSupplier {

    private String supplierUid;
    private Double supplierLat;
    private Double supplierLng;


    public SupActiveSupplier(String supplierUid, Double supplierLat, Double supplierLng){
    this.supplierUid = supplierUid;
    this.supplierLat = supplierLat;
    this.supplierLng = supplierLng;
    }

    public SupActiveSupplier() {

    }

    public Double getSupplierLat() {
        return supplierLat;
    }

    public String getSupplierUid() {
        return supplierUid;
    }

    public Double getSupplierLng() { return supplierLng; }

    public void setSupplierLat(Double supplierLat) { this.supplierLat = supplierLat; }

    public void setSupplierLng(Double supplierLng) { this.supplierLng = supplierLng; }

    public void setSupplierUid(String supplierUid) {
        this.supplierUid = supplierUid;
    }
}


