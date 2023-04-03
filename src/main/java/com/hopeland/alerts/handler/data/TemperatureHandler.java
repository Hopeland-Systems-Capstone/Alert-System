package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TemperatureHandler extends DataHandler {

    // Units for temperature in degrees celsius
    private final double TEMP_MAX = 33;
    private final double TEMP_MIN = 0;
    private final long TEMP_TIMESCALE = 8;

    public TemperatureHandler() {
        super();
    }

    @Override
    public void runTest(int sensorID, double newValue) {
        super.runTest(sensorID, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("sensor_id", sensorID)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(TEMP_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("temperature", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    data.add(reading.getDouble("value"));
                }
            }

            double currentTemperatureNormalized = normalize(newValue, data);
            System.out.println("RESULT: Normalized temperature: " + currentTemperatureNormalized + ", Standard Deviation Bounds: " + STD_DEV_BOUNDS);
            if (currentTemperatureNormalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.TEMPERATURE, sensorID, "A spike in temperature detected");
            }
            if (currentTemperatureNormalized <= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.TEMPERATURE, sensorID, "A drop in temperature detected");
            }
            if (newValue >= TEMP_MAX) {
                alertHandler.alert(DataType.TEMPERATURE, sensorID, "Temperature exceeds normal range");
            }
            if (newValue <= TEMP_MIN) {
                alertHandler.alert(DataType.TEMPERATURE, sensorID, "Temperature below normal range");
            }
        }
    }

}
