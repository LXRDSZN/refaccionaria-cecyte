package com.mycompany.refaccionaria.automotriz.model;

public class Usuario {

    private Integer id;
    private String nombreUsuario;
    private String passwordHash;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(Integer id, String nombreUsuario, String passwordHash, Rol rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Usuario(String nombreUsuario, String passwordHash, Rol rol) {
        this(null, nombreUsuario, passwordHash, rol);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
