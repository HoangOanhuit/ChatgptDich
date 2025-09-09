/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.TableDAO;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class TableService {

    private TableDAO dao;

    public TableService() {
        this.dao = new TableDAO();
    }

    /**
     *
     * @return @throws SQLException
     */
    public List<Table> getAllTables() throws SQLException {
        return dao.getAll();
    }

    public List<Table> getAllWithGroupCapacity() throws SQLException {
        return dao.getAllWithGroupCapacity();
    }

    public List<Table> search(String keyword) throws SQLException {
        return dao.search(keyword);
    }

    public List<Table> searchWithGroupCapacity(String keyword) throws SQLException {
        return dao.searchWithGroupCapacity(keyword);
    }

    public boolean updateTableStatus(int id, String newStatus) throws SQLException {
       
        boolean rs = dao.updateTableStatus(id, newStatus);
         RedisPublisher.publish("tableChanel", "updated");
        return rs;
    }

    public void updateStatusByName(String tableName, String status) throws SQLException {
        dao.updateStatusByName(tableName, status);
        RedisPublisher.publish("tableChanel", "updated");
        
        

    }

    public Table getTableById(int id) throws SQLException {
        return dao.getById(id);
        
        
    }

    public boolean addTable(Table t) throws SQLException {   
         boolean rs = dao.insert(t);
         RedisPublisher.publish("tableChanel", "updated");
        return rs;
    }

    public boolean updateTable(Table t) throws SQLException {
        
        boolean rs = dao.update(t);
         RedisPublisher.publish("tableChanel", "updated");
        return rs;
    }

    public boolean deleteTable(int id) throws SQLException {
       
        boolean rs = dao.delete(id);
         RedisPublisher.publish("tableChanel", "updated");
        return rs;
              
    }

    // Cập nhật trạng thái bàn
    public boolean changeTableStatus(int tableId, String newStatus) throws SQLException {
        Table t = dao.getById(tableId);
        if (t != null) {
            t.setStatus(newStatus);
            boolean rs = dao.update(t);
            RedisPublisher.publish("tableChanel", "updated");
            return rs;
        }
        return false;
    }

    public boolean hasChildTables(int parentTableId) {
        return dao.hasChildTables(parentTableId);
    }

    public void mergeTables(int rootId, List<Integer> childIds, Integer employeeId) throws Exception {
        if (childIds == null || childIds.isEmpty()) {
            throw new IllegalArgumentException("Cần chọn ít nhất 1 bàn để ghép.");
        }

        // bỏ trùng + bỏ root nếu lỡ có
        childIds = childIds.stream()
                .filter(id -> id != rootId)
                .distinct()
                .toList();

        if (childIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách bàn con rỗng sau khi lọc.");
        }

        try (Connection con = DatabaseConnector.getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                // Khóa bản ghi để tránh race condition
                Table root = dao.findByIdForUpdate(con, rootId);
                if (root == null) {
                    throw new IllegalStateException("Không tìm thấy bàn gốc.");
                }
                if (root.getMergedIntoId() != null) {
                    throw new IllegalStateException("Bàn gốc đang là bàn con; không thể làm gốc.");
                }
                if (!root.isCanMerge()) {
                    throw new IllegalStateException("Bàn gốc không cho phép ghép.");
                }

                List<Table> children = dao.findByIdsForUpdate(con, childIds);
                if (children.size() != childIds.size()) {
                    throw new IllegalStateException("Một số bàn con không tồn tại.");
                }

                for (Table t : children) {
                    if (!t.isCanMerge()) {
                        throw new IllegalStateException("Bàn " + t.getName() + " không cho phép ghép.");
                    }
                    if (t.getMergedIntoId() != null) {
                        throw new IllegalStateException("Bàn " + t.getName() + " đã được ghép vào bàn khác.");
                    }
                    if (!"available".equalsIgnoreCase(t.getStatus())) {
                        throw new IllegalStateException("Bàn " + t.getName() + " không ở trạng thái available.");
                    }
                    if (root.getLocationGroup() != null
                            && !root.getLocationGroup().equals(t.getLocationGroup())) {
                        throw new IllegalStateException("Bàn " + t.getName() + " khác khu " + root.getLocationGroup());
                    }
                    boolean hasChild = dao.hasChildTables(t.getId());
                    if (hasChild) {
                        throw new IllegalStateException("Bàn " + t.getName() + " đang là bàn gốc.");
                    }

                }

                // cập nhật từng bàn con
                for (Table t : children) {
                    dao.updateAsMergedChild(con, t.getId(), rootId, employeeId);
                }

                con.commit();

                RedisPublisher.publish("tableChanel", "updated");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true); // luôn trả lại trạng thái mặc định
            }
        }
    }

    public void unmergeChildren(int rootId, List<Integer> childIds) throws Exception {
        if (childIds == null || childIds.isEmpty()) {
            return;
        }

        try (Connection con = DatabaseConnector.getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                dao.unmergeChildren(con, rootId, childIds);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Map<String, List<List<Table>>> suggestTables(int people) {
        // Giả sử dao.getAvailableTables() trả về danh sách các bàn đang trống
        List<Table> availableTables = dao.getAvailableTables();
        Map<String, List<List<Table>>> result = new HashMap<>();

        // 1. Bàn đơn phù hợp (Không thay đổi)
        List<Table> singleTableSuggestions = availableTables.stream()
                .filter(t -> t.getCapacity() >= people)
                .sorted(Comparator.comparingInt(t -> t.getCapacity() - people))
                .collect(Collectors.toList());

        if (!singleTableSuggestions.isEmpty()) {
            result.put("single", singleTableSuggestions.stream()
                    .map(Collections::singletonList)
                    .collect(Collectors.toList()));
            return result;
        }

        // 2. Bàn ghép 
        List<List<Table>> mergeSuggestions = new ArrayList<>();

        // Lọc trước các bàn có thể ghép và nhóm chúng theo khu vực
        Map<String, List<Table>> mergeableTablesByGroup = availableTables.stream()
                .filter(Table::isCanMerge) // Chỉ lấy các bàn có canMerge == true
                .collect(Collectors.groupingBy(Table::getLocationGroup));

        // Chạy hàm tổ hợp trên từng nhóm bàn cùng khu vực
        for (List<Table> groupOfTables : mergeableTablesByGroup.values()) {
            for (int r = 2; r <= 4; r++) { // Tìm tổ hợp 2, 3, và 4 bàn
                if (groupOfTables.size() >= r) {
                    combine(groupOfTables, r, 0, new ArrayList<>(), mergeSuggestions, people);
                }
            }
        }

        // Sắp xếp tất cả các phương án ghép tìm được theo độ chênh lệch gần nhất
        mergeSuggestions.sort(Comparator.comparingInt(
                list -> Math.abs(list.stream().mapToInt(Table::getCapacity).sum() - people)
        ));

        // Chỉ lấy 5 kết quả tốt nhất (có thể tùy chỉnh số lượng)
        result.put("merge", mergeSuggestions.stream().limit(5).collect(Collectors.toList()));
        return result;
    }

    private void combine(List<Table> tables, int r, int start, List<Table> current,
            List<List<Table>> results, int people) {
        if (current.size() == r) {
            int totalCapacity = current.stream().mapToInt(Table::getCapacity).sum();
            if (totalCapacity >= people) {
                results.add(new ArrayList<>(current));
            }
            return;
        }
        for (int i = start; i < tables.size(); i++) {
            current.add(tables.get(i));
            combine(tables, r, i + 1, current, results, people);
            current.remove(current.size() - 1);
        }
    }

}
