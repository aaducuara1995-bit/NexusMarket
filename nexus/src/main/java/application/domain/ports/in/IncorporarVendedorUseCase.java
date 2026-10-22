package application.domain.ports.in;

import application.domain.entities.Usuario;
import application.domain.enums.TipoBodega;
import application.domain.services.IncorporacionVendedorService;
import application.domain.valueobjects.Direccion;

/** Puerto de entrada: un administrador incorpora a un vendedor junto con su primera bodega. */
public interface IncorporarVendedorUseCase {

    IncorporacionVendedorService.Resultado incorporar(Usuario administrador, Usuario usuarioVendedor,
                                                    String nombreBodega, TipoBodega tipoBodega,
                                                    Direccion direccionBodega);
}