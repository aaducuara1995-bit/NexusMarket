package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.AuthenticationResult;
import application.domain.valueobjects.Email;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Login: checks email and password and returns the JWT with the user's role and data. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;
    private final long expirationMs;

    public AuthRestController(PublicAccessPort publicAccessPort,
                              @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.publicAccessPort = publicAccessPort;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthenticationResult result = publicAccessPort.login(new Email(request.email()), request.password());
        return ResponseEntity.ok(new LoginResponseDTO(result.token(), "Bearer", expirationMs / 1000,
                UserRestMapper.toResponseDTO(result.user())));
    }
}