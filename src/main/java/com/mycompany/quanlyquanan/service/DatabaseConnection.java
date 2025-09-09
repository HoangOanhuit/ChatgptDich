/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import java.sql.Connection;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

/**
 *
 * @author Administrator
 */
class DatabaseConnection {

    static Connection getConnection() {
        return DatabaseConnector.getConnection();
    }
    
}
