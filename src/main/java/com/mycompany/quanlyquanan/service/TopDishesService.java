package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.TopDishesDAO;
import com.mycompany.quanlyquanan.model.TopDishes;

import java.time.LocalDate;
import java.util.List;

public class TopDishesService {
    TopDishesDAO dao;

    public TopDishesService() {
        dao = new TopDishesDAO();
    }

    public List<TopDishes> getDayTopDishes(LocalDate date) {
        return dao.getDayTopDishes(date);
    }

    public List<TopDishes> getYearTopDishes(int year) {
        return dao.getYearTopDishes(year);
    }

    public List<TopDishes> getMonthTopDishes(int month, int year) {
        return dao.getMonthTopDishes(month, year);
    }
}
