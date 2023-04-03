package com.hopeland.alerts.handler;

import com.hopeland.alerts.AlertsSystem;
import com.hopeland.alerts.objects.Sensor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class AlertHandler {

    private AlertsSystem alertsSystem;

    private final int MINUTES_BETWEEN_ALERTS = 1; // Rate limits alerts at 1 alert per data type per minute
    private final int MINUTES_BETWEEN_BATTERY_ALERTS = 60; // Rate limits battery alerts at 1 per hour

    public AlertHandler() {
        this.alertsSystem = AlertsSystem.getInstance();
    }

    public void alert(DataHandler.DataType dataType, int sensorID, String alert) {
        Sensor sensor = Sensor.getSensors().get(sensorID);
        long lastAlert = sensor.getLastAlert().get(dataType);
        if (System.currentTimeMillis() - lastAlert > (dataType == DataHandler.DataType.BATTERY ? MINUTES_BETWEEN_BATTERY_ALERTS : MINUTES_BETWEEN_ALERTS) * 60000) {
            sensor.getLastAlert().put(dataType, System.currentTimeMillis());
            send(sensor, alert);
        }
    }

    public void send(Sensor sensor, String alert) {
        System.out.println(alert);
        FindIterable<Document> users = alertsSystem.getDbManager().getDatabase().getUsers().find(Filters.eq("sensors", sensor.getId()));
        for (Document user : users) {
            //TODO: Add alert to alerts Array for user
        }
    }

}
