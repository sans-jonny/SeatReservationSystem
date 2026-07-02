package org.cleancoders.web.resource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.cleancoders.common.domain.User;
import org.cleancoders.common.domain.UserRole;
import org.cleancoders.common.outbound.TokenService;
import org.cleancoders.common.outbound.UserRepository;
import org.cleancoders.common.usecase.AdminAuthUseCase;
import org.cleancoders.common.usecase.AuthUseCase;
import org.cleancoders.common_reservation_seatAndRoom.outbound.SeatRepository;
import org.cleancoders.common_reservation_seatAndRoom.outbound.TimeSlotRepository;
import org.cleancoders.infrastructure.persistence.InMemoryRoomRepo;
import org.cleancoders.infrastructure.persistence.InMemorySeatRepo;
import org.cleancoders.infrastructure.persistence.InMemoryTimeSlotRepo;
import org.cleancoders.infrastructure.persistence.InMemoryUserRepo;
import org.cleancoders.infrastructure.security.JjwtTokenService;
import org.cleancoders.seatandroom.domain.RoomStatus;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.outbound.RoomRepository;
import org.cleancoders.seatandroom.usecase.ListRoomsUseCase;
import org.cleancoders.seatandroom.usecase.ListSeatsUseCase;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.web.dto.admin.CreateRoomRequest;
import org.cleancoders.web.presenter.WebApiAdminPresenter;
import org.cleancoders.web.presenter.WebApiRoomPresenter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminResourceIntegrationTest extends JerseyTest
{

    private InMemoryRoomRepo roomRepo;
    private JjwtTokenService tokenService;

    @Override
    protected Application configure()
    {
        roomRepo = new InMemoryRoomRepo();
        InMemorySeatRepo seatRepo = new InMemorySeatRepo();
        InMemoryTimeSlotRepo timeSlotRepo = new InMemoryTimeSlotRepo();
        InMemoryUserRepo userRepo = new InMemoryUserRepo();
        tokenService = new JjwtTokenService();

        // Pre-seed admin user
        userRepo.save(new User("admin-1", "admin", "admin123", UserRole.ADMIN, "Admin", "admin@example.com"));
        // Pre-seed student user
        userRepo.save(new User("student-1", "alice", "pass123", UserRole.STUDENT, "Alice", "a@b.com"));

        WebApiRoomPresenter roomPresenter = new WebApiRoomPresenter();
        WebApiAdminPresenter adminPresenter = new WebApiAdminPresenter();

        ResourceConfig config = new ResourceConfig();
        config.register(RoomResource.class);
        config.register(AdminResource.class);
        config.register(new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(roomRepo).to(RoomRepository.class);
                bind(seatRepo).to(SeatRepository.class);
                bind(timeSlotRepo).to(TimeSlotRepository.class);
                bind(userRepo).to(UserRepository.class);
                bind(tokenService).to(TokenService.class);

                bind(ListRoomsUseCase.class).to(ListRoomsUseCase.class);
                bind(ListSeatsUseCase.class).to(ListSeatsUseCase.class);
                bind(ManageRoomsUseCase.class).to(ManageRoomsUseCase.class);

                bind(roomPresenter).to(ListRoomsUseCase.Presenter.class);
                bind(roomPresenter).to(ListSeatsUseCase.Presenter.class);
                bind(adminPresenter).to(ManageRoomsUseCase.Presenter.class);
                bind(adminPresenter).to(AdminAuthUseCase.Presenter.class);
                bind(adminPresenter).to(AuthUseCase.Presenter.class);
            }
        });
        return config;
    }

    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    void shouldReturn201WhenAdminCreatesRoom()
    {
        String adminToken = tokenService.generate("admin-1");

        Response response = target("/admin/rooms")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", adminToken)
                .post(Entity.json(new CreateRoomRequest("自习室F", "综合楼二楼", 20)));

        assertEquals(201, response.getStatus());
        Map<String, Object> body = response.readEntity(new GenericType<>()
        {
        });
        assertNotNull(body.get("id"));
        assertEquals("自习室F", body.get("name"));
        assertEquals("综合楼二楼", body.get("location"));
        assertEquals(20, body.get("capacity"));
        assertEquals("OPEN", body.get("status"));
    }

    @Test
    void shouldReturn403WhenStudentCreatesRoom()
    {
        String studentToken = tokenService.generate("student-1");

        Response response = target("/admin/rooms")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", studentToken)
                .post(Entity.json(new CreateRoomRequest("自习室F", "综合楼二楼", 20)));

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldReturn409WhenNameAlreadyExists()
    {
        roomRepo.save(new StudyRoom("r-existing", "自习室F", "图书馆一楼", 30, RoomStatus.OPEN));

        String adminToken = tokenService.generate("admin-1");

        Response response = target("/admin/rooms")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", adminToken)
                .post(Entity.json(new CreateRoomRequest("自习室F", "综合楼二楼", 20)));

        assertEquals(409, response.getStatus());
        Map<String, Object> body = response.readEntity(new GenericType<>()
        {
        });
        assertEquals("自习室名称已存在", body.get("error"));
    }

    @Test
    void shouldReturn401WhenNoToken()
    {
        Response response = target("/admin/rooms")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new CreateRoomRequest("自习室F", "综合楼二楼", 20)));

        assertEquals(401, response.getStatus());
    }
}