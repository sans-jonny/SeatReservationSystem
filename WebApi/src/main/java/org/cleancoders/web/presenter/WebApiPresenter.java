package org.cleancoders.web.presenter;

import jakarta.ws.rs.core.Response;

public abstract class WebApiPresenter
{
    protected final ThreadLocal<Response> current = new ThreadLocal<>();

    /**
     * Returns the response staged by the most recent presenter call on this
     * thread, or {@code null} if no presenter method has run yet.
     */
    public Response getResponse()
    {
        return current.get();
    }
}
