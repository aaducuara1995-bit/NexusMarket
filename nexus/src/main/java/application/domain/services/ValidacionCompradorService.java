package application.domain.services;

import application.domain.entities.Comprador;
import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

public class ValidacionCompradorService {

    public boolean puedeComprar(Usuario usuario, Comprador comprador) {
        validarEntradas(usuario, comprador);

        return usuario.tieneRol(RolUsuario.Comprador)
                && comprador.getUsuarioId().equals(usuario.getId())
                && usuario.estaActivo()
                && comprador.puedeComprar();
    }

    public void exigirHabilitado(Usuario usuario, Comprador comprador) {
        validarEntradas(usuario, comprador);

        if (!usuario.tieneRol(RolUsuario.Comprador))
            throw new AccesoDenegadoException("El usuario no tiene el rol de comprador.");
        if (!comprador.getUsuarioId().equals(usuario.getId()))
            throw new AccesoDenegadoException("El comprador no corresponde a este usuario.");
        if (!usuario.estaActivo())
            throw new IllegalStateException("El usuario estÃ¡ bloqueado y no puede comprar.");
        if (!comprador.puedeComprar())
            throw new IllegalStateException("El comprador estÃ¡ restringido y no puede realizar compras.");
    }

    private static void validarEntradas(Usuario usuario, Comprador comprador) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario es obligatorio.");
        if (comprador == null)
            throw new IllegalArgumentException("El comprador es obligatorio.");
    }
}