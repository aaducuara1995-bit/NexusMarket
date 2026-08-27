using NexusMarket.Domain.Enums;

namespace NexusMarket.Domain.Entities;

public class Pedido
{
    public Guid Id { get; private set; }

    public Comprador Comprador { get; private set; }

    public List<Producto> Productos { get; private set; }

    public EstadoPedido Estado { get; private set; }

    public DateTime Fecha { get; private set; }

    public Pedido(Comprador comprador)
    {
        Id = Guid.NewGuid();
        Comprador = comprador;
        Productos = new List<Producto>();
        Estado = EstadoPedido.Carrito;
        Fecha = DateTime.Now;
    }

    public void AgregarProducto(Producto producto)
    {
        if (Estado != EstadoPedido.Carrito)
            throw new InvalidOperationException(
                "No se pueden agregar productos al pedido.");

        Productos.Add(producto);
    }

    public void Pagar()
    {
        if (Estado != EstadoPedido.PendienteDePago)
            throw new InvalidOperationException(
                "El pedido no está pendiente de pago.");

        Estado = EstadoPedido.Pagado;
    }

    public void Enviar()
    {
        if (Estado != EstadoPedido.Pagado)
            throw new InvalidOperationException(
                "El pedido debe estar pagado.");

        Estado = EstadoPedido.Despachado;
    }

    public void Finalizar()
    {
        if (Estado != EstadoPedido.Despachado)
            throw new InvalidOperationException(
                "El pedido debe estar despachado.");

        Estado = EstadoPedido.Entregado;
    }
}