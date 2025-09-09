package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.TopDishes;
import com.mycompany.quanlyquanan.service.TopDishesService;

import java.util.List;

public class TopDishesController {
    TopDishesService service = new TopDishesService();

    public TopDishesService getService() {
        return service;
    }

    public List<TopDishes> getMonthTopDishes(int month, int year) {
        return service.getMonthTopDishes(month, year);
    }

    public List<TopDishes> getYearTopDishes(int year) {
        return service.getYearTopDishes(year);
    }

    public List<TopDishes> getDayTopDishes(java.time.LocalDate date) {
        return service.getDayTopDishes(date);
    }
}
