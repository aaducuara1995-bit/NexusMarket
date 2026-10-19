package application.domain.services;

import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AutorizacionService {

    private static final Map<ProcesoNegocio, Set<RolUsuario>> PERMISOS =
            new EnumMap<>(ProcesoNegocio.class);

    static {
        // Matriz de responsabilidades (sección 12 de la especificación)
        PERMISOS.put(ProcesoNegocio.RegistroVendedores,
                EnumSet.of(RolUsuario.Administrador));
        PERMISOS.put(ProcesoNegocio.RegistroProductos,
                EnumSet.of(RolUsuario.Vendedor));
        PERMISOS.put(ProcesoNegocio.AdministracionInventario,
                EnumSet.of(RolUsuario.Vendedor, RolUsuario.OperadorLogistico));
        PERMISOS.put(ProcesoNegocio.GestionPedidos,
                EnumSet.of(RolUsuario.Comprador, RolUsuario.Vendedor, RolUsuario.OperadorLogistico));
        PERMISOS.put(ProcesoNegocio.GestionReembolsos,
                EnumSet.of(RolUsuario.Comprador, RolUsuario.Administrador));

        // Descritos en la tabla de participantes (sección 5)
        PERMISOS.put(ProcesoNegocio.AdministracionBodegas,
                EnumSet.of(RolUsuario.Administrador));
        PERMISOS.put(ProcesoNegocio.OperacionDespachos,
                EnumSet.of(RolUsuario.OperadorLogistico));

        // Supuesto: se gestiona igual que los reembolsos (el documento no lo define)
        PERMISOS.put(ProcesoNegocio.GestionDevoluciones,
                EnumSet.of(RolUsuario.Comprador, RolUsuario.Administrador));
    }

    public boolean puedeEjecutar(Usuario usuario, ProcesoNegocio proceso) {
        validarEntradas(usuario, proceso);
        return usuario.estaActivo() && PERMISOS.get(proceso).contains(usuario.getRol());
    }

    public void exigirPermiso(Usuario usuario, ProcesoNegocio proceso) {
        validarEntradas(usuario, proceso);

        if (!usuario.estaActivo())
            throw new AccesoDenegadoException(
                    "El usuario está bloqueado y no puede ejecutar operaciones.");

        if (!PERMISOS.get(proceso).contains(usuario.getRol()))
            throw new AccesoDenegadoException(
                    "El rol " + usuario.getRol() + " no puede ejecutar " + proceso + ".");
    }

    private static void validarEntradas(Usuario usuario, ProcesoNegocio proceso) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario es obligatorio.");
        if (proceso == null)
            throw new IllegalArgumentException("El proceso es obligatorio.");
    }
}

