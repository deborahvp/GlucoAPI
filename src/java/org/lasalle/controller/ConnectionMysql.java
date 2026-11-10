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
        String host = getEnv("DB_HOST", "127.0.0.1");
        String port = getEnv("DB_PORT", "3306");
        String db_name = getEnv("DB_NAME", "gluco_api");
        String user = getEnv("DB_USER", "root");
        String password = getEnv("DB_PASSWORD", "ROOT");

        // DB_URL permite sobreescribir la URL JDBC completa si se prefiere (p. ej. BD administrada en la nube)
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = "jdbc:mysql://" + host + ":" + port + "/" + db_name;
        }
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

    private String getEnv(String name, String defaultValue){
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
