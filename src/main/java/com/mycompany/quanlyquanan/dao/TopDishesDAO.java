package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.OrderItem;
import com.mycompany.quanlyquanan.model.TopDishes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TopDishesDAO {

    private final java.sql.Connection con;

    public TopDishesDAO() {
        this.con = com.mycompany.quanlyquanan.utils.DatabaseConnector.getConnection();
    }

    private TopDishes mapResultSet(ResultSet rs) throws SQLException {
        return new TopDishes(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("total")
        );
    }

    public List<TopDishes> getDayTopDishes(LocalDate date) {
        String sql = "SELECT d.id, d.name ,SUM(o.quantity) AS total " +
                "FROM order_items o " +
                "JOIN dishes d ON o.dish_id = d.id " +
                "JOIN orders od ON o.order_id = od.id " +
                "WHERE DATE(od.created_at) = ? " +
                "GROUP BY d.id, d.name, DATE(od.created_at) " +
                "ORDER BY total DESC LIMIT 9;";
        List<TopDishes> dishes = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
            return dishes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TopDishes> getMonthTopDishes(int month, int year) {
        String sql = "SELECT d.id, d.name, SUM(o.quantity) AS total " +
                "FROM order_items o " +
                "JOIN dishes d ON o.dish_id = d.id " +
                "JOIN orders od ON o.order_id = od.id " +
                "WHERE MONTH(od.created_at) = ? AND YEAR(od.created_at) = ? " +
                "GROUP BY d.id, d.name, MONTH(od.created_at), YEAR(od.created_at) " +
                "ORDER BY total DESC LIMIT 9;";
        List<TopDishes> dishes = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, month);
            preparedStatement.setInt(2, year);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
            return dishes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<TopDishes> getYearTopDishes(int year) {
        String sql = "SELECT d.id, d.name, SUM(o.quantity) AS total " +
                "FROM order_items o " +
                "JOIN dishes d ON o.dish_id = d.id " +
                "JOIN orders od ON o.order_id = od.id " +
                "WHERE YEAR(od.created_at) = ? " +
                "GROUP BY d.id, d.name, YEAR(od.created_at) " +
                "ORDER BY total DESC LIMIT 9;";
        List<TopDishes> dishes = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, year);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
            return dishes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
