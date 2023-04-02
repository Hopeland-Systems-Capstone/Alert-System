package com.hopeland.alerts.handler;

import com.hopeland.alerts.AlertsSystem;

/*

This algorithm detects spikes and dips in measurements on a single sensor basis. (I assume
that this is how Paul would like the algorithm to work; it can easily be modified to detect
anomolies in the average reading for all sensors.)

It will throw an alert in one of four cases:
	1. A reading is significantly higher than the average of recent readings.
	2. A reading is significantly lower than the average of recent readings.
	3. A reading exceeds a determined maximum value for its type of data.
	4. A reading falls below a determined minimum value for its type of data.

*/

public class AlertHandler {

    private AlertsSystem alertsSystem;

    public AlertHandler() {
        this.alertsSystem = AlertsSystem.getInstance();
    }

    //TODO: Send alerts
    public void alert(String string) {

    }

}
