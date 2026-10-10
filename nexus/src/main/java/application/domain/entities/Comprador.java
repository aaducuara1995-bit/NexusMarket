using NexusMarket.Domain.Enums;
using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Comprador : Usuario
{
    public Direccion DireccionPrincipal { get; private set; }

    public List<Direccion> DireccionesAdicionales { get; private set; }

    public bool EstadoComercial { get; private set; }

    public Comprador(
        NombreCompleto nombreCompleto,
        CorreoElectronico correo,
        Direccion direccionPrincipal)
        : base(nombreCompleto, correo, RolUsuario.Comprador)
    {
        DireccionPrincipal = direccionPrincipal;
        DireccionesAdicionales = new List<Direccion>();
        EstadoComercial = true;
    }

    public void AgregarDireccion(Direccion direccion)
    {
        DireccionesAdicionales.Add(direccion);
    }
}