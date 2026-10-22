package application.domain.ports.in;

import application.domain.entities.Bodega;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.TipoBodega;
import application.domain.valueobjects.Direccion;

/** Puerto de entrada: un administrador registra una bodega del Marketplace o de un vendedor. */
public interface RegistrarBodegaUseCase {

    Bodega registrarDelMarketplace(Usuario administrador, String nombre, TipoBodega tipo, Direccion direccion);

    Bodega registrarDeVendedor(Usuario administrador, Vendedor vendedor, String nombre,
                               TipoBodega tipo, Direccion direccion);
}