package application.adapters.rest.dtos.responses;

/** The token goes in every following request as "Authorization: Bearer <token>". */
public record LoginResponseDTO(String token, String tokenType, long expiresInSeconds, UserResponseDTO user) {
}