package com.hopeland.alerts.objects;

import com.hopeland.alerts.handler.DataHandler;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class Sensor {

    @Getter private static HashMap<Integer, Sensor> sensors = new HashMap<>();

    private int id;
    private HashMap<DataHandler.DataType, Long> lastAlert;

    public Sensor(int id) {
        this.id = id;
        this.lastAlert = new HashMap<>();
        for (DataHandler.DataType dataType : DataHandler.DataType.values()) {
            lastAlert.put(dataType, -1L);
        }
        sensors.put(id, this);
    }

}
