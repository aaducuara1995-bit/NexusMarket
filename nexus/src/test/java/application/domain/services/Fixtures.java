package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoBodega;
import application.domain.enums.TipoProducto;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.Dinero;
import application.domain.valueobjects.Direccion;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;

import java.math.BigDecimal;
import java.util.UUID;

final class Fixtures {

    private Fixtures() {
    }

    static Direccion direccion() {
        return new Direccion("Calle 1 # 2-3", "Medellin", "Antioquia", "050001");
    }

    static Usuario usuario(RolUsuario rol) {
        return new Usuario(
                new NombreCompleto("Ana", "Perez"),
                new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("123456"),
                rol);
    }

    static Vendedor vendedor(Usuario usuario) {
        return new Vendedor(usuario.getId());
    }

    static Comprador comprador(Usuario usuario) {
        return new Comprador(usuario.getId(), direccion());
    }

    static Producto producto(TipoProducto tipo) {
        return producto(UUID.randomUUID(), tipo);
    }

    static Producto producto(UUID vendedorId, TipoProducto tipo) {
        return new Producto(vendedorId, "Camiseta", "Algodon", tipo,
                new Dinero(new BigDecimal("45000")));
    }

    static Bodega bodega() {
        return Bodega.delMarketplace("Bodega central", TipoBodega.Fisica, direccion());
    }

    static Inventario inventario(Producto producto, int cantidad) {
        return new Inventario(producto, bodega(), cantidad);
    }

    static Pedido pedidoEntregado(Producto producto, int cantidad) {
        if (!producto.estaPublicado())
            producto.publicar();

        Carrito carrito = new Carrito(comprador(usuario(RolUsuario.Comprador)));
        carrito.agregarProducto(producto, cantidad);

        Pedido pedido = new Pedido(carrito);
        pedido.pagar();
        pedido.enviar();
        pedido.finalizar();
        return pedido;
    }
}