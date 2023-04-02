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
    public void runTest(String sensorName, double newValue) {
        super.runTest(sensorName, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("name", sensorName)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(HUMIDITY_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("humidity", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    data.add(reading.getDouble("value"));
                }
            }

            double currentHumidityNormalized = normalize(newValue, data);
            if (currentHumidityNormalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.HUMIDITY, sensorName, "A spike in humidity detected");
            }
            if (currentHumidityNormalized <= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.HUMIDITY, sensorName, "A drop in humidity detected");
            }
            if (newValue >= HUMIDITY_MAX) {
                alertHandler.alert(DataType.HUMIDITY, sensorName, "Humidity exceeds normal range");
            }
            if (newValue <= HUMIDITY_MIN) {
                alertHandler.alert(DataType.HUMIDITY, sensorName, "Humidity below normal range");
            }
        }
    }

}
