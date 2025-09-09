/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;

import redis.clients.jedis.Jedis;

/**
 * 
 *
 * @author Admin
 */
public class RedisPublisher {
     public static void publish(String channel, String message) {
        try (Jedis jedis = RedisConnector.getConnection()) {
            jedis.publish(channel, message);
        }
    }
}
