/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.dao.TableDAO;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.model.WaitingQueue;
import com.mycompany.quanlyquanan.service.TableService;
import com.mycompany.quanlyquanan.service.WaitingQueueService;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class WaitingQueueController {

    private WaitingQueueService service;

    public WaitingQueueController() {
        this.service = new WaitingQueueService();
    }

    public void addWaitingQueue(WaitingQueue queue) {
        try {
            service.addWaiting(queue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WaitingQueue> getAll() {
        try {
            return service.getAllWaiting();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

     public List<WaitingQueue> search(String keyWord) {
        try {
            return service.search(keyWord);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void updateStatus(int id, String status) {
        try {
            service.updateStatus(id, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
     public void updateSuggestTables(int id, String suggestedTable) throws SQLException {
        try {
            service.updateSuggestTables(id, suggestedTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try {
            service.removeWaiting(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLatestQueueNumberToday() {
        return service.getLatestQueueNumberToday();
    }
    
     public Optional<WaitingQueue> getById(int id) {
        
        return service.getById(id);
    }
     
     public boolean editQueue(WaitingQueue queue) {
        return service.editQueue(queue);
    }
     
     
    public String suggestForWaitingCustomer(int people) {
        TableService tableService = new TableService();
    Map<String, List<List<Table>>> suggestions = tableService.suggestTables(people);

    // 1. Nếu có bàn đơn → lấy bàn đầu tiên
    if (suggestions.containsKey("single") && !suggestions.get("single").isEmpty()) {
        List<Table> firstSingle = suggestions.get("single").get(0);
        return firstSingle.get(0).getName(); // Ví dụ: "Bàn 5"
    }

    // 2. Nếu có bàn ghép → lấy tổ hợp đầu tiên
    if (suggestions.containsKey("merge") && !suggestions.get("merge").isEmpty()) {
        List<Table> firstMerge = suggestions.get("merge").get(0);
        return firstMerge.stream()
                .map(Table::getName)
                .collect(Collectors.joining(" + ")); // Ví dụ: "Bàn 2 + Bàn 3"
    }

    // 3. Nếu không có bàn phù hợp
    return null;
}

    
       public boolean autoCancelExpired() throws SQLException {
        return service.autoCancelExpired();

    }
}
