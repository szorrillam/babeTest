package com.babeltest.quarkus.service;

import com.babeltest.quarkus.model.User;
import com.babeltest.quarkus.model.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Servicio de transformación de usuarios.
 * Transforma datos entre diferentes representaciones.
 */
@ApplicationScoped
public class UserTransformationService {
    
    /**
     * Transforma un UserRequest en un User.
     * Aplica las reglas de negocio de transformación.
     * 
     * @param userRequest El request a transformar
     * @return El usuario transformado
     */
    public User transformToUser(UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException("El UserRequest no puede ser nulo");
        }
        
        User user = new User();
        user.setName(userRequest.getName());
        user.setWhatsapp(userRequest.getWhatsapp());
        user.setEmail(userRequest.getEmail());
        
        // Aplicar regla de negocio: si el rol está vacío, asignar "client"
        String role = userRequest.getRole();
        if (role == null || role.trim().isEmpty()) {
            user.setRole("client");
        } else {
            user.setRole(role);
        }
        
        return user;
    }
    
    /**
     * Actualiza un usuario existente con los datos de un UserRequest.
     * 
     * @param existingUser El usuario existente
     * @param userRequest Los nuevos datos
     * @return El usuario actualizado
     */
    public User updateUserFromRequest(User existingUser, UserRequest userRequest) {
        if (existingUser == null) {
            throw new IllegalArgumentException("El usuario existente no puede ser nulo");
        }
        if (userRequest == null) {
            throw new IllegalArgumentException("El UserRequest no puede ser nulo");
        }
        
        existingUser.setName(userRequest.getName());
        existingUser.setWhatsapp(userRequest.getWhatsapp());
        existingUser.setEmail(userRequest.getEmail());
        
        // Aplicar regla de negocio: si el rol está vacío, asignar "client"
        String role = userRequest.getRole();
        if (role == null || role.trim().isEmpty()) {
            existingUser.setRole("client");
        } else {
            existingUser.setRole(role);
        }
        
        return existingUser;
    }
    
    /**
     * Transforma un User en un UserRequest.
     * Útil para operaciones de actualización.
     * 
     * @param user El usuario a transformar
     * @return El UserRequest transformado
     */
    public UserRequest transformToUserRequest(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El User no puede ser nulo");
        }
        
        UserRequest userRequest = new UserRequest();
        userRequest.setName(user.getName());
        userRequest.setWhatsapp(user.getWhatsapp());
        userRequest.setEmail(user.getEmail());
        userRequest.setRole(user.getRole());
        
        return userRequest;
    }
}
