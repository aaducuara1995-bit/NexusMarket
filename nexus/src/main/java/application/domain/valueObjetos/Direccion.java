namespace NexusMarket.Domain.ValueObjects;

@setter
@getter

public class Direccion
{
    public string Calle { get; private set; }
    public string Ciudad { get; private set; }
    public string Departamento { get; private set; }
    public string CodigoPostal { get; private set; }

    public Direccion(
        string calle,
        string ciudad,
        string departamento,
        string codigoPostal)
    {
        Calle = calle;
        Ciudad = ciudad;
        Departamento = departamento;
        CodigoPostal = codigoPostal;
    }
}