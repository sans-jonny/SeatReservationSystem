package org.cleancoders.web.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.cleancoders.userandauth.usecase.LoginUseCase;
import org.cleancoders.userandauth.usecase.RegisterUseCase;
import org.cleancoders.web.dto.LoginRequest;
import org.cleancoders.web.dto.RegisterRequest;
import org.cleancoders.web.presenter.WebApiAuthPresenter;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject LoginUseCase loginUseCase;
    @Inject RegisterUseCase registerUseCase;
    @Inject WebApiAuthPresenter presenter;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        loginUseCase.execute(new LoginUseCase.Request(request.username(), request.password()));
        return presenter.getResponse();
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        registerUseCase.execute(new RegisterUseCase.Request(
                request.username(), request.password(), request.name(), request.email()));
        return presenter.getResponse();
    }
}
