using NexusMarket.Domain.Enums;

namespace NexusMarket.Domain.Entities;

public class Inventario
{
    public Guid Id { get; private set; }

    public Producto Producto { get; private set; }

    public Bodega Bodega { get; private set; }

    public int Cantidad { get; private set; }

    public Inventario(
        Producto producto,
        Bodega bodega,
        int cantidadInicial)
    {
        if (cantidadInicial < 0)
            throw new ArgumentException(
                "El inventario no puede ser negativo.");

        Id = Guid.NewGuid();
        Producto = producto;
        Bodega = bodega;
        Cantidad = cantidadInicial;
    }

    public void Ingresar(int cantidad)
    {
        if (cantidad <= 0)
            throw new ArgumentException(
                "La cantidad debe ser mayor que cero.");

        Cantidad += cantidad;
    }

    public void Retirar(int cantidad)
    {
        if (cantidad <= 0)
            throw new ArgumentException(
                "La cantidad debe ser mayor que cero.");

        if (Cantidad - cantidad < 0)
            throw new InvalidOperationException(
                "No existen suficientes unidades disponibles.");

        Cantidad -= cantidad;
    }
}