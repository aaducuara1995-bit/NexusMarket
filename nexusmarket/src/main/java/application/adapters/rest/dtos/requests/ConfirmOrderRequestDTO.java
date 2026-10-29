package application.adapters.rest.dtos.requests;

import application.adapters.rest.dtos.AddressDTO;
import jakarta.validation.Valid;

/** Optional body: without deliveryAddress the order goes to the buyer's main address. */
public record ConfirmOrderRequestDTO(@Valid AddressDTO deliveryAddress) {
}