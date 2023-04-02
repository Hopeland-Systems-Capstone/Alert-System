package com.hopeland.alerts;

import com.hopeland.alerts.events.eventbus.MongoChangeBus;
import com.hopeland.alerts.handler.AlertHandler;
import com.hopeland.alerts.handler.data.*;
import com.hopeland.alerts.listeners.MongoListener;
import com.hopeland.alerts.mongo.DBManager;
import com.hopeland.alerts.scheduler.Scheduler;
import lombok.Getter;

import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class AlertsSystem {

    private static AlertsSystem alertsSystem;

    private Scheduler scheduler;
    private DBManager dbManager;
    private AlertHandler alertHandler;

    private BatteryHandler batteryHandler;
    private TemperatureHandler temperatureHandler;
    private HumidityHandler humidityHandler;
    private CO2Handler co2Handler;
    private PressureHandler pressureHandler;

    public void enable() {
        alertsSystem = this;
        Runtime.getRuntime().addShutdownHook(new Thread(this::disable));
        initDB();
    }

    public void disable() {
        System.out.println("Exiting");
        dbManager.getDatabase().getMongoChangeBus().endEventLoop();
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
        alertHandler = new AlertHandler();

        batteryHandler = new BatteryHandler();
        temperatureHandler = new TemperatureHandler();
        humidityHandler = new HumidityHandler();
        co2Handler = new CO2Handler();
        pressureHandler = new PressureHandler();

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
    }

    public void registerListeners() {
        MongoChangeBus mongoChangeBus = dbManager.getDatabase().getMongoChangeBus();
        mongoChangeBus.registerListener(new MongoListener());
    }

    public static AlertsSystem getInstance() {
        return alertsSystem;
    }

}
