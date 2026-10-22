package application.domain.services;

import application.domain.ports.in.PagarPedidoUseCase;

import application.domain.entities.Comprador;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.Dinero;

public class ProcesamientoPagoService implements PagarPedidoUseCase {

    private final ValidacionCompradorService validacionComprador;

    public ProcesamientoPagoService(ValidacionCompradorService validacionComprador) {
        if (validacionComprador == null)
            throw new IllegalArgumentException("El servicio de validación es obligatorio.");
        this.validacionComprador = validacionComprador;
    }

    public void pagar(Usuario usuario, Comprador comprador, Pedido pedido, Dinero montoPagado) {
        validacionComprador.exigirHabilitado(usuario, comprador);
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (montoPagado == null)
            throw new IllegalArgumentException("El monto pagado es obligatorio.");
        if (!pedido.perteneceA(comprador.getId()))
            throw new AccesoDenegadoException("El pedido pertenece a otro comprador.");

        Dinero total = pedido.calcularTotal();
        boolean coincide = total.moneda().equals(montoPagado.moneda())
                && total.monto().compareTo(montoPagado.monto()) == 0;
        if (!coincide)
            throw new IllegalArgumentException("El monto pagado no coincide con el total del pedido.");

        pedido.pagar();
    }
}