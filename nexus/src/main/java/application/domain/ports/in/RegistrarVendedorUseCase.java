package application.domain.ports.in;

import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

/** Puerto de entrada: un administrador crea el perfil de vendedor de un usuario. */
public interface RegistrarVendedorUseCase {

    Vendedor registrar(Usuario administrador, Usuario usuarioVendedor);
}