package application.adapters.rest.dtos.responses;

import java.time.LocalDateTime;

/** Body of every error answer: when, which HTTP status, a short message and the path called. */
public record ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
}