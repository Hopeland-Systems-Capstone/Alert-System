package com.hopeland.alerts.events;

import com.hopeland.alerts.handler.DataHandler;

public record MongoChangeEvent(int sensorId, DataHandler.DataType dataType, double addedValue) {
}
