package com.hopeland.alerts.events.eventbus;

import com.hopeland.alerts.events.MongoChangeEvent;
import com.hopeland.alerts.handler.EventHandler;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoChangeBus {

    private final Set<Object> listeners = new HashSet<>();
    private final BlockingQueue<MongoChangeEvent> eventQueue = new LinkedBlockingQueue<>();
    private boolean running;

    public void init(MongoCursor<ChangeStreamDocument<Document>> cursor) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("Starting eventbus");
            while (cursor.hasNext()) {
                try {
                    eventQueue.put(new MongoChangeEvent(cursor.next().getFullDocument()));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    public void registerListener(Object listener) {
        System.out.println("Registered listener " + listener.getClass());
        listeners.add(listener);
    }

    public void unregisterListener(Object listener) {
        listeners.remove(listener);
    }

    public void startEventLoop() {
        System.out.println("Started event loop");
        this.running = true;
        while (this.running) {
            try {
                MongoChangeEvent event = eventQueue.take();
                listeners.forEach(listener -> {
                    Method[] methods = listener.getClass().getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(EventHandler.class)) {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            if (parameterTypes.length == 1 && parameterTypes[0].equals(MongoChangeEvent.class)) {
                                try {
                                    method.invoke(listener, event);
                                } catch (IllegalAccessException | InvocationTargetException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void endEventLoop() {
        this.running = false;
    }

}

