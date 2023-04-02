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
    public void runTest(String sensorName, double newValue) {
        super.runTest(sensorName, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("name", sensorName)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(CO2_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("co2", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    data.add(reading.getDouble("value"));
                }
            }

            double currentCO2Normalized = normalize(newValue, data);
            if (currentCO2Normalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.CO2, sensorName, "A spike in CO2 level detected");
            }
            if (currentCO2Normalized <= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.CO2, sensorName, "A drop in CO2 level detected");
            }
            if (newValue >= CO2_MAX) {
                alertHandler.alert(DataType.CO2, sensorName, "CO2 level exceeds normal range");
            }
            if (newValue <= CO2_MIN) {
                alertHandler.alert(DataType.CO2, sensorName, "CO2 level below normal range");
            }
        }
    }

}
