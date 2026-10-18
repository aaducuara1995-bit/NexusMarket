package application.domain.entities;

import application.domain.enums.EstadoUsuario;
import application.domain.enums.RolUsuario;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;

import java.util.UUID;

public class Usuario {

    private final UUID id;
    private final NombreCompleto nombreCompleto;
    private final CorreoElectronico correo;
    private final DocumentoIdentidad documento;
    private final RolUsuario rol;
    private EstadoUsuario estado;

    public Usuario(NombreCompleto nombreCompleto,
                   CorreoElectronico correo,
                   DocumentoIdentidad documento,
                   RolUsuario rol) {
        if (nombreCompleto == null || correo == null || documento == null || rol == null)
            throw new IllegalArgumentException("Nombre, correo, documento y rol son obligatorios.");

        this.id = UUID.randomUUID();
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.documento = documento;
        this.rol = rol;
        this.estado = EstadoUsuario.Activo;
    }

    public void bloquear() {
        if (estado == EstadoUsuario.Bloqueado)
            throw new IllegalStateException("El usuario ya está bloqueado.");
        estado = EstadoUsuario.Bloqueado;
    }

    public void activar() {
        if (estado == EstadoUsuario.Activo)
            throw new IllegalStateException("El usuario ya está activo.");
        estado = EstadoUsuario.Activo;
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.Activo;
    }

    public boolean tieneRol(RolUsuario esperado) {
        return rol == esperado;
    }

    public UUID getId() { return id; }
    public NombreCompleto getNombreCompleto() { return nombreCompleto; }
    public CorreoElectronico getCorreo() { return correo; }
    public DocumentoIdentidad getDocumento() { return documento; }
    public RolUsuario getRol() { return rol; }
    public EstadoUsuario getEstado() { return estado; }
}