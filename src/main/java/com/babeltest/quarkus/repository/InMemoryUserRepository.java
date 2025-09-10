package com.babeltest.quarkus.repository;

import com.babeltest.quarkus.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de usuarios.
 * 
 * Utiliza ConcurrentHashMap para garantizar thread-safety en un entorno concurrente.
 */
@ApplicationScoped
public class InMemoryUserRepository implements UserRepository {
    
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }
    
    @Override
    public List<User> findAll() {
        return users.values().stream()
                .collect(Collectors.toList());
    }
    
    @Override
    public User update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (!existsById(user.getId())) {
            throw new IllegalArgumentException("El usuario con ID " + user.getId() + " no existe");
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return users.remove(id) != null;
    }
    
    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return users.containsKey(id);
    }
}
