package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultaDisponibilidadServiceTest {

    private final ConsultaDisponibilidadService servicio = new ConsultaDisponibilidadService();

    @Test
    void sumaElDisponibleDeVariasBodegasDescontandoLoReservado() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario a = Fixtures.inventario(producto, 10);
        Inventario b = Fixtures.inventario(producto, 5);
        a.reservar(3);

        assertEquals(12, servicio.disponibleDe(producto, List.of(a, b)));
    }

    @Test
    void ignoraElInventarioDanado() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario bueno = Fixtures.inventario(producto, 10);
        Inventario danado = Fixtures.inventario(producto, 5);
        danado.marcarDanado();

        assertEquals(10, servicio.disponibleDe(producto, List.of(bueno, danado)));
    }

    @Test
    void ignoraElInventarioDeOtrosProductos() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Producto otro = Fixtures.producto(TipoProducto.Fisico);

        assertEquals(0, servicio.disponibleDe(producto, List.of(Fixtures.inventario(otro, 8))));
    }

    @Test
    void unProductoDigitalSiempreTieneDisponibilidad() {
        Producto digital = Fixtures.producto(TipoProducto.Digital);

        assertTrue(servicio.hayDisponibilidad(digital, 5, List.of()));
    }

    @Test
    void unProductoFisicoSinExistenciasSuficientesNoTieneDisponibilidad() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 4);

        assertTrue(servicio.hayDisponibilidad(producto, 4, List.of(inventario)));
        assertFalse(servicio.hayDisponibilidad(producto, 5, List.of(inventario)));
    }
}