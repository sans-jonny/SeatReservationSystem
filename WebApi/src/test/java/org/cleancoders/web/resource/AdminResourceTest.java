package org.cleancoders.web.resource;

import jakarta.ws.rs.core.Response;
import org.cleancoders.seatandroom.domain.RoomStatus;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.seatandroom.usecase.UpdateRoomUseCase;
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
    private boolean createExecuteCalled;
    private ManageRoomsUseCase.Request lastCreateRequest;
    private ManageRoomsUseCase.Output createOutput;
    private boolean updateExecuteCalled;
    private UpdateRoomUseCase.Request lastUpdateRequest;
    private UpdateRoomUseCase.Output updateOutput;

    @BeforeEach
    void setUp()
    {
        presenter = new WebApiAdminPresenter();
        createExecuteCalled = false;
        lastCreateRequest = null;
        updateExecuteCalled = false;
        lastUpdateRequest = null;

        resource = new AdminResource();
        resource.presenter = presenter;
        resource.manageRoomsUseCase = new ManageRoomsUseCase()
        {
            @Override
            public Output execute(Request request)
            {
                createExecuteCalled = true;
                lastCreateRequest = request;
                return createOutput;
            }
        };
        resource.updateRoomUseCase = new UpdateRoomUseCase()
        {
            @Override
            public Output execute(Request request)
            {
                updateExecuteCalled = true;
                lastUpdateRequest = request;
                return updateOutput;
            }
        };
    }

    // --- create room tests ---

    @Test
    void createRoomShouldDelegateToUseCase()
    {
        StudyRoom room = new StudyRoom("r-new", "自习室F", "综合楼二楼", 20, RoomStatus.OPEN);
        createOutput = new ManageRoomsUseCase.Output("r-new");
        presenter.success(room);

        Response response = resource.createRoom("jwt.token.here",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertTrue(createExecuteCalled);
        assertEquals("jwt.token.here", lastCreateRequest.token());
        assertEquals("自习室F", lastCreateRequest.name());
        assertEquals("综合楼二楼", lastCreateRequest.location());
        assertEquals(20, lastCreateRequest.capacity());
        assertEquals(201, response.getStatus());
    }

    @Test
    void createRoomShouldReturn201OnSuccess()
    {
        StudyRoom room = new StudyRoom("r-new", "自习室G", "图书馆四楼", 15, RoomStatus.OPEN);
        createOutput = new ManageRoomsUseCase.Output("r-new");
        presenter.success(room);

        Response response = resource.createRoom("jwt.token.here",
                new CreateRoomRequest("自习室G", "图书馆四楼", 15));

        assertEquals(201, response.getStatus());
    }

    @Test
    void createRoomShouldReturn409OnDuplicateName()
    {
        createOutput = null;
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
        createOutput = null;
        presenter.invalidToken();

        Response response = resource.createRoom("bad-token",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertEquals(401, response.getStatus());
    }

    @Test
    void createRoomShouldReturn403OnForbidden()
    {
        createOutput = null;
        presenter.forbidden();

        Response response = resource.createRoom("student-token",
                new CreateRoomRequest("自习室F", "综合楼二楼", 20));

        assertEquals(403, response.getStatus());
    }

    // --- update room tests ---

    @Test
    void updateRoomShouldDelegateToUseCase()
    {
        StudyRoom room = new StudyRoom("room-1", "自习室A-改", "图书馆一楼东", 35, RoomStatus.OPEN);
        updateOutput = new UpdateRoomUseCase.Output("room-1");
        presenter.updateSuccess(room);

        Response response = resource.updateRoom("jwt.token.here", "room-1",
                new CreateRoomRequest("自习室A-改", "图书馆一楼东", 35));

        assertTrue(updateExecuteCalled);
        assertEquals("jwt.token.here", lastUpdateRequest.token());
        assertEquals("room-1", lastUpdateRequest.roomId());
        assertEquals("自习室A-改", lastUpdateRequest.name());
        assertEquals("图书馆一楼东", lastUpdateRequest.location());
        assertEquals(35, lastUpdateRequest.capacity());
        assertEquals(200, response.getStatus());
    }

    @Test
    void updateRoomShouldReturn200OnSuccess()
    {
        StudyRoom room = new StudyRoom("room-1", "自习室B", "新位置", 25, RoomStatus.OPEN);
        updateOutput = new UpdateRoomUseCase.Output("room-1");
        presenter.updateSuccess(room);

        Response response = resource.updateRoom("jwt.token.here", "room-1",
                new CreateRoomRequest("自习室B", "新位置", 25));

        assertEquals(200, response.getStatus());
    }

    @Test
    void updateRoomShouldReturn404WhenRoomNotFound()
    {
        updateOutput = null;
        presenter.roomNotFound("nonexistent");

        Response response = resource.updateRoom("jwt.token.here", "nonexistent",
                new CreateRoomRequest("自习室X", "一楼", 10));

        assertEquals(404, response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertEquals("自习室不存在", body.get("error"));
        assertEquals("nonexistent", body.get("roomId"));
    }

    @Test
    void updateRoomShouldReturn409OnDuplicateName()
    {
        updateOutput = null;
        presenter.roomNameAlreadyExists("自习室B");

        Response response = resource.updateRoom("jwt.token.here", "room-1",
                new CreateRoomRequest("自习室B", "图书馆一楼", 30));

        assertEquals(409, response.getStatus());
    }
}