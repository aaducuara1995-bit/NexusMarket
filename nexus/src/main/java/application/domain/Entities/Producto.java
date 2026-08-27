using NexusMarket.Domain.Enums;

namespace NexusMarket.Domain.Entities;

public class Producto
{
    public Guid Id { get; private set; }
    public string Nombre { get; private set; }
    public string Descripcion { get; private set; }

    public TipoProducto Tipo { get; private set; }

    public List<string> Variantes { get; private set; }

    public EstadoProducto Estado { get; private set; }

    public Producto(
        string nombre,
        string descripcion,
        TipoProducto tipo)
    {
        Id = Guid.NewGuid();
        Nombre = nombre;
        Descripcion = descripcion;
        Tipo = tipo;
        Variantes = new List<string>();
        Estado = EstadoProducto.Suspendido;
    }

    public void AgregarVariante(string variante)
    {
        Variantes.Add(variante);
    }

    public void Publicar()
    {
        Estado = EstadoProducto.Publicado;
    }

    public void Descontinuar()
    {
        Estado = EstadoProducto.Descontinuado;
    }
}