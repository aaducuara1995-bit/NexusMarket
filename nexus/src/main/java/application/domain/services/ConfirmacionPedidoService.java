package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.ItemCarrito;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Pedido;
import application.domain.entities.ReservaInventario;
import application.domain.entities.Usuario;
import application.domain.exceptions.AccesoDenegadoException;

import java.util.ArrayList;
import java.util.List;

public class ConfirmacionPedidoService {

    public record Resultado(Pedido pedido,
                            List<ReservaInventario> reservas,
                            List<MovimientoInventario> movimientos) {
    }

    private final ValidacionCompradorService validacionComprador;
    private final ReservaInventarioService reservaService;
    private final LiberacionReservaService liberacionService;

    public ConfirmacionPedidoService(ValidacionCompradorService validacionComprador,
                                     ReservaInventarioService reservaService,
                                     LiberacionReservaService liberacionService) {
        if (validacionComprador == null || reservaService == null || liberacionService == null)
            throw new IllegalArgumentException("Los servicios de apoyo son obligatorios.");
        this.validacionComprador = validacionComprador;
        this.reservaService = reservaService;
        this.liberacionService = liberacionService;
    }

    public Resultado confirmar(Usuario usuario, Comprador comprador, Carrito carrito,
                               List<Inventario> inventarios) {
        validacionComprador.exigirHabilitado(usuario, comprador);
        if (carrito == null)
            throw new IllegalArgumentException("El carrito es obligatorio.");
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");
        if (!carrito.getComprador().getId().equals(comprador.getId()))
            throw new AccesoDenegadoException("El carrito pertenece a otro comprador.");
        if (carrito.getItems().isEmpty())
            throw new IllegalArgumentException("No se puede confirmar un carrito vacío.");

        for (ItemCarrito item : carrito.getItems()) {
            if (!item.getProducto().estaPublicado())
                throw new IllegalStateException(
                        "El producto " + item.getProducto().getNombre() + " ya no está publicado.");
        }

        Pedido pedido = new Pedido(carrito);
        List<ReservaInventario> reservas = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        try {
            for (ItemCarrito item : carrito.getItems()) {
                var resultado = reservaService.reservar(
                        pedido.getId(), item.getProducto(), item.getCantidad(), inventarios);
                reservas.addAll(resultado.reservas());
                movimientos.addAll(resultado.movimientos());
            }
        } catch (RuntimeException e) {
            // Todo o nada: si un producto falla, se deshace lo que ya se había reservado.
            liberacionService.liberar(reservas);
            throw e;
        }

        carrito.vaciar();
        return new Resultado(pedido, reservas, movimientos);
    }
}