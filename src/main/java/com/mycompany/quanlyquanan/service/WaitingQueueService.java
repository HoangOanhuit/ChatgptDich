/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.WaitingQueueDAO;
import com.mycompany.quanlyquanan.model.WaitingQueue;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Admin
 */
public class WaitingQueueService {

    private WaitingQueueDAO waitingQueueDAO;

    public WaitingQueueService() {
        this.waitingQueueDAO = new WaitingQueueDAO();
    }

    public void addWaiting(WaitingQueue queue) throws SQLException {
        waitingQueueDAO.insert(queue);
    }

    public List<WaitingQueue> getAllWaiting() throws SQLException {
        return waitingQueueDAO.getAll();
    }

    public List<WaitingQueue> search(String keyWord) throws SQLException {
        return waitingQueueDAO.search(keyWord);
    }

    public void updateStatus(int id, String status) throws SQLException {
        waitingQueueDAO.updateStatus(id, status);
    }

    public void updateSuggestTables(int id, String suggestedTable) throws SQLException {
        waitingQueueDAO.updateSuggestTables(id, suggestedTable);
    }

    public void removeWaiting(int id) throws SQLException {
        waitingQueueDAO.delete(id);
    }

    public int getLatestQueueNumberToday() {

        return waitingQueueDAO.getLatestQueueNumberToday();
    }

    public Optional<WaitingQueue> getById(int id) {

        return waitingQueueDAO.getById(id);
    }

    public boolean editQueue(WaitingQueue queue) {
        return waitingQueueDAO.update(queue);
    }

    public boolean autoCancelExpired() throws SQLException {
        return waitingQueueDAO.autoCancelExpired();

    }

}
