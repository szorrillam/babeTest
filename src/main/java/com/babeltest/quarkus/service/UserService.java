package com.babeltest.quarkus.service;

import com.babeltest.quarkus.model.User;
import com.babeltest.quarkus.model.UserRequest;
import com.babeltest.quarkus.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio principal de usuarios que orquesta las operaciones.
 */
@ApplicationScoped
public class UserService {
    
    private final UserRepository userRepository;
    private final UserValidationService validationService;
    private final UserTransformationService transformationService;
    
    @Inject
    public UserService(UserRepository userRepository, 
                      UserValidationService validationService,
                      UserTransformationService transformationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.transformationService = transformationService;
    }
    
    /**
     * Crea un nuevo usuario.
     * @param userRequest Los datos del usuario a crear
     * @return El usuario creado
     * @throws IllegalArgumentException si los datos no son válidos
     */
    public User createUser(UserRequest userRequest) {
        // Validar el request
        Set<ConstraintViolation<UserRequest>> violations = validationService.validateUserRequest(userRequest);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Datos de usuario inválidos: " + violations.toString());
        }
        
        // Transformar y crear el usuario
        User user = transformationService.transformToUser(userRequest);
        
        // Validar el usuario transformado
        Set<ConstraintViolation<User>> userViolations = validationService.validateUser(user);
        if (!userViolations.isEmpty()) {
            throw new IllegalArgumentException("Usuario inválido después de transformación: " + userViolations.toString());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario
     * @return Un Optional que puede contener el usuario
     */
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Obtiene todos los usuarios.
     * @return Lista de todos los usuarios
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Actualiza un usuario existente.
     * @param id El ID del usuario a actualizar
     * @param userRequest Los nuevos datos
     * @return El usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe o los datos no son válidos
     */
    public User updateUser(String id, UserRequest userRequest) {
        // Validar el request
        Set<ConstraintViolation<UserRequest>> violations = validationService.validateUserRequest(userRequest);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Datos de usuario inválidos: " + violations.toString());
        }
        
        // Verificar que el usuario existe
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + id + " no encontrado"));
        
        // Actualizar el usuario
        User updatedUser = transformationService.updateUserFromRequest(existingUser, userRequest);
        
        // Validar el usuario actualizado
        Set<ConstraintViolation<User>> userViolations = validationService.validateUser(updatedUser);
        if (!userViolations.isEmpty()) {
            throw new IllegalArgumentException("Usuario inválido después de actualización: " + userViolations.toString());
        }
        
        return userRepository.update(updatedUser);
    }
    
    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar
     * @return true si el usuario fue eliminado, false si no existía
     */
    public boolean deleteUser(String id) {
        return userRepository.deleteById(id);
    }
    
    /**
     * Verifica si un usuario existe.
     * @param id El ID del usuario
     * @return true si existe, false en caso contrario
     */
    public boolean userExists(String id) {
        return userRepository.existsById(id);
    }
}
