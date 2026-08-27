using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Factura
{
    public Guid Id { get; private set; }

    public Pedido Pedido { get; private set; }

    public Dinero Total { get; private set; }

    public DateTime Fecha { get; private set; }

    public Factura(
        Pedido pedido,
        Dinero total)
    {
        Id = Guid.NewGuid();
        Pedido = pedido;
        Total = total;
        Fecha = DateTime.Now;
    }
}