package com.hopeland.alerts.handler;

import com.hopeland.alerts.AlertsSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

/*

This algorithm detects spikes and dips in measurements on a single sensor basis. (I assume
that this is how Paul would like the algorithm to work; it can easily be modified to detect
anomolies in the average reading for all sensors.)

It will throw an alert in one of four cases:
	1. A reading is significantly higher than the average of recent readings.
	2. A reading is significantly lower than the average of recent readings.
	3. A reading exceeds a determined maximum value for its type of data.
	4. A reading falls below a determined minimum value for its type of data.

*/

public class DataHandler {

    private AlertHandler alertHandler;

    /*
	These constants are just estimated values, they should be determined using real world data
	The max and min values for some measurements may need to fluctuate throughout the day or
	across the span of a year; e.g. a temperature reading of 80 during the summer isn't cause
	for alarm, but a reading of 80 during winter would be.
	*/

    //If a reading is more than 1.5 std deviations from the mean, an alert is thrown
    double STD_DEV_BOUNDS = 1.5;

    //BAROMETRIC PRESSURE
    //Units for barometric pressure in hPa
    double BARO_PRESSURE_MAX = 1050;
    double BARO_PRESSURE_MIN = 950;
    double BARO_PRESSURE_TIMESCALE = 10;
    //All readings within this number of hours are used to calculate average

    //TEMPERATURE
    //Units in degrees C
    double TEMP_MAX = 33;
    double TEMP_MIN = 0;
    double TEMP_TIMESCALE = 8;

    //CO2 LEVEL
    //Units in ppm
    double CO2_MAX = 1200;
    double CO2_MIN = 0;
    double CO2_TIMESCALE = 12;

    //HUMIDITY LEVEL
    //Units are a proportion of max humidity
    double HUMIDITY_MAX = 1;
    double HUMIDITY_MIN = 0.5;
    double HUMIDITY_TIMESCALE = 8;


    //WATERLEVEL
    //Units in feet
    double WATER_LEVEL_MAX = 5;
    double WATER_LEVEL_MIN = 0;
    double WATER_LEVEL_TIMESCALE = 12;


    public DataHandler() {
        this.alertHandler = AlertsSystem.getInstance().getAlertHandler();
    }

    public void runTest(DataType dataType, double newValue) {
        switch (dataType) {
            case PRESSURE -> {
                List<Double> data = new ArrayList<>();
                //TODO: Get list of data based on TIMESCALE

                double currentBaroPressureNormalized = currentBaroPressureNormalized(newValue, data);
                if (currentBaroPressureNormalized >= STD_DEV_BOUNDS) {
                    alertHandler.alert("A spike in barometric pressure detected");
                }
                if (currentBaroPressureNormalized <= STD_DEV_BOUNDS) {
                    alertHandler.alert("A drop in barometric pressure detected");
                }
                if (newValue >= BARO_PRESSURE_MAX) {
                    alertHandler.alert("Barometric pressure exceeds normal range");
                }
                if (newValue <= BARO_PRESSURE_MIN) {
                    alertHandler.alert("Barometric pressure below normal range");
                }
            }
        }
    }

    public double currentBaroPressureNormalized(double baroPressure, List<Double> data) {
        return (baroPressure - baroPressureAverage(data)) / baroPressureStdDev(data);
    }

    public double baroPressureAverage(List<Double> data) {
        double sum = 0.0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    public double baroPressureStdDev(List<Double> data) {
        DoubleStream doubleStream = data.stream().mapToDouble(Double::doubleValue);
        double mean = doubleStream.average().orElse(Double.NaN);
        double variance = doubleStream.map(x -> Math.pow(x - mean, 2)).average().orElse(Double.NaN);
        return Math.sqrt(variance);
    }

    private enum DataType {
        PRESSURE,
        TEMPERATURE,
        CO2_LEVEL,
        HUMIDITY,
        WATER_LEVEL
    }

}
