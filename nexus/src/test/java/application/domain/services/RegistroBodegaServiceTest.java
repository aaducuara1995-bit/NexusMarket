package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoBodega;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroBodegaServiceTest {

    private final RepositoriosEnMemoria.Bodegas repo = new RepositoriosEnMemoria.Bodegas();
    private final RegistroBodegaService servicio =
            new RegistroBodegaService(new AutorizacionService(), repo);

    private final Usuario admin = Fixtures.usuario(RolUsuario.Administrador);

    @Test
    void elAdministradorRegistraUnaBodegaDelMarketplace() {
        Bodega bodega = servicio.registrarDelMarketplace(admin, "Central", TipoBodega.Fisica,
                Fixtures.direccion());

        assertTrue(bodega.esDelMarketplace());
        assertEquals(1, repo.guardadas.size());
    }

    @Test
    void elAdministradorRegistraLaBodegaDeUnVendedor() {
        Vendedor vendedor = Fixtures.vendedor(Fixtures.usuario(RolUsuario.Vendedor));

        Bodega bodega = servicio.registrarDeVendedor(admin, vendedor, "Bodega Norte",
                TipoBodega.Fisica, Fixtures.direccion());

        assertTrue(bodega.perteneceA(vendedor.getId()));
    }

    @Test
    void rechazaUnNombreDeBodegaRepetido() {
        servicio.registrarDelMarketplace(admin, "Central", TipoBodega.Fisica, Fixtures.direccion());

        assertThrows(IllegalStateException.class,
                () -> servicio.registrarDelMarketplace(admin, "central", TipoBodega.Fisica,
                        Fixtures.direccion()));
        assertEquals(1, repo.guardadas.size());
    }

    @Test
    void unVendedorNoRegistraBodegas() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrarDeVendedor(usuario, Fixtures.vendedor(usuario), "Mia",
                        TipoBodega.Fisica, Fixtures.direccion()));
    }

    @Test
    void unaBodegaFisicaSinDireccionNoSeGuarda() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarDelMarketplace(admin, "Central", TipoBodega.Fisica, null));
        assertEquals(0, repo.guardadas.size());
    }
}