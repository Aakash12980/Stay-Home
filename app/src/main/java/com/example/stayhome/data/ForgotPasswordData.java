package com.example.stayhome.data;

public class ForgotPasswordData {
    private String email;
    private int tempKey;

    public ForgotPasswordData() {
    }

    public ForgotPasswordData(String email, int tempKey) {
        this.email = email;
        this.tempKey = tempKey;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTempKey() {
        return tempKey;
    }

    public void setTempKey(int tempKey) {
        this.tempKey = tempKey;
    }
}
