namespace NexusMarket.Domain.ValueObjects;

public class DocumentoIdentidad
{
    public string Valor { get; }

    public DocumentoIdentidad(string valor)
    {
        if (string.IsNullOrWhiteSpace(valor))
            throw new ArgumentException("El documento de identidad no puede estar vacío.");

        if (!valor.All(char.IsDigit))
            throw new ArgumentException("El documento de identidad solo debe contener números.");

        Valor = valor;
    }

    public override bool Equals(object? obj) =>
        obj is DocumentoIdentidad otro && Valor == otro.Valor;

    public override int GetHashCode() => Valor.GetHashCode();

    public override string ToString() => Valor;
}