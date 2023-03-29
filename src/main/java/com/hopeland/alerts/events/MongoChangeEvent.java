package com.hopeland.alerts.events;

import lombok.Getter;
import org.bson.Document;

public record MongoChangeEvent(@Getter Document document) {

}
