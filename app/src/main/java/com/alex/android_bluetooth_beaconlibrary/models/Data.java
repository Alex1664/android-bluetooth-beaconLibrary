package com.alex.android_bluetooth_beaconlibrary.models;

/**
 * Created by alex on 04/12/17.
 */

public class Data {

    private long message;

    public Data() {
    }

    public Data(long message) {
        this.message = message;
    }

    public long getMessage() {
        return message;
    }

    public void setMessage(long message) {
        this.message = message;
    }
}
