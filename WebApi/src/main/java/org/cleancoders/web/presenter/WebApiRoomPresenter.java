package org.cleancoders.web.presenter;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.usecase.ListRoomsUseCase;
import org.cleancoders.web.dto.room.RoomListResponse;
import org.cleancoders.web.dto.room.RoomResponse;

import java.util.List;

/**
 * WebApi presenter for {@link ListRoomsUseCase}. Public use case — no auth
 * branches, so extends {@link WebApiPresenter} directly rather than
 * {@link WebApiCommonPresenter}. Response body is a {@link RoomListResponse}
 * wrapper object ({"rooms": [...]}) rather than a bare array.
 */
@Singleton
public class WebApiRoomPresenter extends WebApiPresenter implements
        ListRoomsUseCase.Presenter
{

    @Override
    public void presentRooms(List<StudyRoom> rooms)
    {
        List<RoomResponse> dtos = rooms.stream()
                .map(r -> new RoomResponse(r.id(), r.name(), r.location(), r.capacity(), r.status()))
                .toList();
        current.set(Response.ok(new RoomListResponse(dtos)).build());
    }
}
