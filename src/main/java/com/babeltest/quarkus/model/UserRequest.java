package com.babeltest.quarkus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para las solicitudes de creación y actualización de usuarios.
 */
public class UserRequest {
    
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
    public UserRequest() {}
    
    // Constructor con parámetros
    public UserRequest(String name, String whatsapp, String email, String role) {
        this.name = name;
        this.whatsapp = whatsapp;
        this.email = email;
        this.role = role;
    }
    
    // Getters y Setters
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
    public String toString() {
        return "UserRequest{" +
                "name='" + name + '\'' +
                ", whatsapp='" + whatsapp + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
