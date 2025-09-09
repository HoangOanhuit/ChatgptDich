/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber {
    private static final Map<String, List<Consumer<String>>> listeners = new HashMap<>();

    public static void subscribe(JedisPubSub listener, String channel, String cancel_oi_channel1, String done_oi_channel) {
        new Thread(() -> {
            try (Jedis jedis = RedisConnector.getConnection()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String ch, String message) {
                        System.out.println("nhan duoc tin nhan " + message);
                        notifyListeners(ch, message);
                    }
                }, channel);
            }
        }).start();
    }
    
    public static void addListener(String channel, Consumer<String> listener) {
        listeners.computeIfAbsent(channel, k -> new ArrayList<>()).add(listener);
    }

    private static void notifyListeners(String channel, String message) {
        List<Consumer<String>> channelListeners = listeners.get(channel);
        if (channelListeners != null) {
            for (Consumer<String> listener : channelListeners) {
                listener.accept(message);
            }
        }
    }
    
}
