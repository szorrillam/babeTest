package com.babeltest.quarkus.service;

import com.babeltest.quarkus.model.User;
import com.babeltest.quarkus.model.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Servicio de validación de usuarios.
 * Valida datos de usuarios.
 */
@ApplicationScoped
public class UserValidationService {
    
    private final Validator validator;
    
    // Patrones de validación específicos
    private static final Pattern WHATSAPP_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    public UserValidationService(Validator validator) {
        this.validator = validator;
    }
    
    /**
     * Valida un UserRequest usando las anotaciones de validación.
     * @param userRequest El request a validar
     * @return Set de violaciones de validación
     */
    public Set<ConstraintViolation<UserRequest>> validateUserRequest(UserRequest userRequest) {
        return validator.validate(userRequest);
    }
    
    /**
     * Valida un User usando las anotaciones de validación.
     * @param user El usuario a validar
     * @return Set de violaciones de validación
     */
    public Set<ConstraintViolation<User>> validateUser(User user) {
        return validator.validate(user);
    }
    
    /**
     * Valida el formato del número de WhatsApp.
     * @param whatsapp El número de WhatsApp a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean isValidWhatsapp(String whatsapp) {
        if (whatsapp == null || whatsapp.trim().isEmpty()) {
            return false;
        }
        return WHATSAPP_PATTERN.matcher(whatsapp.trim()).matches();
    }
    
    /**
     * Valida el formato del email.
     * @param email El email a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Valida que el rol sea válido.
     * @param role El rol a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean isValidRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return true; // El rol puede estar vacío
        }
        return "admin".equals(role) || "client".equals(role);
    }
    
    /**
     * Valida todos los campos de un UserRequest.
     * @param userRequest El request a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean isValidUserRequest(UserRequest userRequest) {
        if (userRequest == null) {
            return false;
        }
        
        return isValidEmail(userRequest.getEmail()) &&
               isValidWhatsapp(userRequest.getWhatsapp()) &&
               isValidRole(userRequest.getRole()) &&
               userRequest.getName() != null && !userRequest.getName().trim().isEmpty();
    }
}
