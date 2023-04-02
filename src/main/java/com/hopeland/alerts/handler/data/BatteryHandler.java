package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;

public class BatteryHandler extends DataHandler {

    // Units for battery in percent
    private final double BATTERY_MIN = 10;

    public BatteryHandler() {
        super();
    }

    @Override
    public void runTest(int sensorID, double newValue) {
        super.runTest(sensorID, newValue);
        if (newValue <= BATTERY_MIN) {
            alertHandler.alert(DataType.BATTERY, sensorID, "Battery is low");
        }
    }

}
