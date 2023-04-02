package com.hopeland.alerts.handler;

import com.hopeland.alerts.AlertsSystem;
import com.hopeland.alerts.objects.Sensor;

import java.util.List;
import java.util.stream.DoubleStream;


/**
 * This algorithm detects spikes and dips in measurements on a single sensor basis. (I assume
 * that this is how Paul would like the algorithm to work; it can easily be modified to detect
 * anomalies in the average reading for all sensors.)
 * <p>
 * It will throw an alert in one of four cases:
 * 1. A reading is significantly higher than the average of recent readings.
 * 2. A reading is significantly lower than the average of recent readings.
 * 3. A reading exceeds a determined maximum value for its type of data.
 * 4. A reading falls below a determined minimum value for its type of data.
 * <p>
 * Constants are just estimated values, they should be determined using real world data
 * The max and min values for some measurements may need to fluctuate throughout the day or
 * across the span of a year; e.g. a temperature reading of 80 during the summer isn't cause
 * for alarm, but a reading of 80 during winter would be.
 **/

public abstract class DataHandler {

    protected AlertsSystem alertsSystem;
    protected AlertHandler alertHandler;

    //If a reading is more than 1.5 std deviations from the mean, an alert is thrown
    protected double STD_DEV_BOUNDS = 1.5;

    public DataHandler() {
        this.alertsSystem = AlertsSystem.getInstance();
        this.alertHandler = alertsSystem.getAlertHandler();
    }

    public void runTest(String sensorName, double newValue) {
        if (!Sensor.getSensors().containsKey(sensorName))
            new Sensor(sensorName);
    }

    protected double normalize(double value, List<Double> data) {
        return (value - average(data)) / stdDev(data);
    }

    protected double average(List<Double> data) {
        double sum = 0.0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    protected double stdDev(List<Double> data) {
        DoubleStream doubleStream = data.stream().mapToDouble(Double::doubleValue);
        double mean = doubleStream.average().orElse(Double.NaN);
        double variance = doubleStream.map(x -> Math.pow(x - mean, 2)).average().orElse(Double.NaN);
        return Math.sqrt(variance);
    }

    public enum DataType {
        BATTERY,
        TEMPERATURE,
        HUMIDITY,
        CO2,
        PRESSURE
    }
}
