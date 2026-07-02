package org.cleancoders.web.presenter;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.web.dto.room.RoomResponse;

import java.util.Map;

/**
 * WebApi presenter for admin use cases ({@link ManageRoomsUseCase}).
 * Extends {@link WebApiCommonPresenter} to inherit auth error branches
 * (401 invalid token, 404 user not found, 403 forbidden).
 */
@Singleton
public class WebApiAdminPresenter extends WebApiCommonPresenter implements
        ManageRoomsUseCase.Presenter
{

    @Override
    public void success(StudyRoom room)
    {
        current.set(Response.status(201).entity(
                new RoomResponse(room.id(), room.name(), room.location(), room.capacity(), room.status())
        ).build());
    }

    @Override
    public void roomNameAlreadyExists(String name)
    {
        current.set(Response.status(409).entity(Map.of(
                "error", "自习室名称已存在",
                "name", name
        )).build());
    }
}