using NexusMarket.Domain.ValueObjects;

namespace NexusMarket.Domain.Entities;

public class Reembolso
{
    public Guid Id { get; private set; }

    public Pedido Pedido { get; private set; }

    public Dinero Monto { get; private set; }

    public string Estado { get; private set; }

    public Reembolso(
        Pedido pedido,
        Dinero monto)
    {
        Id = Guid.NewGuid();
        Pedido = pedido;
        Monto = monto;
        Estado = "Pendiente";
    }

    public void Procesar()
    {
        Estado = "Procesado";
    }
}