package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.TipoBodega;
import application.domain.ports.out.BodegaRepositorioPort;
import application.domain.valueobjects.Direccion;

public class RegistroBodegaService {

    private final AutorizacionService autorizacion;
    private final BodegaRepositorioPort bodegas;

    public RegistroBodegaService(AutorizacionService autorizacion, BodegaRepositorioPort bodegas) {
        if (autorizacion == null || bodegas == null)
            throw new IllegalArgumentException("Las dependencias del servicio son obligatorias.");
        this.autorizacion = autorizacion;
        this.bodegas = bodegas;
    }

    public Bodega registrarDelMarketplace(Usuario administrador, String nombre,
                                          TipoBodega tipo, Direccion direccion) {
        validar(administrador, nombre);
        Bodega bodega = Bodega.delMarketplace(nombre, tipo, direccion);
        bodegas.guardar(bodega);
        return bodega;
    }

    public Bodega registrarDeVendedor(Usuario administrador, Vendedor vendedor, String nombre,
                                      TipoBodega tipo, Direccion direccion) {
        validar(administrador, nombre);
        if (vendedor == null)
            throw new IllegalArgumentException("El vendedor es obligatorio.");

        Bodega bodega = Bodega.deVendedor(vendedor.getId(), nombre, tipo, direccion);
        bodegas.guardar(bodega);
        return bodega;
    }

    private void validar(Usuario administrador, String nombre) {
        autorizacion.exigirPermiso(administrador, ProcesoNegocio.AdministracionBodegas);
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la bodega no puede estar vacío.");
        if (bodegas.existePorNombre(nombre.trim()))
            throw new IllegalStateException("Ya existe una bodega con ese nombre.");
    }
}