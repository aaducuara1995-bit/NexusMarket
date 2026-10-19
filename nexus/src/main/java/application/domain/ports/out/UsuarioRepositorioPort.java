package application.domain.ports.out;

import application.domain.entities.Usuario;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;

public interface UsuarioRepositorioPort {

    boolean existePorCorreo(CorreoElectronico correo);

    boolean existePorDocumento(DocumentoIdentidad documento);

    void guardar(Usuario usuario);
}