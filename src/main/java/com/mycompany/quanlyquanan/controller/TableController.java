/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.service.TableService;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class TableController {

    private TableService service;

    public TableController() {
        this.service = new TableService();
    }

    public List<Table> getAllTables() {
        try {
            return service.getAllTables();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Table> getAllWithGroupCapacity() {
        try {
            return service.getAllWithGroupCapacity();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Table> search(String keyword) {
        try {
            return service.search(keyword);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Table> searchWithGroupCapacity(String keyword) {
        try {
            return service.searchWithGroupCapacity(keyword);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Table getTableById(int id) {
        try {
            return service.getTableById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addTable(Table t) {
        try {
            return service.addTable(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTable(Table t) {
        try {
            return service.updateTable(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTableStatus(int id, String newStatus) {

        try {
            return service.updateTableStatus(id, newStatus);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean deleteTable(int id) {
        try {
            return service.deleteTable(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeStatus(int tableId, String status) {
        try {
            return service.changeTableStatus(tableId, status);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, List<List<Table>>> suggestTables(int people) {
        Map<String, List<List<Table>>> suggestions = service.suggestTables(people);
        System.out.println("Single table suggestions:");
        suggestions.getOrDefault("single", Collections.emptyList())
                .forEach(list -> System.out.println(list.get(0).getId() + " (capacity " + list.get(0).getCapacity() + ")"));

        System.out.println("Merge table suggestions:");
        suggestions.getOrDefault("merge", Collections.emptyList())
                .forEach(list -> System.out.println(
                list.stream().map(t -> "Table " + t.getId()).collect(Collectors.joining(" + "))
                + " = " + list.stream().mapToInt(Table::getCapacity).sum()
        ));

        return suggestions;
    }

    public StringBuilder suggestTablesString(int people) {
        Map<String, List<List<Table>>> suggestions = service.suggestTables(people);
        StringBuilder sb = new StringBuilder();

        if (suggestions.containsKey("single") && !suggestions.get("single").isEmpty()) {
            // Có bàn đơn
            suggestions.get("single").forEach(list -> {
                Table t = list.get(0);
                sb
                        .append(t.getName())
                        .append(" - sức chứa : ")
                        .append(t.getCapacity())
                        .append("\n");
            });
        } else if (suggestions.containsKey("merge") && !suggestions.get("merge").isEmpty()) {
            // Không có single, dùng bàn ghép
            suggestions.get("merge").forEach(list -> {
                String merged = list.stream()
                        .map(t -> t.getName() + " ( " + t.getCapacity() + " )")
                        .collect(Collectors.joining(" + "));
                int totalCapacity = list.stream().mapToInt(Table::getCapacity).sum();

                sb.append(merged)
                        .append(" = ")
                        .append(totalCapacity)
                        .append("\n");
            });
        } else {
            // Không có cả 2
            sb.append("Không có bàn phù hợp\n");
        }

        return sb;
    }

    public void updateStatusByName(String tableName, String status) throws SQLException {
        service.updateStatusByName(tableName, status);

    }

  
}
