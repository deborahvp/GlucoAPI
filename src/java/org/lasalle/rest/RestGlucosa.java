/*

* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license

* Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template

*/

package org.lasalle.rest;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lasalle.controller.ControllerMedida;
import org.lasalle.controller.ControllerUsuario;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;
 
/**
*/

@Path("glucosa")
public class RestGlucosa {
    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar(){
        String respuesta = """
                           {"response" : "Hola desde Java"}
                            """;
        return Response.ok(respuesta).build();        

    }
    @Path("saludarPersonalizado")
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarPersonalizado(
            @QueryParam("nombre") String nombre
    ){
        String respuesta = """
                           {"response" : "Hola %s desde java"}
                           """;
        respuesta = String.format(respuesta, nombre);
        return Response.ok(respuesta).build();
    }
    
    @Path("getAllUsuarios")
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuario(){
        ControllerUsuario cu = new ControllerUsuario();
        List<Usuario> lista = new ArrayList<>();
        Gson gson = new Gson();
        String out = "";
        try{
            lista = cu.getUsuario();
            out = gson.toJson(lista);
            
        } catch (Exception e){
            e.printStackTrace();
            out = """
                  {"result": "Lista vacía"}
                  
                  """;
        
        }
        
        return Response.ok(out).build();
    }

    @Path("createUsuario")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("email") String email) {

        Gson gson = new Gson();
        Map<String, Object> cuerpo = new LinkedHashMap<>();

        // Validación básica de campos obligatorios
        if (nombre == null || nombre.isBlank()
                || primerApellido == null || primerApellido.isBlank()
                || email == null || email.isBlank()) {

            cuerpo.put("error", "nombre, primerApellido y email son obligatorios");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }

        // Límites reales de las columnas en MySQL
        if (nombre.trim().length() > 40
                || primerApellido.trim().length() > 40
                || (segundoApellido != null && segundoApellido.trim().length() > 40)
                || email.trim().length() > 50) {

            cuerpo.put("error", "nombre y apellidos máximo 40 caracteres, email máximo 50");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setPrimerApellido(primerApellido.trim());
        usuario.setSegundoApellido(segundoApellido == null || segundoApellido.isBlank()
                ? null
                : segundoApellido.trim());
        usuario.setEmail(email.trim().toLowerCase());

        try {
            ControllerUsuario cu = new ControllerUsuario();
            int filas = cu.insertUsuario(usuario);

            if (filas == 0) {
                cuerpo.put("error", "No se pudo registrar el usuario");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(gson.toJson(cuerpo))
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(usuario))
                    .build();

        } catch (SQLException e) {
            e.printStackTrace();

            // SQLState 23000 => violación de llave única (email duplicado)
            if ("23000".equals(e.getSQLState())) {
                cuerpo.put("error", "El email ya está registrado");
                return Response.status(Response.Status.CONFLICT)
                        .entity(gson.toJson(cuerpo))
                        .build();
            }

            cuerpo.put("error", "Error al guardar el usuario");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }
    }

    @Path("createMedida")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMedida(
            @FormParam("idUsuario") Integer idUsuario,
            @FormParam("glucosa") Integer glucosa,
            @FormParam("fechaHora") String fechaHora,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas) {

        Gson gson = new Gson();
        Map<String, Object> cuerpo = new LinkedHashMap<>();

        // Campos obligatorios (idUsuario y glucosa son NOT NULL en la BD)
        if (idUsuario == null || idUsuario <= 0 || glucosa == null || glucosa <= 0) {
            cuerpo.put("error", "idUsuario y glucosa son obligatorios y mayores que 0");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }

        // Límites reales de las columnas en MySQL
        if ((contexto != null && contexto.trim().length() > 40)
                || (notas != null && notas.trim().length() > 100)) {

            cuerpo.put("error", "contexto máximo 40 caracteres, notas máximo 100");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }

        // fechaHora es opcional: si no llega se registra la fecha/hora actual
        String fechaNormalizada = null;
        if (fechaHora != null && !fechaHora.isBlank()) {
            try {
                fechaNormalizada = normalizarFecha(fechaHora.trim());
            } catch (DateTimeParseException e) {
                cuerpo.put("error", "fechaHora inválida, usa el formato yyyy-MM-dd HH:mm:ss");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(gson.toJson(cuerpo))
                        .build();
            }
        }

        Medida medida = new Medida();
        medida.setIdUsuario(idUsuario);
        medida.setGlucosa(glucosa);
        medida.setFechaHora(fechaNormalizada);
        medida.setContexto(contexto == null || contexto.isBlank() ? null : contexto.trim());
        medida.setNotas(notas == null || notas.isBlank() ? null : notas.trim());

        try {
            ControllerMedida cm = new ControllerMedida();
            int filas = cm.insertMedida(medida);

            if (filas == 0) {
                cuerpo.put("error", "No se pudo registrar la medida");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(gson.toJson(cuerpo))
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(medida))
                    .build();

        } catch (SQLException e) {
            e.printStackTrace();

            // SQLState 23000 => llave foránea: el idUsuario no existe
            if ("23000".equals(e.getSQLState())) {
                cuerpo.put("error", "El idUsuario indicado no existe");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(gson.toJson(cuerpo))
                        .build();
            }

            cuerpo.put("error", "Error al guardar la medida");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }
    }

    @Path("getAllMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedidas() {
        Gson gson = new Gson();

        try {
            ControllerMedida cm = new ControllerMedida();
            return Response.ok(gson.toJson(cm.getMedidas())).build();

        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("error", "Error al obtener las medidas");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }
    }

    @Path("getMedidasByUsuario")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidasByUsuario(@QueryParam("idUsuario") Integer idUsuario) {
        Gson gson = new Gson();

        if (idUsuario == null || idUsuario <= 0) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("error", "idUsuario es obligatorio y debe ser mayor que 0");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }

        try {
            ControllerMedida cm = new ControllerMedida();
            return Response.ok(gson.toJson(cm.getMedidasByUsuario(idUsuario))).build();

        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("error", "Error al obtener las medidas del usuario");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(gson.toJson(cuerpo))
                    .build();
        }
    }

    /**
     * Acepta "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss"
     * y "yyyy-MM-dd'T'HH:mm"; devuelve siempre "yyyy-MM-dd HH:mm:ss".
     */
    private String normalizarFecha(String fecha) {
        String[] patrones = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm"
        };

        for (String patron : patrones) {
            try {
                LocalDateTime f = LocalDateTime.parse(fecha, DateTimeFormatter.ofPattern(patron));
                return f.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException ex) {
                // probamos con el siguiente patrón
            }
        }
        throw new DateTimeParseException("Formato de fecha no válido: " + fecha, fecha, 0);
    }
}

 