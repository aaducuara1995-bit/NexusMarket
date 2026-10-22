package application.domain.ports.in;

import application.domain.entities.Comprador;
import application.domain.entities.Usuario;
import application.domain.valueobjects.Direccion;

/** Puerto de entrada: crea el perfil de comprador de un usuario. */
public interface RegistrarCompradorUseCase {

    Comprador registrar(Usuario usuario, Direccion direccionPrincipal);
}