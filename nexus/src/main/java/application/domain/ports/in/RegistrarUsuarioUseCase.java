package application.domain.ports.in;

import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;

/** Puerto de entrada: registra un usuario con correo y documento únicos en la plataforma. */
public interface RegistrarUsuarioUseCase {

    Usuario registrar(Usuario actor, NombreCompleto nombre, CorreoElectronico correo,
                      DocumentoIdentidad documento, RolUsuario rol);
}