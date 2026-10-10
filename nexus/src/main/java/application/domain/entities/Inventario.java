package application.domain.entities;

import application.domain.enums.*;


public class Inventario
{
    public Guid Id { get; private set; }
    public Producto Producto { get; private set; }
    public Bodega Bodega { get; private set; }
    public int Cantidad { get; private set; }
    public int Reservado { get; private set; }
    public EstadoInventario Estado { get; private set; }

    public int Disponible => Cantidad - Reservado;

    public Inventario(Producto producto, Bodega bodega, int cantidadInicial)
    {
        if (cantidadInicial < 0)
            throw new ArgumentException("El inventario no puede ser negativo.");

        Id = Guid.NewGuid();
        Producto = producto;
        Bodega = bodega;
        Cantidad = cantidadInicial;
        Reservado = 0;
        Estado = EstadoInventario.Disponible;
    }

    public void Reservar(int cantidad)
    {
        if (cantidad <= 0)
            throw new ArgumentException("La cantidad debe ser mayor que cero.");
        if (Estado != EstadoInventario.Disponible)
            throw new InvalidOperationException("No se puede reservar inventario que no está disponible.");
        if (Disponible < cantidad)
            throw new InvalidOperationException("No hay unidades suficientes para reservar.");

        Reservado += cantidad;
    }

    public void LiberarReserva(int cantidad)
    {
        if (cantidad <= 0 || cantidad > Reservado)
            throw new ArgumentException("Cantidad inválida para liberar.");

        Reservado -= cantidad;
    }

    public void ConfirmarSalida(int cantidad)
    {
        if (cantidad <= 0 || cantidad > Reservado)
            throw new ArgumentException("La salida debe corresponder a una reserva vigente.");

        Reservado -= cantidad;
        Cantidad -= cantidad;
    }

    public void Ingresar(int cantidad)
    {
        if (cantidad <= 0)
            throw new ArgumentException("La cantidad debe ser mayor que cero.");
        Cantidad += cantidad;
    }
}