package application.adapters.rest.mappers;

import application.adapters.rest.dtos.AddressDTO;
import application.domain.valueobjects.Address;

/** Address value object <-> AddressDTO. */
public final class AddressRestMapper {

    private AddressRestMapper() {
    }

    public static Address toDomain(AddressDTO dto) {
        return dto == null ? null : new Address(dto.street(), dto.city(), dto.department(), dto.postalCode());
    }

    public static AddressDTO toDTO(Address address) {
        return address == null ? null
                : new AddressDTO(address.street(), address.city(), address.department(), address.postalCode());
    }
}