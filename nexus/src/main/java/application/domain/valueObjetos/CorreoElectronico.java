namespace NexusMarket.Domain.ValueObjects;

public class CorreoElectronico
{
    public string Valor { get; private set; }

    public CorreoElectronico(string valor)
    {
        if (string.IsNullOrWhiteSpace(valor))
            throw new ArgumentException("El correo electrónico es obligatorio.");

        Valor = valor;
    }

    public override string ToString()
    {
        return Valor;
    }
}