package com.hopeland.alerts;

import com.hopeland.alerts.handler.DataHandler;
import com.hopeland.alerts.mongo.DBManager;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertsSystem {

    private static AlertsSystem alertsSystem;

    @Getter @Setter private DBManager dbManager;

    public void enable() {
        alertsSystem = this;
        Runtime.getRuntime().addShutdownHook(new Thread(this::disable));
        initDB();
    }

    public void disable() {
        System.out.println("Exiting");
        if (dbManager.isConnected() && dbManager.isConfirmed())
            dbManager.getDatabase().disconnect();
    }

    public void initDB() {
        System.setProperty("DEBUG.GO", "true");
        System.setProperty("DB.TRACE", "true");
        System.setProperty("javax.net.ssl.keyStore", "classpath:X509-cert-2957358956160653009.pem");
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
        dbManager = new DBManager();
    }

    public void setup() {
        new DataHandler();
    }

    public static AlertsSystem getInstance() {
        return alertsSystem;
    }

}
