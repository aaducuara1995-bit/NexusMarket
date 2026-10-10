package application.domain.entities;

import application.domain.enums.*;


public class Producto
{
    public Guid Id { get; private set; }
    public string Nombre { get; private set; }
    public string Descripcion { get; private set; }
    public TipoProducto Tipo { get; private set; }
    public EstadoProducto Estado { get; private set; }

    private readonly List<string> _variantes = new();
    public IReadOnlyList<string> Variantes => _variantes.AsReadOnly();

    public Producto(string nombre, string descripcion, TipoProducto tipo)
    {
        Id = Guid.NewGuid();
        Nombre = nombre;
        Descripcion = descripcion;
        Tipo = tipo;
        Estado = EstadoProducto.Suspendido;
    }

    public void AgregarVariante(string variante)
    {
        if (string.IsNullOrWhiteSpace(variante))
            throw new ArgumentException("La variante no puede estar vacía.");

        if (_variantes.Contains(variante))
            throw new InvalidOperationException("Esa variante ya existe en el producto.");

        _variantes.Add(variante);
    }

    public void Publicar()
    {
        if (Estado == EstadoProducto.Descontinuado)
            throw new InvalidOperationException("Un producto descontinuado no puede volver a publicarse.");

        Estado = EstadoProducto.Publicado;
    

    public void Descontinuar()
    {
        Estado = EstadoProducto.Descontinuado;
    }
}