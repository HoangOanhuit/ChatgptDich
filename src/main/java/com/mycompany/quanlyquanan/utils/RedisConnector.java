/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 * @author Admin
 */
public class RedisConnector {
     private static JedisPool jedisPool;

    static {
        // Chỉ khởi tạo 1 lần
        jedisPool = new JedisPool("localhost", 6379);
    }

    // Lấy một kết nối Redis
    public static Jedis getConnection() {
        return jedisPool.getResource();
    }

    // Đóng toàn bộ pool (nếu cần)
    public static void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
