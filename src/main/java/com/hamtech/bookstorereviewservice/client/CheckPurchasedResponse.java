package com.hamtech.bookstorereviewservice.client;

public class CheckPurchasedResponse {
    private boolean isPurchased;
    private String message;

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

