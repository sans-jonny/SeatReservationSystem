package org.cleancoders.web.resource;

import jakarta.ws.rs.core.Response;
import org.cleancoders.seatandroom.domain.RoomStatus;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.web.dto.admin.CreateRoomRequest;
import org.cleancoders.web.presenter.WebApiAdminPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminResourceTest
{

    private AdminResource resource;
    private WebApiAdminPresenter presenter;
    private boolean executeCalled;
    private ManageRoomsUseCase.Request lastRequest;
    private ManageRoomsUseCase.Output outputToReturn;

    @BeforeEach
    void setUp()
    {
        presenter = new WebApiAdminPresenter();
        executeCalled = false;
        lastRequest = null;

        resource = new AdminResource();
        resource.presenter = presenter;
        resource.manageRoomsUseCase = new ManageRoomsUseCase()
        {
            @Override
            public Output execute(Request request)
            {
                executeCalled = true;
                lastRequest = request;
                return outputToReturn;
            }
        };
    }

    @Test
    void createRoomShouldDelegateToUseCase()
    {
        StudyRoom room = new StudyRoom("r-new", "自习室F", "综合楼二楼", 20, RoomStatus.OPEN);
        outputToReturn = new ManageRoomsUseCase.Output("r-new");
        presenter.success(room);

        Response response = resource.createRoom("jwt.token.here",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertTrue(executeCalled);
        assertEquals("jwt.token.here", lastRequest.token());
        assertEquals("自习室F", lastRequest.name());
        assertEquals("综合楼二楼", lastRequest.location());
        assertEquals(20, lastRequest.capacity());
        assertEquals(201, response.getStatus());
    }

    @Test
    void createRoomShouldReturn201OnSuccess()
    {
        StudyRoom room = new StudyRoom("r-new", "自习室G", "图书馆四楼", 15, RoomStatus.OPEN);
        outputToReturn = new ManageRoomsUseCase.Output("r-new");
        presenter.success(room);

        Response response = resource.createRoom("jwt.token.here",
                new CreateRoomRequest("自习室G", "图书馆四楼", 15));

        assertEquals(201, response.getStatus());
    }

    @Test
    void createRoomShouldReturn409OnDuplicateName()
    {
        outputToReturn = null;
        presenter.roomNameAlreadyExists("自习室F");

        Response response = resource.createRoom("jwt.token.here",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertEquals(409, response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertEquals("自习室名称已存在", body.get("error"));
        assertEquals("自习室F", body.get("name"));
    }

    @Test
    void createRoomShouldReturn401OnInvalidToken()
    {
        outputToReturn = null;
        presenter.invalidToken();

        Response response = resource.createRoom("bad-token",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertEquals(401, response.getStatus());
    }

    @Test
    void createRoomShouldReturn403OnForbidden()
    {
        outputToReturn = null;
        presenter.forbidden();

        Response response = resource.createRoom("student-token",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertEquals(403, response.getStatus());
    }
}