package application.domain.services;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

public class ValidacionVendedorService {

    public void exigirVendedor(Usuario usuario, Vendedor vendedor) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario es obligatorio.");
        if (vendedor == null)
            throw new IllegalArgumentException("El vendedor es obligatorio.");

        if (!usuario.tieneRol(RolUsuario.Vendedor))
            throw new AccesoDenegadoException("El usuario no tiene el rol de vendedor.");
        if (!vendedor.getUsuarioId().equals(usuario.getId()))
            throw new AccesoDenegadoException("El vendedor no corresponde a este usuario.");
        if (!usuario.estaActivo())
            throw new IllegalStateException("El usuario está bloqueado y no puede operar.");
    }

    public void exigirPropietario(Usuario usuario, Vendedor vendedor, Producto producto) {
        exigirVendedor(usuario, vendedor);

        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");
        if (!producto.perteneceA(vendedor.getId()))
            throw new AccesoDenegadoException("El producto pertenece a otro vendedor.");
    }
}