package com.babeltest.quarkus.camel;

import com.babeltest.quarkus.model.User;
import com.babeltest.quarkus.model.UserRequest;
import com.babeltest.quarkus.service.UserService;
import com.babeltest.quarkus.service.UserValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Rutas de Apache Camel para el manejo de usuarios.
 * Implementa la lógica de validación, transformación y enrutamiento
 */
@ApplicationScoped
public class UserRoutes extends RouteBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRoutes.class);
    
    @Inject
    UserService userService;
    
    @Inject
    UserValidationService userValidationService;
    
    @Override
    public void configure() throws Exception {
        
        // Configuración REST
        restConfiguration()
            .component("platform-http")
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .contextPath("/api")
            .port(8080);
        
        // Rutas directas para el controlador REST
        from("direct:createUser")
            .log("Procesando solicitud de creación de usuario: ${body}")
            .process(this::validateAndTransformUser)
            .choice()
                .when(header("validationError").isNull())
                    .process(this::createUser)
                    .log("Usuario creado exitosamente: ${body}")
                .otherwise()
                    .setBody(header("validationError"))
                    .log("Error de validación: ${body}")
            .end();
        
        from("direct:getAllUsers")
            .log("Procesando solicitud de listado de usuarios")
            .process(this::getAllUsers)
            .log("Usuarios obtenidos exitosamente: ${body.size()} usuarios");
        
        from("direct:getUserById")
            .log("Procesando solicitud de usuario con ID: ${body}")
            .process(this::getUserById)
            .log("Usuario obtenido: ${body}");
        
        from("direct:updateUser")
            .log("Procesando solicitud de actualización de usuario: ${body}")
            .process(this::validateAndTransformUserUpdate)
            .choice()
                .when(header("validationError").isNull())
                    .process(this::updateUserFromDirect)
                    .log("Usuario actualizado exitosamente: ${body}")
                .otherwise()
                    .setBody(header("validationError"))
                    .log("Error de validación en actualización: ${body}")
            .end();
        
        from("direct:deleteUser")
            .log("Procesando solicitud de eliminación de usuario con ID: ${body}")
            .process(this::deleteUserFromDirect)
            .log("Usuario eliminado: ${header.deleted}");
        
        from("direct:generateCsvReport")
            .log("Procesando solicitud de reporte CSV")
            .process(this::generateCsvReport)
            .log("Reporte CSV generado exitosamente");
        
        // Las rutas REST están manejadas por el controlador de Quarkus
        // Apache Camel solo maneja el procesamiento interno
    }
    
    /**
     * Procesador para validar y transformar datos de usuario.
     */
    private void validateAndTransformUser(Exchange exchange) {
        try {
            UserRequest userRequest = exchange.getIn().getBody(UserRequest.class);
            
            if (userRequest == null) {
                exchange.getIn().setHeader("validationError", "Datos de usuario requeridos");
                return;
            }
            
            String validationError = validateUserRequest(userRequest);
            if (validationError != null) {
                exchange.getIn().setHeader("validationError", validationError);
                return;
            }
            
            // Aplicar transformación: asignar rol "client" si está vacío
            if (userRequest.getRole() == null || userRequest.getRole().trim().isEmpty()) {
                userRequest.setRole("client");
            }
            
            exchange.getIn().setBody(userRequest);
            logger.info("Usuario validado y transformado exitosamente: {}", userRequest);
            
        } catch (Exception e) {
            logger.error("Error en validación y transformación: {}", e.getMessage(), e);
            exchange.getIn().setHeader("validationError", "Error interno en validación: " + e.getMessage());
        }
    }
    
    /**
     * Valida un UserRequest y retorna el mensaje de error si existe.
     */
    private String validateUserRequest(UserRequest userRequest) {
        if (userRequest.getName() == null || userRequest.getName().trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        
        if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
            return "El email es obligatorio";
        }
        
        if (userRequest.getWhatsapp() == null || userRequest.getWhatsapp().trim().isEmpty()) {
            return "El número de WhatsApp es obligatorio";
        }
        
        if (!userValidationService.isValidEmail(userRequest.getEmail())) {
            return "El email debe tener un formato válido";
        }
        
        if (!userValidationService.isValidWhatsapp(userRequest.getWhatsapp())) {
            return "El número de WhatsApp debe tener un formato válido";
        }
        
        if (!userValidationService.isValidRole(userRequest.getRole())) {
            return "El rol debe ser 'admin' o 'client'";
        }
        
        return null; // No hay errores
    }
    
    /**
     * Procesador para crear un usuario.
     */
    private void createUser(Exchange exchange) {
        try {
            UserRequest userRequest = exchange.getIn().getBody(UserRequest.class);
            User user = userService.createUser(userRequest);
            exchange.getIn().setBody(user);
        } catch (Exception e) {
            logger.error("Error al crear usuario: {}", e.getMessage(), e);
            exchange.getIn().setHeader("validationError", "Error al crear usuario: " + e.getMessage());
            exchange.getIn().setBody(null);
        }
    }
    
    /**
     * Procesador para obtener todos los usuarios.
     */
    private void getAllUsers(Exchange exchange) {
        try {
            List<User> users = userService.getAllUsers();
            exchange.getIn().setBody(users);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios: {}", e.getMessage(), e);
            exchange.getIn().setBody(null);
        }
    }
    
    /**
     * Procesador para obtener un usuario por ID.
     */
    private void getUserById(Exchange exchange) {
        try {
            String id = exchange.getIn().getHeader("id", String.class);
            if (id == null) {
                id = exchange.getIn().getBody(String.class);
            }
            User user = userService.getUserById(id).orElse(null);
            exchange.getIn().setBody(user);
        } catch (Exception e) {
            logger.error("Error al obtener usuario por ID: {}", e.getMessage(), e);
            exchange.getIn().setBody(null);
        }
    }
    
    /**
     * Procesador para actualizar un usuario.
     */
    private void updateUser(Exchange exchange) {
        try {
            String id = exchange.getIn().getHeader("id", String.class);
            UserRequest userRequest = exchange.getIn().getBody(UserRequest.class);
            User user = userService.updateUser(id, userRequest);
            exchange.getIn().setBody(user);
        } catch (Exception e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage(), e);
            exchange.getIn().setHeader("validationError", "Error al actualizar usuario: " + e.getMessage());
            exchange.getIn().setBody(null);
        }
    }
    
    /**
     * Procesador para eliminar un usuario.
     */
    private void deleteUser(Exchange exchange) {
        try {
            String id = exchange.getIn().getHeader("id", String.class);
            boolean deleted = userService.deleteUser(id);
            exchange.getIn().setHeader("deleted", deleted);
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage(), e);
            exchange.getIn().setHeader("deleted", false);
        }
    }
    
    /**
     * Procesador para validar y transformar datos de usuario en actualizaciones.
     */
    private void validateAndTransformUserUpdate(Exchange exchange) {
        try {
            String id = exchange.getIn().getHeader("id", String.class);
            UserRequest userRequest = exchange.getIn().getBody(UserRequest.class);
            
            logger.info("ID recibido: {}, UserRequest: {}", id, userRequest);
            
            if (id == null || userRequest == null) {
                exchange.getIn().setHeader("validationError", "Datos de usuario requeridos");
                return;
            }
            
            // Validar datos básicos
            if (userRequest.getName() == null || userRequest.getName().trim().isEmpty()) {
                exchange.getIn().setHeader("validationError", "El nombre es obligatorio");
                return;
            }
            
            if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
                exchange.getIn().setHeader("validationError", "El email es obligatorio");
                return;
            }
            
            if (userRequest.getWhatsapp() == null || userRequest.getWhatsapp().trim().isEmpty()) {
                exchange.getIn().setHeader("validationError", "El número de WhatsApp es obligatorio");
                return;
            }
            
            // Validar formato de email
            if (!userRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                exchange.getIn().setHeader("validationError", "El email debe tener un formato válido");
                return;
            }
            
            // Validar formato de WhatsApp
            if (!userRequest.getWhatsapp().matches("^\\+?[1-9]\\d{1,14}$")) {
                exchange.getIn().setHeader("validationError", "El número de WhatsApp debe tener un formato válido");
                return;
            }
            
            // Validar rol si está presente
            if (userRequest.getRole() != null && !userRequest.getRole().trim().isEmpty()) {
                if (!"admin".equals(userRequest.getRole()) && !"client".equals(userRequest.getRole())) {
                    exchange.getIn().setHeader("validationError", "El rol debe ser 'admin' o 'client'");
                    return;
                }
            }
            
            // Aplicar transformación: asignar rol "client" si está vacío
            if (userRequest.getRole() == null || userRequest.getRole().trim().isEmpty()) {
                userRequest.setRole("client");
            }
            
            // Crear UserUpdateRequest con los datos validados
            UserUpdateRequest updateRequest = new UserUpdateRequest(id, userRequest);
            exchange.getIn().setBody(updateRequest);
            logger.info("Usuario para actualización validado y transformado exitosamente: {}", userRequest);
            
        } catch (Exception e) {
            logger.error("Error en validación y transformación de actualización: {}", e.getMessage(), e);
            exchange.getIn().setHeader("validationError", "Error interno en validación: " + e.getMessage());
        }
    }
    
    /**
     * Procesador para actualizar un usuario desde ruta directa.
     */
    private void updateUserFromDirect(Exchange exchange) {
        try {
            UserUpdateRequest updateRequest = exchange.getIn().getBody(UserUpdateRequest.class);
            User user = userService.updateUser(updateRequest.getId(), updateRequest.getUserRequest());
            exchange.getIn().setBody(user);
        } catch (Exception e) {
            logger.error("Error al actualizar usuario desde ruta directa: {}", e.getMessage(), e);
            exchange.getIn().setHeader("validationError", "Error al actualizar usuario: " + e.getMessage());
            exchange.getIn().setBody(null);
        }
    }
    
    /**
     * Procesador para eliminar un usuario desde ruta directa.
     */
    private void deleteUserFromDirect(Exchange exchange) {
        try {
            String id = exchange.getIn().getBody(String.class);
            boolean deleted = userService.deleteUser(id);
            exchange.getIn().setBody(deleted);
        } catch (Exception e) {
            logger.error("Error al eliminar usuario desde ruta directa: {}", e.getMessage(), e);
            exchange.getIn().setBody(false);
        }
    }
    
    /**
     * Procesador para generar reporte CSV.
     */
    private void generateCsvReport(Exchange exchange) {
        try {
            List<User> users = userService.getAllUsers();
            StringBuilder csv = new StringBuilder();
            
            // Encabezados CSV
            csv.append("ID,Name,WhatsApp,Email,Role\n");
            
            // Datos de usuarios
            for (User user : users) {
                csv.append(String.format("%s,%s,%s,%s,%s\n",
                    user.getId(),
                    escapeCsvField(user.getName()),
                    escapeCsvField(user.getWhatsapp()),
                    escapeCsvField(user.getEmail()),
                    escapeCsvField(user.getRole())
                ));
            }
            
            exchange.getIn().setBody(csv.toString());
            logger.info("Reporte CSV generado con {} usuarios", users.size());
            
        } catch (Exception e) {
            logger.error("Error al generar reporte CSV: {}", e.getMessage(), e);
            exchange.getIn().setBody("Error al generar reporte");
        }
    }
    
    /**
     * Escapa campos CSV para manejar comas y comillas.
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
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
