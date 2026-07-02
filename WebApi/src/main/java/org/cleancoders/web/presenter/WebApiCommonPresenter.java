package org.cleancoders.web.presenter;

import jakarta.ws.rs.core.Response;
import org.cleancoders.common.usecase.AdminAuthUseCase;
import org.cleancoders.common.usecase.AuthUseCase;
import org.cleancoders.common.usecase.StudentAuthUseCase;
import org.cleancoders.web.dto.common.ErrorResponse;

/**
 * Shared base for all WebApi presenters that back authenticated use cases.
 * <p>
 * Implements the three auth-related presenter interfaces defined in the
 * {@code Common} module so every leaf presenter inherits the same default
 * HTTP responses for the branches handled by {@link AuthUseCase}'s template
 * method:
 * <ul>
 *   <li>{@link #invalidToken()} — 401, token missing/invalid/expired</li>
 *   <li>{@link #userNotFound()} — 404, token valid but user no longer exists</li>
 *   <li>{@link #forbidden()} — 403, role check failed (default message; leaf
 *       presenters may override with a context-specific message)</li>
 * </ul>
 * <p>
 * The {@link Response} is stored in the {@link ThreadLocal} inherited from
 * {@link WebApiPresenter}, and {@link #getResponse()} reads that same field —
 * so subclasses must not re-declare {@code current} or {@code getResponse()}
 * (doing so would shadow the inherited field and silently drop the response).
 */
public class WebApiCommonPresenter extends WebApiPresenter implements
        StudentAuthUseCase.Presenter,
        AdminAuthUseCase.Presenter,
        AuthUseCase.Presenter
{
    @Override
    public void invalidToken()
    {
        current.set(Response.status(401).entity(new ErrorResponse("Invalid or expired token")).build());
    }

    @Override
    public void userNotFound()
    {
        current.set(Response.status(404).entity(new ErrorResponse("User not found")).build());
    }

    @Override
    public void forbidden()
    {
        current.set(Response.status(403).entity(new ErrorResponse("权限不足，拒绝访问")).build());
    }
}
