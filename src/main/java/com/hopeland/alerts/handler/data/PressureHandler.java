package com.hopeland.alerts.handler.data;

import com.hopeland.alerts.handler.DataHandler;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PressureHandler extends DataHandler {

    // Units for barometric pressure in hPa
    private final double BARO_PRESSURE_MAX = 1050;
    private final double BARO_PRESSURE_MIN = 950;
    private final long BARO_PRESSURE_TIMESCALE = 10;

    public PressureHandler() {
        super();
    }

    @Override
    public void runTest(String sensorName, double newValue) {
        super.runTest(sensorName, newValue);
        Document sensor = alertsSystem.getDbManager().getDatabase().getSensors().find(Filters.eq("name", sensorName)).first();
        if (sensor != null) {
            long cutoffTime = Instant.now().minus(Duration.ofHours(BARO_PRESSURE_TIMESCALE)).toEpochMilli();
            List<Double> data = new ArrayList<>();
            for (Document reading : sensor.getList("pressure", Document.class)) {
                if (reading.getLong("time") >= cutoffTime) {
                    data.add(reading.getDouble("value"));
                }
            }

            double currentBaroPressureNormalized = normalize(newValue, data);
            if (currentBaroPressureNormalized >= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.PRESSURE, sensorName, "A spike in barometric pressure detected");
            }
            if (currentBaroPressureNormalized <= STD_DEV_BOUNDS) {
                alertHandler.alert(DataType.PRESSURE, sensorName, "A drop in barometric pressure detected");
            }
            if (newValue >= BARO_PRESSURE_MAX) {
                alertHandler.alert(DataType.PRESSURE, sensorName, "Barometric pressure exceeds normal range");
            }
            if (newValue <= BARO_PRESSURE_MIN) {
                alertHandler.alert(DataType.PRESSURE, sensorName, "Barometric pressure below normal range");
            }
        }
    }
}
