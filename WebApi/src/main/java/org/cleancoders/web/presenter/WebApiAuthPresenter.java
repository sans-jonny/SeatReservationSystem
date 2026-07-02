package org.cleancoders.web.presenter;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.cleancoders.common.domain.User;
import org.cleancoders.userandauth.usecase.GetMeUseCase;
import org.cleancoders.userandauth.usecase.LoginUseCase;
import org.cleancoders.userandauth.usecase.RegisterUseCase;
import org.cleancoders.web.dto.auth.LoginResponse;
import org.cleancoders.web.dto.auth.MeResponse;
import org.cleancoders.web.dto.auth.RegisterResponse;
import org.cleancoders.web.dto.auth.UsernameConflictResponse;
import org.cleancoders.web.dto.common.ErrorResponse;
import org.cleancoders.web.dto.common.UserResponse;

@Singleton
public class WebApiAuthPresenter extends WebApiCommonPresenter implements
        LoginUseCase.Presenter,
        RegisterUseCase.Presenter,
        GetMeUseCase.Presenter
{

    // --- LoginUseCase.Presenter ---

    @Override
    public void success(String token, User user)
    {
        NewCookie authCookie = new NewCookie.Builder("Authorization")
                .value(token)
                .path("/api")
                .httpOnly(true)
                .build();

        current.set(Response.ok(new LoginResponse(token, toUserResponse(user)))
                .cookie(authCookie).build());
    }

    @Override
    public void invalidCredentials()
    {
        current.set(Response.status(401).entity(new ErrorResponse("Invalid credentials")).build());
    }

    // --- RegisterUseCase.Presenter ---

    @Override
    public void success(User user)
    {
        current.set(Response.status(201).entity(new RegisterResponse(toUserResponse(user))).build());
    }

    @Override
    public void usernameAlreadyExists(String username)
    {
        current.set(Response.status(409)
                .entity(new UsernameConflictResponse("Username already exists", username))
                .build());
    }

    // --- GetMeUseCase.Presenter ---

    @Override
    public void presentUser(User user)
    {
        current.set(Response.ok(new MeResponse(toUserResponse(user))).build());
    }

    private UserResponse toUserResponse(User user)
    {
        return new UserResponse(user.id(), user.username(), user.role(), user.name(), user.email());
    }
}
