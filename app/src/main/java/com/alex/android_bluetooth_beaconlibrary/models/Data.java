package com.alex.android_bluetooth_beaconlibrary.models;

/**
 * Created by alex on 04/12/17.
 */

public class Data {

    private String message;

    public Data() {}

    public Data(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
