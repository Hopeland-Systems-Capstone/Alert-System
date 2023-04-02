package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;

public class BatteryHandler extends DataHandler {

    // Units for battery in percent
    private final double BATTERY_MIN = 10;

    public BatteryHandler() {
        super();
    }

    @Override
    public void runTest(String sensorName, double newValue) {
        super.runTest(sensorName, newValue);
        if (newValue <= BATTERY_MIN) {
            alertHandler.alert(DataType.BATTERY, sensorName, "Battery is low");
        }
    }

}
