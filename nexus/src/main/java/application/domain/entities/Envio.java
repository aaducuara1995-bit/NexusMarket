using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Envio
{
    public Guid Id { get; private set; }

    public Pedido Pedido { get; private set; }

    public Direccion DireccionEntrega { get; private set; }

    public string Estado { get; private set; }

    public Envio(
        Pedido pedido,
        Direccion direccionEntrega)
    {
        Id = Guid.NewGuid();
        Pedido = pedido;
        DireccionEntrega = direccionEntrega;
        Estado = "Pendiente";
    }

    public void Despachar()
    {
        Estado = "Despachado";
    }

    public void Entregar()
    {
        Estado = "Entregado";
    }
}