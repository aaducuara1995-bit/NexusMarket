namespace NexusMarket.Domain.ValueObjects;

public class Dinero
{
    public decimal Monto { get; private set; }
    public string Moneda { get; private set; }

    public Dinero(decimal monto, string moneda = "COP")
    {
        if (monto < 0)
            throw new ArgumentException("El monto no puede ser negativo.");

        Monto = monto;
        Moneda = moneda;
    }
}