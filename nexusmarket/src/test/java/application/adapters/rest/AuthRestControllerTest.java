package application.adapters.rest;

import application.adapters.rest.dtos.AddressDTO;
import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AuthRestControllerTest {

    private static final AddressDTO HOME = new AddressDTO("Calle 10 # 20-30", "Medellin", "Antioquia", "050001");

    @Test
    void theAdminSignsInAndGetsABearerToken() {
        RestTestContext ctx = new RestTestContext();

        ResponseEntity<LoginResponseDTO> response = ctx.authController.login(
                new LoginRequestDTO("admin@nexusmarket.com", RestTestContext.ADMIN_PASSWORD));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-" + ctx.admin.getId(), response.getBody().token());
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals(3600, response.getBody().expiresInSeconds());
        assertEquals(UserRole.ADMIN, response.getBody().user().role());
    }

    @Test
    void aBuyerSignsInWithThePasswordChosenAtSignUp() {
        RestTestContext ctx = new RestTestContext();
        ctx.publicController.registerBuyer(new RegisterBuyerRequestDTO("Ana", "Perez", "ana@mail.com", "123",
                "mi-clave-segura", HOME));

        LoginResponseDTO login = ctx.authController.login(new LoginRequestDTO("ana@mail.com", "mi-clave-segura")).getBody();

        assertEquals(UserRole.BUYER, login.user().role());
        assertThrows(InvalidCredentialsException.class,
                () -> ctx.authController.login(new LoginRequestDTO("ana@mail.com", "otra-clave")));
    }

    @Test
    void aBlockedUserIsTurnedAwayAtLogin() {
        RestTestContext ctx = new RestTestContext();
        ctx.publicController.registerBuyer(new RegisterBuyerRequestDTO("Ana", "Perez", "ana@mail.com", "123",
                RestTestContext.PASSWORD, HOME));
        ctx.adminController.blockUser(ctx.admin, ctx.user("ana@mail.com").getId());

        assertThrows(AccessDeniedException.class,
                () -> ctx.authController.login(new LoginRequestDTO("ana@mail.com", RestTestContext.PASSWORD)));
    }

    @Test
    void wrongCredentialsAnswer401() {
        RestTestContext ctx = new RestTestContext();

        InvalidCredentialsException error = assertThrows(InvalidCredentialsException.class,
                () -> ctx.authController.login(new LoginRequestDTO("admin@nexusmarket.com", "wrong-password")));

        assertEquals(401, ctx.errors.handleInvalidCredentials(error,
                RestTestContext.request("POST", "/api/v1/auth/login")).getBody().status());
    }
}