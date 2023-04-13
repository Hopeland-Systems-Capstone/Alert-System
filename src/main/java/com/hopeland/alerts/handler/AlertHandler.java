package com.hopeland.alerts.handler;

import com.hopeland.alerts.AlertsSystem;
import com.hopeland.alerts.objects.Sensor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.json.JsonObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AlertHandler {

    private AlertsSystem alertsSystem;

    private final int MINUTES_BETWEEN_ALERTS = 1; // Rate limits alerts at 1 alert per data type per minute
    private final int MINUTES_BETWEEN_BATTERY_ALERTS = 60; // Rate limits battery alerts at 1 per hour

    public AlertHandler() {
        this.alertsSystem = AlertsSystem.getInstance();
    }

    public void alert(DataHandler.DataType dataType, int sensorID, String alert) {
        Sensor sensor = Sensor.getSensors().get(sensorID);
        long lastAlert = sensor.getLastAlert().get(dataType);
        if (System.currentTimeMillis() - lastAlert > (dataType == DataHandler.DataType.BATTERY ? MINUTES_BETWEEN_BATTERY_ALERTS : MINUTES_BETWEEN_ALERTS) * 60000) {
            sensor.getLastAlert().put(dataType, System.currentTimeMillis());

            //POST /alerts?key=val&title=val&alert=val&associated_sensor=val
            int alertID = -1;
            try {
                String apiKey = "9178ea6e1bfb55f9a26edbb1f292e82d";
                String title = dataType.name().charAt(0) + dataType.name().substring(1).toLowerCase() + " Alert";
                String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
                String encodedAlert = URLEncoder.encode(alert, StandardCharsets.UTF_8.toString());
                String requestUrl = "http://localhost:3000/alerts?key=" + apiKey + "&title=" + encodedTitle + "&alert=" + encodedAlert + "&associated_sensor=" + sensorID;

                HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                JsonObject jsonResponse = new JsonObject(responseBody);
                alertID = jsonResponse.toBsonDocument().getInt32("alert_id").intValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (alertID == -1) {
                System.out.println("Creation of alert was unsuccessful");
                return;
            }

            send(sensor, alertID);
        }
    }

    public void send(Sensor sensor, int alertID) {
        FindIterable<Document> users = alertsSystem.getDbManager().getDatabase().getUsers().find(Filters.eq("sensors", sensor.getId()));
        for (Document user : users) {

            //PUT /users/:user_id/alert/:alert_id?key=val
            try {
                String apiKey = "9178ea6e1bfb55f9a26edbb1f292e82d";
                int user_id = user.getInteger("user_id");
                String requestUrl = "http://localhost:3000/users/" + user_id + "/alert/" + alertID + "?key=" + apiKey;

                HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                JsonObject jsonResponse = new JsonObject(responseBody);
                System.out.println(jsonResponse.toBsonDocument().getString("message").getValue());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

}
