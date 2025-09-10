package com.babeltest.quarkus.repository;

import com.babeltest.quarkus.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de usuarios.
 */
public interface UserRepository {
    
    /**
     * Guarda un usuario en el repositorio.
     * @param user El usuario a guardar
     * @return El usuario guardado
     */
    User save(User user);
    
    /**
     * Busca un usuario por su ID.
     * @param id El ID del usuario
     * @return Un Optional que puede contener el usuario encontrado
     */
    Optional<User> findById(String id);
    
    /**
     * Obtiene todos los usuarios del repositorio.
     * @return Lista de todos los usuarios
     */
    List<User> findAll();
    
    /**
     * Actualiza un usuario existente.
     * @param user El usuario con los datos actualizados
     * @return El usuario actualizado
     */
    User update(User user);
    
    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar
     * @return true si el usuario fue eliminado, false si no existía
     */
    boolean deleteById(String id);
    
    /**
     * Verifica si existe un usuario con el ID dado.
     * @param id El ID del usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsById(String id);
}
