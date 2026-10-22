package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoPedido;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.ports.in.*;
import application.domain.valueobjects.Dinero;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuertosEntradaTest {

    @Test
    void cadaPuertoDeEntradaTieneSuServicio() {
        Map<Class<?>, Class<?>> pares = Map.ofEntries(
                Map.entry(IncorporarVendedorUseCase.class, IncorporacionVendedorService.class),
                Map.entry(PublicarProductoUseCase.class, PublicacionProductoService.class),
                Map.entry(CambiarEstadoProductoUseCase.class, CambioEstadoProductoService.class),
                Map.entry(ConsultarDisponibilidadUseCase.class, ConsultaDisponibilidadService.class),
                Map.entry(RegistrarInventarioInicialUseCase.class, RegistroInventarioInicialService.class),
                Map.entry(AjustarInventarioUseCase.class, AjusteInventarioService.class),
                Map.entry(MarcarInventarioUseCase.class, MarcadoInventarioService.class),
                Map.entry(ReintegrarInventarioUseCase.class, ReintegroInventarioService.class),
                Map.entry(AgregarAlCarritoUseCase.class, AgregarAlCarritoService.class),
                Map.entry(ConfirmarPedidoUseCase.class, ConfirmacionPedidoService.class),
                Map.entry(PagarPedidoUseCase.class, ProcesamientoPagoService.class),
                Map.entry(EntregarPedidoDigitalUseCase.class, EntregaDigitalService.class),
                Map.entry(EmitirFacturaUseCase.class, EmisionFacturaService.class),
                Map.entry(CrearEnvioUseCase.class, CreacionEnvioService.class),
                Map.entry(DespacharPedidoUseCase.class, DespachoService.class),
                Map.entry(ConfirmarEntregaUseCase.class, ConfirmacionEntregaService.class),
                Map.entry(SolicitarDevolucionUseCase.class, SolicitudDevolucionService.class),
                Map.entry(ResolverDevolucionUseCase.class, ResolucionDevolucionService.class),
                Map.entry(GenerarReembolsoUseCase.class, GeneracionReembolsoService.class),
                Map.entry(ProcesarReembolsoUseCase.class, ProcesamientoReembolsoService.class),
                Map.entry(RegistrarUsuarioUseCase.class, RegistroUsuarioService.class),
                Map.entry(RegistrarCompradorUseCase.class, RegistroCompradorService.class),
                Map.entry(RegistrarVendedorUseCase.class, RegistroVendedorService.class),
                Map.entry(RegistrarBodegaUseCase.class, RegistroBodegaService.class),
                Map.entry(RegistrarProductoUseCase.class, RegistroProductoService.class));

        assertEquals(25, pares.size());
        pares.forEach((puerto, servicio) ->
                assertTrue(puerto.isAssignableFrom(servicio)));
    }

    @Test
    void unaCompraCompletaSeHaceSoloAtravesDeLosPuertos() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
        Comprador comprador = Fixtures.comprador(usuario);
        Producto digital = Fixtures.producto(TipoProducto.Digital);
        digital.publicar();
        Carrito carrito = new Carrito(comprador);

        ValidacionCompradorService validacion = new ValidacionCompradorService();
        AgregarAlCarritoUseCase agregar = new AgregarAlCarritoService(validacion);
        ConfirmarPedidoUseCase confirmar = new ConfirmacionPedidoService(
                validacion, new ReservaInventarioService(), new LiberacionReservaService());
        PagarPedidoUseCase pagar = new ProcesamientoPagoService(validacion);
        EntregarPedidoDigitalUseCase entregar = new EntregaDigitalService();

        agregar.agregar(usuario, comprador, carrito, digital, 2, List.of());
        Pedido pedido = confirmar.confirmar(usuario, comprador, carrito, List.of()).pedido();
        Dinero total = pedido.calcularTotal();
        pagar.pagar(usuario, comprador, pedido, total);
        entregar.entregar(pedido);

        assertEquals(EstadoPedido.Entregado, pedido.getEstado());
    }
}