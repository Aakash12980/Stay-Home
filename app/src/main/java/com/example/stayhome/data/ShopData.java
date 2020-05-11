package com.example.stayhome.data;

import java.util.List;

public class ShopData {
    private String shopName;
    private String shopGenre;
    private String shopLoc;
    private List<String > latLng;
    private String uid;
    private String contact;
    private boolean active;
    private double distance;
    private String openTime = "12:00";
    private String closeTime = "12:05";

    public ShopData() {
    }



    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopGenre() {
        return shopGenre;
    }

    public void setShopGenre(String shopGenre) {
        this.shopGenre = shopGenre;
    }

    public String getShopLoc() {
        return shopLoc;
    }

    public void setShopLoc(String shopLoc) {
        this.shopLoc = shopLoc;
    }

    public List<String> getLatLng() {
        return latLng;
    }

    public void setLatLng(List<String> latLng) {
        this.latLng = latLng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
