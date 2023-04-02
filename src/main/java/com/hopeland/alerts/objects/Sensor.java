package com.hopeland.alerts.objects;

import com.hopeland.alerts.handler.DataHandler;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class Sensor {

    @Getter private static HashMap<String, Sensor> sensors = new HashMap<>();

    private String name;
    private HashMap<DataHandler.DataType, Long> lastAlert;

    public Sensor(String name) {
        this.name = name;
        this.lastAlert = new HashMap<>();
        for (DataHandler.DataType dataType : DataHandler.DataType.values()) {
            lastAlert.put(dataType, -1L);
        }
        sensors.put(name, this);
    }

}
