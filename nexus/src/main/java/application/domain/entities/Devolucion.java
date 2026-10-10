namespace NexusMarket.Domain.Entities;

public class Devolucion
{
    public Guid Id { get; private set; }

    public Pedido Pedido { get; private set; }

    public string Motivo { get; private set; }

    public string Estado { get; private set; }

    public Devolucion(
        Pedido pedido,
        string motivo)
    {
        Id = Guid.NewGuid();
        Pedido = pedido;
        Motivo = motivo;
        Estado = "Solicitada";
    }

    public void Aprobar()
    {
        Estado = "Aprobada";
    }

    public void Rechazar()
    {
        Estado = "Rechazada";
    }
}