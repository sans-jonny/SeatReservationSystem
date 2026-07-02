package org.cleancoders.web.resource;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.cleancoders.infrastructure.persistence.InMemoryRoomRepo;
import org.cleancoders.seatandroom.domain.RoomStatus;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.outbound.RoomRepository;
import org.cleancoders.seatandroom.usecase.ListRoomsUseCase;
import org.cleancoders.web.presenter.WebApiRoomPresenter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoomResourceIntegrationTest extends JerseyTest
{

    private InMemoryRoomRepo roomRepo;

    @Override
    protected Application configure()
    {
        roomRepo = new InMemoryRoomRepo();
        WebApiRoomPresenter presenterInstance = new WebApiRoomPresenter();

        ResourceConfig config = new ResourceConfig();
        config.register(RoomResource.class);
        config.register(new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(roomRepo).to(RoomRepository.class);
                bind(ListRoomsUseCase.class).to(ListRoomsUseCase.class);
                bind(presenterInstance).to(WebApiRoomPresenter.class);
                bind(presenterInstance).to(ListRoomsUseCase.Presenter.class);
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
    void shouldReturn200WithOnlyOpenRooms()
    {
        roomRepo.save(new StudyRoom("r1", "A", "L1", 10, RoomStatus.OPEN));
        roomRepo.save(new StudyRoom("r2", "B", "L2", 10, RoomStatus.CLOSED));
        roomRepo.save(new StudyRoom("r3", "C", "L3", 10, RoomStatus.OPEN));
        roomRepo.save(new StudyRoom("r4", "D", "L4", 10, RoomStatus.MAINTENANCE));

        Response response = target("/rooms").request(MediaType.APPLICATION_JSON).get();

        assertEquals(200, response.getStatus());
        // 响应体是包装对象 {"rooms": [...]}
        Map<String, Object> body = response.readEntity(new GenericType<>()
        {
        });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) body.get("rooms");
        assertEquals(2, rooms.size());
        List<String> ids = rooms.stream().map(m -> (String) m.get("id")).toList();
        assertTrue(ids.contains("r1"));
        assertTrue(ids.contains("r3"));
        assertFalse(ids.contains("r2"));
        assertFalse(ids.contains("r4"));
    }

    @Test
    void shouldReturn200WithEmptyArrayWhenNoOpenRooms()
    {
        roomRepo.save(new StudyRoom("r1", "A", "L1", 10, RoomStatus.CLOSED));

        Response response = target("/rooms").request(MediaType.APPLICATION_JSON).get();

        assertEquals(200, response.getStatus());
        Map<String, Object> body = response.readEntity(new GenericType<>()
        {
        });
        @SuppressWarnings("unchecked")
        List<?> rooms = (List<?>) body.get("rooms");
        assertTrue(rooms.isEmpty());
    }
}
