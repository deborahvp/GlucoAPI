/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.Medida;

/**
 *
 * @author debor
 */
public class ControllerMedida {

    public int insertMedida(Medida medida) throws SQLException {
        String sql = "INSERT INTO medida (idUsuario, glucosa, fechaHora, contexto, notas) "
                   + "VALUES (?, ?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();

        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setInt(1, medida.getIdUsuario());
            pstm.setInt(2, medida.getGlucosa());

            // fechaHora llega como String "yyyy-MM-dd HH:mm:ss"; si viene vacía se usa ahora
            if (medida.getFechaHora() == null || medida.getFechaHora().isBlank()) {
                LocalDateTime ahora = LocalDateTime.now();
                medida.setFechaHora(ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pstm.setTimestamp(3, Timestamp.valueOf(ahora));
            } else {
                pstm.setTimestamp(3, Timestamp.valueOf(medida.getFechaHora()));
            }

            if (medida.getContexto() == null || medida.getContexto().isBlank()) {
                pstm.setNull(4, Types.VARCHAR);
            } else {
                pstm.setString(4, medida.getContexto());
            }

            if (medida.getNotas() == null || medida.getNotas().isBlank()) {
                pstm.setNull(5, Types.VARCHAR);
            } else {
                pstm.setString(5, medida.getNotas());
            }

            int filas = pstm.executeUpdate();

            // Recuperamos el id autogenerado por MySQL (idMedida AUTO_INCREMENT)
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    medida.setIdMedida(rs.getInt(1));
                }
            }

            return filas;
        }
    }

    public List<Medida> getMedidas() throws SQLException {
        String sql = "SELECT * FROM medida ORDER BY fechaHora DESC, idMedida DESC";

        List<Medida> lista = new ArrayList<>();
        ConnectionMysql connMysql = new ConnectionMysql();

        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(fillMedida(rs));
            }
        }

        return lista;
    }

    public List<Medida> getMedidasByUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM medida WHERE idUsuario = ? ORDER BY fechaHora DESC, idMedida DESC";

        List<Medida> lista = new ArrayList<>();
        ConnectionMysql connMysql = new ConnectionMysql();

        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idUsuario);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(fillMedida(rs));
                }
            }
        }

        return lista;
    }

    public Medida fillMedida(ResultSet rs) throws SQLException {
        Medida medida = new Medida();
        medida.setIdMedida(rs.getInt("idMedida"));
        medida.setIdUsuario(rs.getInt("idUsuario"));
        medida.setGlucosa(rs.getInt("glucosa"));

        Timestamp fecha = rs.getTimestamp("fechaHora");
        if (fecha != null) {
            medida.setFechaHora(fecha.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        medida.setContexto(rs.getString("contexto"));
        medida.setNotas(rs.getString("notas"));

        return medida;
    }
}
