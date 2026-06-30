package org.cleancoders.web.resource;

import jakarta.ws.rs.core.Response;
import org.cleancoders.userandauth.domain.User;
import org.cleancoders.userandauth.domain.UserRole;
import org.cleancoders.userandauth.usecase.LoginUseCase;
import org.cleancoders.web.dto.LoginRequest;
import org.cleancoders.web.dto.RegisterRequest;
import org.cleancoders.web.presenter.WebApiAuthPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResourceTest {

    private AuthResource resource;
    private WebApiAuthPresenter presenter;
    private boolean executeCalled;
    private LoginUseCase.Request lastRequest;
    private LoginUseCase.Output outputToReturn;

    @BeforeEach
    void setUp() {
        presenter = new WebApiAuthPresenter();
        executeCalled = false;
        lastRequest = null;

        resource = new AuthResource();
        resource.presenter = presenter;
        resource.loginUseCase = new LoginUseCase() {
            @Override
            public Output execute(Request request) {
                executeCalled = true;
                lastRequest = request;
                return outputToReturn;
            }
        };
    }

    @Test
    void loginShouldDelegateToUseCase() {
        outputToReturn = new LoginUseCase.Output("test.jwt.token");
        presenter.success("test.jwt.token",
                new User("u1", "alice", "pw", UserRole.STUDENT, "Alice", "a@b.com"));

        Response response = resource.login(new LoginRequest("alice", "secret"));

        assertTrue(executeCalled);
        assertEquals("alice", lastRequest.username());
        assertEquals("secret", lastRequest.password());
        assertEquals(200, response.getStatus());
    }

    @Test
    void loginShouldReturn401OnBadCredentials() {
        outputToReturn = null;
        presenter.invalidCredentials();

        Response response = resource.login(new LoginRequest("alice", "wrong"));

        assertEquals(401, response.getStatus());
    }

    @Test
    void registerShouldReturn501() {
        Response response = resource.register(new RegisterRequest("u", "p", "n", "e"));
        assertEquals(501, response.getStatus());
    }
}
