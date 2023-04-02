package com.hopeland.alerts;

import com.hopeland.alerts.events.eventbus.MongoChangeBus;
import com.hopeland.alerts.handler.DataHandler;
import com.hopeland.alerts.listeners.MongoListener;
import com.hopeland.alerts.mongo.DBManager;
import com.hopeland.alerts.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertsSystem {

    private static AlertsSystem alertsSystem;

    @Getter private Scheduler scheduler;
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
        //System.setProperty("javax.net.ssl.keyStore", "X509-cert-2957358956160653009.pem");
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
        scheduler = new Scheduler();
        dbManager = new DBManager();


        //Start Event Loop
        while (!dbManager.isConfirmed()) {
            try {
                System.out.println("Waiting for db confirmation...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DB connection confirmed!");
        dbManager.getDatabase().getMongoChangeBus().startEventLoop();
    }

    public void setup() {
        registerListeners();
        new DataHandler();
    }

    public void registerListeners() {
        MongoChangeBus mongoChangeBus = dbManager.getDatabase().getMongoChangeBus();
        mongoChangeBus.registerListener(new MongoListener());
    }

    public static AlertsSystem getInstance() {
        return alertsSystem;
    }

}
