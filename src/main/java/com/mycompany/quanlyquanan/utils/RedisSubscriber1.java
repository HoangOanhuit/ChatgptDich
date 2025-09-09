/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber1 {
    
    public void subscribe(JedisPubSub listener, String ... channels) {
        new Thread(() -> {
            try (Jedis jedis = RedisConnector.getConnection()) {
                jedis.subscribe(listener, channels);
            }
        }).start();
    }
}
