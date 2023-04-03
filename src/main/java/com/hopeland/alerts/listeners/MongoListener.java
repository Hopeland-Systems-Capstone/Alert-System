package com.hopeland.alerts.listeners;

import com.hopeland.alerts.AlertsSystem;
import com.hopeland.alerts.events.MongoChangeEvent;
import com.hopeland.alerts.handler.EventHandler;

public class MongoListener {

    private final AlertsSystem alertsSystem;

    public MongoListener() {
        this.alertsSystem = AlertsSystem.getInstance();
    }

    @EventHandler
    public void onMongoChange(MongoChangeEvent event) {
        System.out.println("Mongo change detected | Type: " + event.dataType().name() + ", New Value: " + event.addedValue() + ", Sensor ID: " + event.sensorId());

        int sensorId = event.sensorId();
        double newValue = event.addedValue();

        switch (event.dataType()) {
            case BATTERY -> alertsSystem.getBatteryHandler().runTest(sensorId, newValue);
            case TEMPERATURE -> alertsSystem.getTemperatureHandler().runTest(sensorId, newValue);
            case HUMIDITY -> alertsSystem.getHumidityHandler().runTest(sensorId, newValue);
            case CO2 -> alertsSystem.getCo2Handler().runTest(sensorId, newValue);
            case PRESSURE -> alertsSystem.getPressureHandler().runTest(sensorId, newValue);
        }
    }


}