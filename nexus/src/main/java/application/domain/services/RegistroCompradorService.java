package application.domain.services;

import application.domain.entities.Comprador;
import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.ports.out.CompradorRepositorioPort;
import application.domain.valueobjects.Direccion;

public class RegistroCompradorService {

    private final CompradorRepositorioPort compradores;

    public RegistroCompradorService(CompradorRepositorioPort compradores) {
        if (compradores == null)
            throw new IllegalArgumentException("El repositorio de compradores es obligatorio.");
        this.compradores = compradores;
    }

    public Comprador registrar(Usuario usuario, Direccion direccionPrincipal) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario es obligatorio.");
        if (!usuario.tieneRol(RolUsuario.Comprador))
            throw new IllegalStateException("El usuario no tiene el rol de comprador.");
        if (!usuario.estaActivo())
            throw new IllegalStateException("Un usuario bloqueado no puede registrarse como comprador.");
        if (compradores.existePorUsuarioId(usuario.getId()))
            throw new IllegalStateException("El usuario ya tiene un perfil de comprador.");

        Comprador comprador = new Comprador(usuario.getId(), direccionPrincipal);
        compradores.guardar(comprador);
        return comprador;
    }
}