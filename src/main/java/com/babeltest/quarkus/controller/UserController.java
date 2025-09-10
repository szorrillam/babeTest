package com.babeltest.quarkus.controller;

import com.babeltest.quarkus.model.User;
import com.babeltest.quarkus.model.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de usuarios.
 * Sigue el principio de responsabilidad única (SRP) al encargarse
 * únicamente de manejar las solicitudes HTTP y delegar el procesamiento
 * a Apache Camel mediante ProducerTemplate.
 * 
 * También sigue el principio de inversión de dependencias (DIP) al depender
 * de la abstracción ProducerTemplate en lugar de implementaciones concretas.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Operaciones de gestión de usuarios")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Inject
    ProducerTemplate producerTemplate;
    
    /**
     * Crea un nuevo usuario.
     * Delega el procesamiento a Apache Camel para validación, transformación y almacenamiento.
     */
    @POST
    @Operation(
        summary = "Crear un nuevo usuario",
        description = "Crea un nuevo usuario con validación automática de datos y asignación de rol por defecto"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Datos de usuario inválidos",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public Response createUser(
            @Parameter(description = "Datos del usuario a crear", required = true)
            UserRequest userRequest) {
        
        try {
            logger.info("Recibida solicitud de creación de usuario: {}", userRequest);
            
            // Delegar a Apache Camel para procesamiento
            Object result = producerTemplate.requestBody("direct:createUser", userRequest);
            
            if (result instanceof User) {
                logger.info("Usuario creado exitosamente: {}", result);
                return Response.status(Response.Status.CREATED)
                        .entity(result)
                        .build();
            } else {
                logger.warn("Error en creación de usuario: {}", result);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(result)
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Error interno al crear usuario", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene todos los usuarios.
     * Delega el procesamiento a Apache Camel.
     */
    @GET
    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Retorna una lista con todos los usuarios almacenados en memoria"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = User[].class))
        )
    })
    public Response getAllUsers() {
        try {
            logger.info("Recibida solicitud de listado de usuarios");
            
            // Delegar a Apache Camel para procesamiento
            @SuppressWarnings("unchecked")
            List<User> users = producerTemplate.requestBody("direct:getAllUsers", null, List.class);
            
            logger.info("Usuarios obtenidos exitosamente: {} usuarios", users != null ? users.size() : 0);
            return Response.ok(users).build();
            
        } catch (Exception e) {
            logger.error("Error interno al obtener usuarios", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene un usuario por su ID.
     * Delega el procesamiento a Apache Camel.
     */
    @GET
    @Path("/{id}")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna los detalles de un usuario específico por su ID"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public Response getUserById(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id) {
        
        try {
            logger.info("Recibida solicitud de usuario con ID: {}", id);
            
            // Delegar a Apache Camel para procesamiento
            User user = producerTemplate.requestBody("direct:getUserById", id, User.class);
            
            if (user != null) {
                logger.info("Usuario encontrado: {}", user);
                return Response.ok(user).build();
            } else {
                logger.warn("Usuario no encontrado con ID: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuario no encontrado")
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Error interno al obtener usuario por ID", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza un usuario existente.
     * Delega el procesamiento a Apache Camel para validación, transformación y actualización.
     */
    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Actualizar usuario por ID",
        description = "Actualiza los datos de un usuario existente con validación automática"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Usuario actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Datos de usuario inválidos"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public Response updateUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Nuevos datos del usuario", required = true)
            UserRequest userRequest) {
        
        try {
            logger.info("Recibida solicitud de actualización de usuario con ID: {}", id);
            
            // Configurar headers para Apache Camel
            Map<String, Object> headers = new HashMap<>();
            headers.put("id", id);
            
            // Delegar a Apache Camel para procesamiento
            Object result = producerTemplate.requestBodyAndHeaders("direct:updateUser", userRequest, headers);
            
            if (result instanceof User) {
                logger.info("Usuario actualizado exitosamente: {}", result);
                return Response.ok(result).build();
            } else if (result instanceof String && ((String) result).contains("no encontrado")) {
                logger.warn("Usuario no encontrado para actualización con ID: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(result)
                        .build();
            } else {
                logger.warn("Error en actualización de usuario: {}", result);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(result)
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Error interno al actualizar usuario", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Elimina un usuario por su ID.
     * Delega el procesamiento a Apache Camel.
     */
    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Eliminar usuario por ID",
        description = "Elimina un usuario específico por su ID"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Usuario eliminado exitosamente"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public Response deleteUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id) {
        
        try {
            logger.info("Recibida solicitud de eliminación de usuario con ID: {}", id);
            
            // Delegar a Apache Camel para procesamiento
            Boolean deleted = producerTemplate.requestBody("direct:deleteUser", id, Boolean.class);
            
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Usuario eliminado exitosamente con ID: {}", id);
                return Response.noContent().build();
            } else {
                logger.warn("Usuario no encontrado para eliminación con ID: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuario no encontrado")
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Error interno al eliminar usuario", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Genera un reporte CSV de todos los usuarios.
     * Delega el procesamiento a Apache Camel.
     */
    @GET
    @Path("/report")
    @Produces("text/csv")
    @Operation(
        summary = "Generar reporte CSV",
        description = "Genera y descarga un archivo CSV con todos los usuarios almacenados"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Reporte CSV generado exitosamente",
            content = @Content(mediaType = "text/csv")
        )
    })
    public Response generateCsvReport() {
        try {
            logger.info("Recibida solicitud de generación de reporte CSV");
            
            // Delegar a Apache Camel para procesamiento
            String csvContent = producerTemplate.requestBody("direct:generateCsvReport", null, String.class);
            
            logger.info("Reporte CSV generado exitosamente");
            return Response.ok(csvContent)
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=users_report.csv")
                    .build();
            
        } catch (Exception e) {
            logger.error("Error interno al generar reporte CSV", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Clase auxiliar para encapsular el ID y los datos del usuario en actualizaciones.
     */
    public static class UserUpdateRequest {
        private String id;
        private UserRequest userRequest;
        
        public UserUpdateRequest() {}
        
        public UserUpdateRequest(String id, UserRequest userRequest) {
            this.id = id;
            this.userRequest = userRequest;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public UserRequest getUserRequest() {
            return userRequest;
        }
        
        public void setUserRequest(UserRequest userRequest) {
            this.userRequest = userRequest;
        }
    }
}
