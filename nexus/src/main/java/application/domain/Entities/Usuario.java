using NexusMarket.Domain.Enums;
using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Usuario
{
    public Guid Id { get; private set; }
    public NombreCompleto NombreCompleto { get; private set; }
    public CorreoElectronico Correo { get; private set; }
    public RolUsuario Rol { get; private set; }
    public EstadoUsuario Estado { get; private set; }

    public Usuario(
        NombreCompleto nombreCompleto,
        CorreoElectronico correo,
        RolUsuario rol)
    {
        Id = Guid.NewGuid();
        NombreCompleto = nombreCompleto;
        Correo = correo;
        Rol = rol;
        Estado = EstadoUsuario.Activo;
    }

    public void Bloquear()
    {
        Estado = EstadoUsuario.Bloqueado;
    }
}