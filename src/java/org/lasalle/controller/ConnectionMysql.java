/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author debor
 */
public class ConnectionMysql {
    Connection conn;
    
    public Connection open(){
        String user = "root";
        String password = "ROOT";
        String db_name = "gluco_api";
        String url = "jdbc:mysql://127.0.0.1:3306/"+db_name;
        String parametros = "?useSSL=false&useUnicode=true&characterEncoding=utf-8";
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url+parametros,user,password);
            return conn;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }       
            
    }
    
    public void close(){
        if(conn != null){
            try {
                conn.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
