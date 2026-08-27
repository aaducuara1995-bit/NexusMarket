namespace NexusMarket.Domain.ValueObjects;

public class NombreCompleto
{
    public string Nombres { get; private set; }
    public string Apellidos { get; private set; }

    public NombreCompleto(string nombres, string apellidos)
    {
        if (string.IsNullOrWhiteSpace(nombres))
            throw new ArgumentException("Los nombres son obligatorios.");

        if (string.IsNullOrWhiteSpace(apellidos))
            throw new ArgumentException("Los apellidos son obligatorios.");

        Nombres = nombres;
        Apellidos = apellidos;
    }

    public override string ToString()
    {
        return $"{Nombres} {Apellidos}";
    }
}