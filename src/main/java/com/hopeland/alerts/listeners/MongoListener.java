package com.hopeland.alerts.listeners;

import com.hopeland.alerts.events.MongoChangeEvent;
import com.hopeland.alerts.handler.EventHandler;

public class MongoListener {

    @EventHandler
    public void onMongoChange(MongoChangeEvent event) {
        System.out.println("Mongo change detected");
    }

}