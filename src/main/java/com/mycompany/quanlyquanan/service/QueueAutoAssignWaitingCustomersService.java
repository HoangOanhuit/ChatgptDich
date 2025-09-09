/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.controller.TableController;
import com.mycompany.quanlyquanan.controller.WaitingQueueController;
import com.mycompany.quanlyquanan.dao.WaitingQueueDAO;
import com.mycompany.quanlyquanan.model.WaitingQueue;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Admin
 */
public class QueueAutoAssignWaitingCustomersService {
     private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
     private FPTTTSService voice = new FPTTTSService();
    private final WaitingQueueController controller;

    public QueueAutoAssignWaitingCustomersService( WaitingQueueController controller) {
        
        this.controller = controller;
    }

    // Hàm bắt đầu chạy tự động
    public void startAutoAssign() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                autoAssignWaitingCustomers();
                 RedisPublisher.publish("waitingQueue", "updated" );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 20, TimeUnit.SECONDS); // chạy ngay và lặp lại mỗi 30s
    }

    // Hàm gán bàn cho khách chờ
private void autoAssignWaitingCustomers() {
    // Lấy tất cả khách hàng đang ở trạng thái waiting
    var waitingList = controller.getAll();
    int countAssigned =0;
    for (WaitingQueue queue : waitingList) {
        if (!"waiting".equals(queue.getStatus())) {
            continue; // bỏ qua khách không ở trạng thái waiting
        }

        String suggestedTable = controller.suggestForWaitingCustomer(queue.getGuestCount());

        if (suggestedTable == null) {
            System.out.println("⚠️ Không có bàn phù hợp cho khách "
                    + queue.getCustomerName() + " (" + queue.getGuestCount() + " người).");
            continue;
        }

        // Gán bàn cho khách
        queue.setSuggestedTables(suggestedTable);
        try {
            controller.updateSuggestTables(queue.getId(), suggestedTable);
            countAssigned++;
            System.out.println("✅ Đã gợi ý bàn " + suggestedTable
                    + " cho khách " + queue.getCustomerName()
                    + " (" + queue.getGuestCount() + " người).");
        } catch (SQLException ex) {
            System.err.println("❌ Lỗi khi cập nhật bàn cho khách "
                    + queue.getCustomerName() + ": " + ex.getMessage());
        }
        
        if(countAssigned > 0){
        voice.speak("Gợi ý bàn thành công");
        
        }
    }
}

    // Hàm dừng scheduler
    public void stopAutoAssign() {
        scheduler.shutdown();
    }
}
