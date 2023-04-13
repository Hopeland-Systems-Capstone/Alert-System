package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CO2Handler extends DataHandler {

    // Units for CO2 levels in ppm
    private final double CO2_MAX = 1200;
    private final double CO2_MIN = 0;
    private final long CO2_TIMESCALE = 12;

    public CO2Handler() {
        super();
    }

    @Override
    public void runTest(int sensorID, double newValue) {
        super.runTest(sensorID, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("sensor_id", sensorID)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(CO2_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("co2", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    Object value = reading.get("value");
                    data.add(value instanceof Integer ? ((Integer) value).doubleValue() : (Double) value);
                }
            }

            double currentCO2Normalized = normalize(newValue, data);
            System.out.println("RESULT: Normalized CO2 Level: " + currentCO2Normalized + ", Standard Deviation Bounds: " + STD_DEV_BOUNDS);
            if (currentCO2Normalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.CO2, sensorID, "A spike in CO2 level detected");
            }
            if (currentCO2Normalized <= -1 * STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.CO2, sensorID, "A drop in CO2 level detected");
            }
            if (newValue >= CO2_MAX) {
                alertHandler.alert(DataType.CO2, sensorID, "CO2 level exceeds normal range");
            }
            if (newValue <= CO2_MIN) {
                alertHandler.alert(DataType.CO2, sensorID, "CO2 level below normal range");
            }
        }
    }

}
