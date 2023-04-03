package com.hopeland.alerts.mongo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hopeland.alerts.AlertsSystem;
import com.hopeland.alerts.events.eventbus.MongoChangeBus;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MongoDB {

    private final AlertsSystem alertsSystem;

    //private String connectionURL = "mongodb+srv://hopelandsystems.dobnt5r.mongodb.net/?authSource=%24external&authMechanism=MONGODB-X509&retryWrites=true&w=majority";
    private final String connectionURL = "mongodb+srv://java:RHm8RK3XTtftFln1@hopelandsystems.dobnt5r.mongodb.net/?retryWrites=true&w=majority";
    private final String databaseName = "hopelandsystems";

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    @Getter private MongoChangeBus mongoChangeBus;

    @Getter private MongoCollection<Document> sensors;
    @Getter private MongoCollection<Document> users;
    @Getter private MongoCollection<Document> alerts;

    public MongoDB() {
        alertsSystem = AlertsSystem.getInstance();
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setPriority(3).setNameFormat("hopeland-db-executor-thread-%d").build()
    );

    public void connect() {
        run(() -> {
            try {

                mongoClient = MongoClients.create(connectionURL);
                CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
                mongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);

                sensors = mongoDatabase.getCollection("sensors");
                users = mongoDatabase.getCollection("users");
                alerts = mongoDatabase.getCollection("alerts");

                (mongoChangeBus = new MongoChangeBus()).init(sensors.watch().iterator());
                System.out.println("Successfully connected to MONGO database");
                alertsSystem.getDbManager().setConfirmed(true);

                SwingUtilities.invokeLater(alertsSystem::setup);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Unable to connect to MONGO database");
                alertsSystem.getDbManager().setConfirmed(false);
            }
        });
    }

    public void run(Runnable runnable) {
        executor.execute(runnable);
    }

    public boolean isConnected() {
        return mongoClient != null;
    }

    public void disconnect() {
        if (!isConnected())
            return;

        mongoClient.close();
        mongoClient = null;
    }
}
