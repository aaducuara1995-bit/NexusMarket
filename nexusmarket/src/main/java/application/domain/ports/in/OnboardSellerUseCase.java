package application.domain.ports.in;

import application.domain.entities.User;
import application.domain.enums.WarehouseType;
import application.domain.services.OnboardSellerService;
import application.domain.valueobjects.Address;

/** Input port: an admin onboards a seller together with their first warehouse. */
public interface OnboardSellerUseCase {

    OnboardSellerService.Result onboard(User admin, User sellerUser,
                                                    String warehouseName, WarehouseType warehouseType,
                                                    Address warehouseAddress);
}