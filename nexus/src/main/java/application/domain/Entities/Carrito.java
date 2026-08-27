namespace NexusMarket.Domain.Entities;

public class Carrito
{
    public Guid Id { get; private set; }

    public Comprador Comprador { get; private set; }

    public List<Producto> Productos { get; private set; }

    public Carrito(Comprador comprador)
    {
        Id = Guid.NewGuid();
        Comprador = comprador;
        Productos = new List<Producto>();
    }

    public void AgregarProducto(Producto producto)
    {
        Productos.Add(producto);
    }

    public void Vaciar()
    {
        Productos.Clear();
    }
}