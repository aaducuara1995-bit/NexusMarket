package application.adapters.rest.controllers;

import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.responses.AvailabilityResponseDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.mappers.AddressRestMapper;
import application.adapters.rest.mappers.BuyerRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.entities.Buyer;
import application.domain.ports.in.PublicAccessPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Endpoints anyone can call without signing in: buyer sign-up and the catalog. */
@RestController
@RequestMapping("/api/v1/public")
public class PublicAccessRestController {

    private final PublicAccessPort publicAccessPort;
    private final RestReferenceLoader loader;

    public PublicAccessRestController(PublicAccessPort publicAccessPort, RestReferenceLoader loader) {
        this.publicAccessPort = publicAccessPort;
        this.loader = loader;
    }

    @PostMapping("/buyers")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody RegisterBuyerRequestDTO request) {
        Buyer buyer = publicAccessPort.registerBuyer(UserRestMapper.toDomain(request),
                AddressRestMapper.toDomain(request.mainAddress()), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(BuyerRestMapper.toResponseDTO(buyer));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> consultCatalog() {
        return ResponseEntity.ok(publicAccessPort.consultCatalog().stream()
                .map(ProductRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<AvailabilityResponseDTO> consultAvailability(@PathVariable("productId") UUID productId) {
        int available = publicAccessPort.consultAvailability(loader.product(productId));
        return ResponseEntity.ok(new AvailabilityResponseDTO(productId, available));
    }
}