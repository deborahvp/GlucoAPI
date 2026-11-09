/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;


import java.util.List;
import java.util.ArrayList;
import org.lasalle.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author debor
 */
public class ControllerUsuario  {
    
        public List<Usuario> getUsuario() throws SQLException{
        List<Usuario> lista = new ArrayList<>();

        //Declaramos el query a ejecutar
        String sql = "SELECT * FROM usuario";
        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();


        while(rs.next()){
            lista.add(fillUsuario(rs));
        }
        rs.close();
        connMysql.close();
        conn.close();
        return lista;
    }
    
    public int insertUsuario(Usuario user) throws SQLException{
        String sql = "INSERT INTO usuario (nombre, primerApellido, segundoApellido, email) "
                   + "VALUES (?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();

        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, user.getNombre());
            pstm.setString(2, user.getPrimerApellido());
            pstm.setString(3, user.getSegundoApellido()); // null -> NULL en MySQL
            pstm.setString(4, user.getEmail());

            int filas = pstm.executeUpdate();

            // Recuperamos el id autogenerado por MySQL (idUsuario AUTO_INCREMENT)
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setIdUsuario(rs.getInt(1));
                }
            }

            return filas;
        }
    }

    public Usuario fillUsuario(ResultSet rs) throws SQLException{
        Usuario user = new Usuario();
        user.setIdUsuario(rs.getInt("idUsuario"));
        user.setNombre(rs.getString("nombre"));
        user.setPrimerApellido(rs.getString("primerApellido"));
        user.setSegundoApellido(rs.getString("segundoApellido"));
        user.setEmail(rs.getString("email"));
        
        return user;
    }
    
}
