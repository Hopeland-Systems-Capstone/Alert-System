package com.hopeland.alerts.events.eventbus;

import com.hopeland.alerts.events.MongoChangeEvent;
import com.hopeland.alerts.handler.EventHandler;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoChangeBus {

    private final Set<Object> listeners = new HashSet<>();

    public void init(MongoCursor<ChangeStreamDocument<Document>> cursor) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("Starting eventbus");
            while (cursor.hasNext()) {
                SwingUtilities.invokeLater(() -> notifyListeners(new MongoChangeEvent(cursor.next().getFullDocument())));
            }
        });
        executor.shutdown();
    }

    public void registerListener(Object listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Object listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(MongoChangeEvent event) {
        listeners.forEach(listener -> {
            Method[] methods = listener.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(EventHandler.class)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0].equals(MongoChangeEvent.class)) {
                        try {
                            method.invoke(listener, event);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}

