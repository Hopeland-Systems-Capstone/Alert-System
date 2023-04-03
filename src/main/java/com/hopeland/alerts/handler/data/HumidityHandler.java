package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HumidityHandler extends DataHandler {

    // Units are a proportion of max humidity
    private final double HUMIDITY_MAX = 1;
    private final double HUMIDITY_MIN = 0.5;
    private final long HUMIDITY_TIMESCALE = 8;

    public HumidityHandler() {
        super();
    }

    @Override
    public void runTest(int sensorID, double newValue) {
        super.runTest(sensorID, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("sensor_id", sensorID)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(HUMIDITY_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("humidity", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    data.add(reading.getDouble("value"));
                }
            }

            double currentHumidityNormalized = normalize(newValue, data);
            System.out.println("RESULT: Normalized humidity: " + currentHumidityNormalized + ", Standard Deviation Bounds: " + STD_DEV_BOUNDS);
            if (currentHumidityNormalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.HUMIDITY, sensorID, "A spike in humidity detected");
            }
            if (currentHumidityNormalized <= -1 * STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.HUMIDITY, sensorID, "A drop in humidity detected");
            }
            if (newValue >= HUMIDITY_MAX) {
                alertHandler.alert(DataType.HUMIDITY, sensorID, "Humidity exceeds normal range");
            }
            if (newValue <= HUMIDITY_MIN) {
                alertHandler.alert(DataType.HUMIDITY, sensorID, "Humidity below normal range");
            }
        }
    }

}
