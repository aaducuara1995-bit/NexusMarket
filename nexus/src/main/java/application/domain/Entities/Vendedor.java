using NexusMarket.Domain.Enums;
using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Vendedor : Usuario
{
    public Vendedor(
        NombreCompleto nombreCompleto,
        CorreoElectronico correo)
        : base(nombreCompleto, correo, RolUsuario.Vendedor)
    {
    }
}