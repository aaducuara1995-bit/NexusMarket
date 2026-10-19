package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Comprador;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.ports.out.BodegaRepositorioPort;
import application.domain.ports.out.CompradorRepositorioPort;
import application.domain.ports.out.ProductoRepositorioPort;
import application.domain.ports.out.UsuarioRepositorioPort;
import application.domain.ports.out.VendedorRepositorioPort;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Implementaciones simples de los puertos de salida, solo para las pruebas. */
final class RepositoriosEnMemoria {

    private RepositoriosEnMemoria() {
    }

    static class Usuarios implements UsuarioRepositorioPort {
        final List<Usuario> guardados = new ArrayList<>();

        public boolean existePorCorreo(CorreoElectronico correo) {
            return guardados.stream().anyMatch(u -> u.getCorreo().equals(correo));
        }

        public boolean existePorDocumento(DocumentoIdentidad documento) {
            return guardados.stream().anyMatch(u -> u.getDocumento().equals(documento));
        }

        public void guardar(Usuario usuario) {
            guardados.add(usuario);
        }
    }

    static class Compradores implements CompradorRepositorioPort {
        final List<Comprador> guardados = new ArrayList<>();

        public boolean existePorUsuarioId(UUID usuarioId) {
            return guardados.stream().anyMatch(c -> c.getUsuarioId().equals(usuarioId));
        }

        public void guardar(Comprador comprador) {
            guardados.add(comprador);
        }
    }

    static class Vendedores implements VendedorRepositorioPort {
        final List<Vendedor> guardados = new ArrayList<>();

        public boolean existePorUsuarioId(UUID usuarioId) {
            return guardados.stream().anyMatch(v -> v.getUsuarioId().equals(usuarioId));
        }

        public void guardar(Vendedor vendedor) {
            guardados.add(vendedor);
        }
    }

    static class Bodegas implements BodegaRepositorioPort {
        final List<Bodega> guardadas = new ArrayList<>();

        public boolean existePorNombre(String nombre) {
            return guardadas.stream().anyMatch(b -> b.getNombre().equalsIgnoreCase(nombre));
        }

        public void guardar(Bodega bodega) {
            guardadas.add(bodega);
        }
    }

    static class Productos implements ProductoRepositorioPort {
        final List<Producto> guardados = new ArrayList<>();

        public boolean existeNombreParaVendedor(UUID vendedorId, String nombre) {
            return guardados.stream().anyMatch(p ->
                    p.getVendedorId().equals(vendedorId) && p.getNombre().equalsIgnoreCase(nombre));
        }

        public void guardar(Producto producto) {
            guardados.add(producto);
        }
    }
}