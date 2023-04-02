package com.hopeland.alerts.mongo;

import com.hopeland.alerts.AlertsSystem;
import lombok.Getter;

@Getter
public class DBManager {

    private final AlertsSystem alertsSystem;
    private final MongoDB database;
    private boolean confirmed = false;

    public DBManager() {
        alertsSystem = AlertsSystem.getInstance();
        database = new MongoDB();
        database.connect();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConnected() {
        return database != null && database.isConnected();
    }
}

