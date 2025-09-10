package com.babeltest.quarkus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad User que representa un usuario en el sistema.
 * Información y validaciones relacionadas con un usuario.
 */
public class User {
    
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("name")
    private String name;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El número de WhatsApp debe tener un formato válido")
    @JsonProperty("whatsapp")
    private String whatsapp;
    
    @Email(message = "El email debe tener un formato válido")
    @JsonProperty("email")
    private String email;
    
    @Pattern(regexp = "^(admin|client)?$", message = "El rol debe ser 'admin' o 'client'")
    @JsonProperty("role")
    private String role;
    
    // Constructor por defecto
    public User() {
        this.id = UUID.randomUUID().toString();
    }
    
    // Constructor con parámetros
    public User(String name, String whatsapp, String email, String role) {
        this();
        this.name = name;
        this.whatsapp = whatsapp;
        this.email = email;
        this.role = role;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getWhatsapp() {
        return whatsapp;
    }
    
    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", whatsapp='" + whatsapp + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
