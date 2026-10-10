namespace NexusMarket.Domain.Entities;

public class Bodega
{
    public Guid Id { get; private set; }
    public string Nombre { get; private set; }
    public string Tipo { get; private set; }

    public Bodega(string nombre, string tipo)
    {
        Id = Guid.NewGuid();
        Nombre = nombre;
        Tipo = tipo;
    }
}