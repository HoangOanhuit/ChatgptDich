/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.controller.WaitingQueueController;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Admin
 */
public class QueueAutoCancelService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final WaitingQueueController queueController ;

    public QueueAutoCancelService(WaitingQueueController queueController) {
        this.queueController = queueController;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if(queueController.autoCancelExpired()){
                  RedisPublisher.publish("waitingQueue", "updated" );
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS); // kiểm tra mỗi phút
    }

    public void stop() {
        scheduler.shutdown();
    }
}
